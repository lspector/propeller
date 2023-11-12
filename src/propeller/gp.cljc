(ns propeller.gp
  "Main genetic programming loop."
  (:require [clojure.string]
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
            [propeller.push.instructions.vector]
            [propeller.selection :as selection]
            [propeller.utils :as utils]))

(defn report
  "Reports information each generation."
  [evaluations pop generation argmap training-data]
  (let [best (first pop)]
    (utils/pretty-map-println
     {:generation            generation
      :best-plushy           (:plushy best)
      :best-program          (genome/plushy->push (:plushy best) argmap)
      :best-total-error      (:total-error best)
      :evaluations           evaluations
      :ds-indices            (if (:downsample? argmap)
                              (map #(:index %) training-data)
                               nil)
      :best-errors           (:errors best)
      :best-behaviors        (:behaviors best)
      :genotypic-diversity   (float (/ (count (distinct (map :plushy pop))) (count pop)))
      :behavioral-diversity  (float (/ (count (distinct (map :behaviors pop))) (count pop)))
      :average-genome-length (float (/ (reduce + (map count (map :plushy pop))) (count pop)))
      :average-total-error   (float (/ (reduce + (map :total-error pop)) (count pop)))})))

(defn cleanup
  []
  #?(:clj (shutdown-agents))
  (prn {:run-completed true})
  nil)

(defn gp-loop
  "Main GP loop."
  [{:keys [population-size max-generations error-function instructions
           max-initial-plushy-size solution-error-threshold ds-parent-rate ds-parent-gens dont-end ids-type downsample?]
    :or   {solution-error-threshold 0.0
           dont-end false
           ds-parent-rate 0
           ds-parent-gens 1
           ids-type :solved ; :solved or :elite or :soft
           downsample? false}
    :as   argmap}]
  ;;
  (prn {:starting-args (update (update argmap :error-function str) :instructions str)})
  (println)
  ;;
  (loop [generation 0
         evaluations 0
         population (utils/pmapallv
                     (fn [_] {:plushy (genome/make-random-plushy instructions max-initial-plushy-size)}) 
                     (range population-size) 
                     argmap)
         indexed-training-data (if downsample? (downsample/assign-indices-to-data (downsample/initialize-case-distances argmap) argmap) (:training-data argmap))]
    (let [training-data (if downsample?
                          (case (:ds-function argmap)
                            :case-maxmin (downsample/select-downsample-maxmin indexed-training-data argmap)
                            :case-maxmin-auto (downsample/select-downsample-maxmin-adaptive indexed-training-data argmap)
                            :case-rand (downsample/select-downsample-random indexed-training-data argmap)
                            (do (prn {:error "Invalid Downsample Function"}) (downsample/select-downsample-random indexed-training-data argmap)))
                          indexed-training-data) ;defaults to full training set
          parent-reps (if
                       (and downsample? ; if we are down-sampling
                            (zero? (mod generation ds-parent-gens))) ;every ds-parent-gens generations
                        (take (* ds-parent-rate (count population)) (shuffle population))
                        '()) ;else just empty list
          ; parent representatives for down-sampling
          rep-evaluated-pop (if downsample?
                              (sort-by :total-error
                                       (utils/pmapallv
                                        (partial error-function argmap indexed-training-data)
                                        parent-reps
                                        argmap))
                              '())
          evaluated-pop (sort-by :total-error
                                 (utils/pmapallv
                                  (partial error-function argmap training-data)
                                  population
                                  argmap))
          best-individual (first evaluated-pop)
          best-individual-passes-ds (and downsample? (<= (:total-error best-individual) solution-error-threshold))
          argmap (if (= (:parent-selection argmap) :epsilon-lexicase)
                   (assoc argmap :epsilons (selection/epsilon-list evaluated-pop))
                   argmap)]   ; epsilons
      (if (:custom-report argmap)
        ((:custom-report argmap) evaluations evaluated-pop generation argmap)
        (report evaluations evaluated-pop generation argmap training-data))
      ;;did the indvidual pass all cases in ds?
      (when best-individual-passes-ds
        (prn {:semi-success-generation generation}))
      (cond
        ;; If either the best individual on the ds passes all training cases, or best individual on full sample passes all training cases
        ;; We verify success on test cases and end evolution
        (if (or (and best-individual-passes-ds (<= (:total-error (error-function argmap indexed-training-data best-individual)) solution-error-threshold))
                (and (not downsample?)
                     (<= (:total-error best-individual) solution-error-threshold)))
          (do (prn {:success-generation generation})
              (prn {:total-test-error
                    (:total-error (error-function argmap (:testing-data argmap) best-individual))})
              (when (:simplification? argmap)
                (let [simplified-plushy (simplification/auto-simplify-plushy (:plushy best-individual) error-function argmap)]
                  (prn {:total-test-error-simplified (:total-error (error-function argmap (:testing-data argmap) (hash-map :plushy simplified-plushy)))})))
              (if dont-end false true))
          false)
        (cleanup)
        ;;
        (and (not downsample?) (>= generation max-generations))
        (cleanup)
        ;;
        (and downsample? (>= evaluations (* max-generations population-size (count indexed-training-data))))
        (cleanup)
        ;;
        :else (recur (inc generation)
                     (+ evaluations (* population-size (count training-data)) ;every member evaluated on the current sample
                        (if (zero? (mod generation ds-parent-gens)) (* (count parent-reps) (- (count indexed-training-data) (count training-data))) 0) ; the parent-reps not evaluted already on down-sample
                        (if best-individual-passes-ds (- (count indexed-training-data) (count training-data)) 0)) ; if we checked for generalization or not  
                     (if (:elitism argmap)
                          (conj (utils/pmapallv (fn [_] (variation/new-individual evaluated-pop argmap))
                                                (range (dec population-size))
                                                argmap)
                                (first evaluated-pop))         ;elitism maintains the most-fit individual
                          (utils/pmapallv (fn [_] (variation/new-individual evaluated-pop argmap))
                                          (range population-size)
                                          argmap))
                     (if downsample?
                       (if (zero? (mod generation ds-parent-gens))
                         (downsample/update-case-distances rep-evaluated-pop indexed-training-data indexed-training-data ids-type (/ solution-error-threshold (count indexed-training-data))) ; update distances every ds-parent-gens generations
                         indexed-training-data)
                       indexed-training-data))))))

(defn gp
  "Top-level gp function. Calls gp-loop with possibly-adjusted arguments."
  [argmap]
  (let [adjust-for-autoconstructive-crossover
        (fn [args]
          (let [prob (:autoconstructive-crossover (:variation args))
                n (:autoconstructive-crossover-enrichment args)]
            (if (and prob (> prob 0))
              (update args :instructions concat (repeat (or n 1) :gene))
              args)))
        ;
        adjust-for-ah-umad
        (fn [args]
          (let [prob (:ah-umad (:variation args))
                n (:ah-umad-enrichment args)]
            (if (and prob (> prob 0))
              (update args :instructions concat (flatten (repeat (or n 1) [:vary :protect])))
              args)))]
    (gp-loop (-> argmap
                 (adjust-for-autoconstructive-crossover)
                 (adjust-for-ah-umad)))))