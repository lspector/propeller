(ns propeller.gp
  (:require [clojure.string]
            [clojure.pprint]
            [propeller.genome :as genome]
            [propeller.simplification :as simplification]
            [propeller.variation :as variation]
            [propeller.downsample :as downsample]
            [propeller.hyperselection :as hyperselection]
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
           max-initial-plushy-size solution-error-threshold mapper ds-parent-rate ds-parent-gens dont-end]
    :or   {solution-error-threshold 0.0
           dont-end false
           ds-parent-rate 0
           ds-parent-gens 1
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
    (let [training-data (if (= (:parent-selection argmap) :ds-lexicase)
                          (case (:ds-function argmap)
                            :case-avg (downsample/select-downsample-avg indexed-training-data argmap)
                            :case-maxmin (downsample/select-downsample-maxmin indexed-training-data argmap)
                            (downsample/select-downsample-random indexed-training-data argmap))
                          indexed-training-data) ;defaults to random
          parent-reps (if (zero? (mod generation ds-parent-gens)) ;every ds-parent-gens generations
                        (take (* ds-parent-rate (count population)) (shuffle population))
                        '()) ;else just empty list
          rep-evaluated-pop (sort-by :total-error
                                     (mapper
                                      (partial error-function argmap indexed-training-data)
                                      parent-reps))
          ds-evaluated-pop (sort-by :total-error
                                    (mapper
                                     (partial error-function argmap training-data)
                                     population))
          best-individual (first ds-evaluated-pop)
          best-individual-passes-ds (and (= (:parent-selection argmap) :ds-lexicase) (<= (:total-error best-individual) solution-error-threshold))]
      (prn {:ds-indices-list (map #(:index %) training-data)})
      ;(if (sequential? (:input1 (first training-data)))
        ;(prn {:ds-inputs (map #(first (:input1 %)) training-data)})
        ;(prn {:ds-inputs (map #(:input1 %) training-data)}))
      (if (:custom-report argmap)
        ((:custom-report argmap) ds-evaluated-pop generation argmap)
        (report ds-evaluated-pop generation argmap))
      ;;did the indvidual pass all cases in ds?
      (when best-individual-passes-ds
        (prn {:semi-success-generation generation}))
      (cond
        ;; Success on training cases is verified on testing cases
        (if (or (and best-individual-passes-ds (<= (:total-error (error-function argmap indexed-training-data best-individual)) solution-error-threshold))
                     (and (not= (:parent-selection argmap) :ds-lexicase)
                          (<= (:total-error best-individual) solution-error-threshold)))
               (do (prn {:success-generation generation})
                   (prn {:total-test-error
                         (:total-error (error-function argmap (:testing-data argmap) best-individual))})
                   (when (:simplification? argmap)
                     (let [simplified-plushy (simplification/auto-simplify-plushy (:plushy best-individual) error-function argmap)]
                       (prn {:total-test-error-simplified (:total-error (error-function argmap (:testing-data argmap) (hash-map :plushy simplified-plushy)))})))
                   (if dont-end false true))
               false)
        nil
        ;;
        (>= generation max-generations)
        nil
        ;;
        :else (recur (inc generation)
                     (let [reindexed-pop (hyperselection/reindex-pop ds-evaluated-pop)]
                       (if (:elitism argmap)
                         (hyperselection/log-hyperselection-and-ret (conj (repeatedly (dec population-size)
                                                                                      #(variation/new-individual reindexed-pop argmap))
                                                                          (first reindexed-pop)))
                         (hyperselection/log-hyperselection-and-ret (repeatedly population-size ;need to count occurance of each parent, and reset IDs
                                                                                #(variation/new-individual reindexed-pop argmap)))))
                     (if (= (:parent-selection argmap) :ds-lexicase)
                       (if (zero? (mod generation ds-parent-gens))
                         (downsample/update-case-distances rep-evaluated-pop indexed-training-data indexed-training-data) ; update distances every ds-parent-gens generations
                         indexed-training-data)
                       indexed-training-data))))))