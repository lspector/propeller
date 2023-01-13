(ns propeller.gp
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
            [propeller.selection :as selection]))

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
                      (range population-size))]             ;creates population of random plushys
    (let [evaluated-pop (sort-by :total-error
                                 (mapper
                                   (partial error-function argmap (:training-data argmap))
                                   population))             ;population sorted by :total-error
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
              (let [simplified-plushy (simplification/auto-simplify-plushy (:plushy best-individual) error-function argmap)]
                (prn {:total-test-error-simplified (:total-error (error-function argmap (:testing-data argmap) (hash-map :plushy simplified-plushy)))}))))
        ;;
        (>= generation max-generations)
        nil
        ;;
        :else (recur (inc generation)
                     (if (:elitism argmap)
                       (conj (repeatedly (dec population-size)
                                         #(variation/new-individual evaluated-pop argmap))
                             (first evaluated-pop))         ;elitism maintains the most-fit individual
                       (repeatedly population-size
                                   #(variation/new-individual evaluated-pop argmap))))))))

; This code is defining a function called "gp" (short for genetic programming) that takes in a map of parameters as its argument.
; The map contains keys such as "population-size", "max-generations", "error-function", "instructions", "max-initial-plushy-size",
; "solution-error-threshold", and "mapper", with default values specified for some of them.
;
;The function starts by printing some information about the starting arguments using
; the prn function and then uses a loop to iterate over generations.
; On each iteration, it creates a population of random plushies using a mapper
; function and genome/make-random-plushy function,
; then it sorts the population by the total error using the error-function
; and sort-by function. It then takes the best individual from the sorted population,
; and if the parent selection is set to epsilon-lexicase, it adds the epsilons to the argmap.
;
;The function then checks if the custom-report argument is set,
; if so it calls that function passing the evaluated population,
; current generation and argmap. If not, it calls the report function
; passing the evaluated population, current generation and argmap.
;
;Then, it checks if the total error of the best individual is less than or equal
; to the solution-error-threshold or if the current generation is greater than or
; equal to the max-generations specified. If either is true, the function
; exits with the best individual or nil. If not, it creates new individuals
; for the next generation using the variation/new-individual function and the
; repeatedly function, and then continues to the next iteration of the loop.