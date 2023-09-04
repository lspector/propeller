(ns propeller.gp
  "Main genetic programming loop."
  (:require [clojure.string]
            [clojure.pprint]
            [propeller.genome :as genome]
            [propeller.simplification :as simplification]
            [propeller.variation :as variation]
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
  "Reports information for each generation."
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
           max-initial-plushy-size solution-error-threshold]
    :or   {solution-error-threshold 0.0}
    :as   argmap}]
  ;;
  (prn {:starting-args (update (update argmap :error-function str) :instructions str)})
  (println)
  ;;
  (loop [generation 0
         population (utils/pmapallv
                     (fn [_] {:plushy (let [plushy  (genome/make-random-plushy instructions max-initial-plushy-size)]
                                        (if (:diploid argmap)
                                          (interleave plushy plushy)
                                          plushy))})
                     (range population-size)
                     argmap)]             ;creates population of random plushys
    (let [evaluated-pop (sort-by :total-error
                                 (utils/pmapallv
                                  (partial error-function argmap (:training-data argmap))
                                  population ;population sorted by :total-error
                                  argmap))         
          best-individual (first evaluated-pop)
          argmap (if (= (:parent-selection argmap) :epsilon-lexicase)
                   (assoc argmap :epsilons (selection/epsilon-list evaluated-pop))
                   argmap)]                                 ;adds :epsilons if using epsilon-lexicase
      (if (:custom-report argmap)
        ((:custom-report argmap) evaluated-pop generation argmap)
        (report evaluated-pop generation argmap))
      (cond
        ;; Success on training cases is verified on testing cases
        (<= (:total-error best-individual) solution-error-threshold)
        (do (prn {:success-generation generation})
            (prn {:total-test-error
                  (:total-error (error-function argmap (:testing-data argmap) best-individual))})
            (when (:simplification? argmap)
              (let [simplified-plushy (simplification/auto-simplify-plushy 
                                       (:plushy best-individual) 
                                       error-function argmap)]
                (prn {:total-test-error-simplified
                      (:total-error (error-function argmap
                                                    (:testing-data argmap)
                                                    (hash-map :plushy simplified-plushy)))})))
            #?(:clj (shutdown-agents)))
        ;;
        (>= generation max-generations)
        (do #?(:clj (shutdown-agents)))
        ;;
        :else (recur (inc generation)
                     (if (:elitism argmap)
                       (conj (utils/pmapallv (fn [_] (variation/new-individual evaluated-pop argmap))
                                             (range (dec population-size))
                                             argmap)
                             (first evaluated-pop))         ;elitism maintains the most-fit individual
                       (utils/pmapallv (fn [_] (variation/new-individual evaluated-pop argmap))
                                       (range population-size)
                                       argmap)))))))

