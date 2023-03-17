(ns propeller.gp
  "Main genetic programming loop."
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
            [propeller.push.instructions.vector]
            [propeller.selection :as selection]))

(defn report
  "Reports information each generation."
  [evaluations pop generation argmap training-data]
  (let [best (first pop)]
    (clojure.pprint/pprint {:generation            generation
                            :best-plushy           (:plushy best)
                            :best-program          (genome/plushy->push (:plushy best) argmap)
                            :best-total-error      (:total-error best)
                            :evaluations           evaluations
                            :ds-indices            (map #(:index %) training-data)
                            :best-errors           (:errors best)
                            :best-behaviors        (:behaviors best)
                            :genotypic-diversity   (float (/ (count (distinct (map :plushy pop))) (count pop)))
                            :behavioral-diversity  (float (/ (count (distinct (map :behaviors pop))) (count pop)))
                            :average-genome-length (float (/ (reduce + (map count (map :plushy pop))) (count pop)))
                            :average-total-error   (float (/ (reduce + (map :total-error pop)) (count pop)))})
    (println)))

(defn gp
  "Main GP loop.

On each iteration, it creates a population of random plushies using a mapper
function and genome/make-random-plushy function,
then it sorts the population by the total error using the error-function
and sort-by function. It then takes the best individual from the sorted population,
and if the parent selection is set to epsilon-lexicase, it adds the epsilons to the argmap.

The function then checks if the custom-report argument is set,
if so it calls that function passing the evaluated population,
current generation and argmap. If not, it calls the report function
passing the evaluated population, current generation and argmap.

Then, it checks if the total error of the best individual is less than or equal
to the solution-error-threshold or if the current generation is greater than or
equal to the max-generations specified. If either is true, the function
exits with the best individual or nil. If not, it creates new individuals
for the next generation using the variation/new-individual function and the
repeatedly function, and then continues to the next iteration of the loop. "
  [{:keys [population-size max-generations error-function instructions
           max-initial-plushy-size solution-error-threshold mapper ds-parent-rate ds-parent-gens dont-end ids-type downsample?]
    :or   {solution-error-threshold 0.0
           dont-end false
           ds-parent-rate 0
           ds-parent-gens 1
           ids-type :solved ; :solved or :elite or :soft
           downsample? false
           ;; The `mapper` will perform a `map`-like operation to apply a function to every individual
           ;; in the population. The default is `map` but other options include `mapv`, or `pmap`.
           mapper #?(:clj pmap :cljs map)}
    :as   argmap}]
  ;;
  (prn {:starting-args (update (update argmap :error-function str) :instructions str)})
  (println)
  ;;
  (loop [generation 0
         evaluations 0
         population (mapper
                     (fn [_] {:plushy (genome/make-random-plushy instructions max-initial-plushy-size)})
                     (range population-size))
         indexed-training-data (downsample/assign-indices-to-data (downsample/initialize-case-distances argmap))]
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
          rep-evaluated-pop (if downsample? 
                              (sort-by :total-error
                                     (mapper
                                      (partial error-function argmap indexed-training-data)
                                      parent-reps))
                              '())
          evaluated-pop (sort-by :total-error
                                    (mapper
                                     (partial error-function argmap training-data)
                                     population))
          best-individual (first evaluated-pop)
          best-individual-passes-ds (and downsample? (<= (:total-error best-individual) solution-error-threshold))
          argmap (if (= (:parent-selection argmap) :epsilon-lexicase)
                   (assoc argmap :epsilons (selection/epsilon-list evaluated-pop))
                   argmap)]                                 ;adds :epsilons if using epsilon-lexicase
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
        nil
        ;;
        (and (not downsample?) (>= generation max-generations))
        nil
        ;;
        (and downsample? (>= evaluations (* max-generations population-size (count indexed-training-data))))
        nil
        ;;
        :else (recur (inc generation)
                     (+ evaluations (* population-size (count training-data)) ;every member evaluated on the current sample
                        (if (zero? (mod generation ds-parent-gens)) (* (count parent-reps) (- (count indexed-training-data) (count training-data))) 0) ; the parent-reps not evaluted already on down-sample
                        (if best-individual-passes-ds (- (count indexed-training-data) (count training-data)) 0)) ; if we checked for generalization or not
                     (let [reindexed-pop (hyperselection/reindex-pop evaluated-pop)] ; give every individual an index for hyperselection loggin
                       (hyperselection/log-hyperselection-and-ret
                        (if (:elitism argmap)
                         (conj (repeatedly (dec population-size) #(variation/new-individual reindexed-pop argmap))
                                                                          (first reindexed-pop))
                         (repeatedly population-size ;need to count occurance of each parent, and reset IDs
                                                                                #(variation/new-individual reindexed-pop argmap)))))
                     (if downsample?
                      (if (zero? (mod generation ds-parent-gens))
                        (downsample/update-case-distances rep-evaluated-pop indexed-training-data indexed-training-data ids-type (/ solution-error-threshold (count indexed-training-data))) ; update distances every ds-parent-gens generations
                        indexed-training-data)
                       indexed-training-data))))))
