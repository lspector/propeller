(ns propeller.gp
  (:require [clojure.string]
            [clojure.pprint]
            [propeller.genome :as genome]
            [propeller.simplification :as simplification]
            [propeller.variation :as variation]
            [propeller.downsample :as downsample]
            [propeller.push.instructions.bool]
            [propeller.push.instructions.character]
            [propeller.push.instructions.code]
            [propeller.push.instructions.input-output]
            [propeller.push.instructions.numeric]
            [propeller.push.instructions.polymorphic]
            [propeller.push.instructions.string]
            [propeller.push.instructions.vector]))

(defn report
  "Reports information each generation."
  [pop generation argmap]
  (let [best (first pop)]
    (clojure.pprint/pprint {:generation            generation
                            :best-plushy           (:plushy best)
                            :best-program          (genome/plushy->push (:plushy best) argmap)
                            :best-total-error      (:total-error best)
                            :best-errors           (:errors best)
                            :best-behaviors        (:behaviors best)
                            :genotypic-diversity   (float (/ (count (distinct (map :plushy pop))) (count pop)))
                            :behavioral-diversity  (float (/ (count (distinct (map :behaviors pop))) (count pop)))
                            :average-genome-length (float (/ (reduce + (map count (map :plushy pop))) (count pop)))
                            :average-total-error   (float (/ (reduce + (map :total-error pop)) (count pop)))})
    (println)))

(defn gp
  "Main GP loop."
  [{:keys [population-size max-generations error-function instructions
           max-initial-plushy-size solution-error-threshold mapper]
    :or   {solution-error-threshold 0.0
           ;; The `mapper` will perform a `map`-like operation to apply a function to every individual
           ;; in the population. The default is `map` but other options include `mapv`, or `pmap`.
           mapper #?(:clj pmap :cljs map)}
    :as   argmap}]
  ;;
  (prn {:starting-args (update (update argmap :error-function str) :instructions str)})
  (println)
  ;;
  (loop [generation 0
         population (mapper
                     (fn [_] {:plushy (genome/make-random-plushy instructions max-initial-plushy-size)})
                     (range population-size))
         indexed-training-data (downsample/assign-indices-to-data (downsample/initialize-case-distances argmap))]
    ;;TODO: REMOVE THIS IT IS JUST FOR TESTING
    (prn {:data (some #(when (zero? (:index %)) %) indexed-training-data)})
    (let [training-data (if (= (:parent-selection argmap) :ds-lexicase)
                            (case (:ds-function argmap)
                               :case-tournament (downsample/select-downsample-tournament indexed-training-data argmap)
                               (downsample/select-downsample-random indexed-training-data argmap))
                          indexed-training-data) ;defaults to random
          full-evaluated-pop (sort-by :total-error
                                 (mapper
                                  (partial error-function argmap indexed-training-data)
                                  population))
          ds-evaluated-pop (sort-by :total-error
                                    (mapper
                                     (partial error-function argmap training-data)
                                     population))
          best-individual (first ds-evaluated-pop)
          best-individual-passes-ds (and (= (:parent-selection argmap) :ds-lexicase) (<= (:total-error best-individual) solution-error-threshold))
          tot-evaluated-pop (when best-individual-passes-ds ;evaluate the whole pop on all training data
                              (sort-by :total-error
                                       (mapper
                                        (partial error-function argmap (:training-data argmap))
                                        population)))
          ;;best individual on all training-cases
          tot-best-individual (if best-individual-passes-ds (first tot-evaluated-pop) best-individual)]
      (prn (first training-data))
      (if (:custom-report argmap)
        ((:custom-report argmap) ds-evaluated-pop generation argmap)
        (report ds-evaluated-pop generation argmap))
      ;;did the indvidual pass all cases in ds?
      (when best-individual-passes-ds
        (prn {:semi-success-generation generation}))
      (cond
        ;; Success on training cases is verified on testing cases
        (or (and best-individual-passes-ds (<= (:total-error tot-best-individual) solution-error-threshold))
            (and (not= (:parent-selection argmap) :ds-lexicase)
                 (<= (:total-error best-individual) solution-error-threshold)))
        (do (prn {:success-generation generation})
            (prn {:total-test-error
                  (:total-error (error-function argmap (:testing-data argmap) tot-best-individual))})
            (when (:simplification? argmap)
              (let [simplified-plushy (simplification/auto-simplify-plushy (:plushy tot-best-individual) error-function argmap)]
                (prn {:total-test-error-simplified (:total-error (error-function argmap (:testing-data argmap) (hash-map :plushy simplified-plushy)))}))))
        ;;
        (>= generation max-generations)
        nil
        ;;
        :else (recur (inc generation)
                     (if (:elitism argmap)
                       (conj (repeatedly (dec population-size)
                                         #(variation/new-individual ds-evaluated-pop argmap))
                             (first ds-evaluated-pop))
                       (repeatedly population-size
                                   #(variation/new-individual ds-evaluated-pop argmap)))
                     (if (= (:parent-selection argmap) :ds-lexicase)
                         (downsample/update-case-distances full-evaluated-pop indexed-training-data indexed-training-data) ; update distances every generation
                       indexed-training-data))))))