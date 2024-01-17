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
     (merge
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
       :average-total-error   (float (/ (reduce + (map :total-error pop)) (count pop)))}
      (if (:bmx? argmap)
        {:best-gene-count     (utils/count-genes (:plushy best))
         :average-gene-count  (float (/ (reduce + (map utils/count-genes (map :plushy pop)))
                                        (count pop)))}
        {})))
    (flush)))

(defn cleanup
  []
  #?(:clj (shutdown-agents))
  (prn {:run-completed true})
  nil)

(defn fill-defaults
  "Returns argmap with any unspecified values filled with defaults."
  [argmap]
  (let [defaults
        {:alignment-deviation         2 ; for alternation, the standard deviation of deviation of index when alternating
         :alternation-rate            0.1 ; for alternation, the probability of switching parents at each location
         :bmx-exchange-rate           0.5 ; for bmx, the rate at which genes will be exchanged
         :bmx-gene-length-limit       10  ; for bmx, the maximum length of a gene
         :bmx-gap-change-probability  0.001 ; for bmx, the mutation rate for gaps
         :bmx-complementary?          false ; for bmx, whether mates selected using reverse case sequence of first parent
         :bmx-maximum-distance        1000000 ; for bmx, don't exchange if distance is greater than this
         :bmx-same-gene-count         false ; for bmx, only allow exchanges between individuals with same number of genes
         :closes                      :specified ; :specified, :balanced, :none
         :custom-report               false ; if provided, should be a function that takes an argmap
         :dont-end                    false ; if true, keep running until limit regardless of success
         :downsample?                 true ; wether to use downsampling
         :ds-function                 :case-maxmin ; :case-rand, case-maxmin, case-maxmin-auto
         :downsample-rate             0.05 ; proportion of data used in downsample
         :ds-parent-rate              0.01 ; proportion of parents used to evaluate case distances
         :ds-parent-gens              10 ; generations between computation of parent distances
         :elitism                     false ; whether always to add the lowest-error individual to the next generation
         :error-function              (fn [& args] (println "ERROR FUNCTION NOT PROVIDED")) ; must provide
         :ids-type                    :solved ; type of informed downsampling, :solved or :elite or :soft
         :instructions                ["INSTRUCTIONS NOT PROVIDED"] ; must be provided
         :max-batch-size              10 ; for motley-batch-lexicase-selection, the max size of a batch of cases
         :max-initial-plushy-size     100 ; the maximum size of genomes in initial population
         :max-generations             1000 ; generation limi when downsampling is not used, adjusted by downsampling
         :parent-selection            :lexicase ; see options in variation.cljc
         :population-size             1000 ; the size of the GP ppopulation
         :replacement-rate            0.1 ; for uniform-replacement, the rate at with items will be replaced
         :simplification?             false ; whether to auto-simplify solutions
         :simplification-k            4 ; when auto-simplifying, max number of items deleted in single step
         :simplification-steps        1000 ; when auto-simplifying, number of simplification steps to perform
         :simplification-verbose?     false ; when auto-simplifying, whether to print a lot of information
         :single-thread-mode          false ; if true, don't use multithreading
         :solution-error-threshold    0 ; maximum total error for solutions
         :ssx-not-bmx                 false ; for bmx, swap with segment with same sequence index, not by best match
         :step-limit                  1000 ; limit of Push interpreter steps in a Push program evaluation 
         :testing-data                [] ; must be provided unless there is no testing data
         :tournament-size             5 ; for torunament selection, the number of individuals in each tournament
         :training-data               [] ; must be provided
         :umad-rate                   0.1 ; addition rate (from which deletion rate will be derived) for UMAD
         :variation                   {:umad 1} ; genetic operators and probabilities for their use, which should sum to 1
         }
        defaulted (merge defaults argmap)]
    (merge defaulted ; use the map below to include derived values in argmap
           {:bmx? (some #{:bmx :bmx-umad} (keys (:variation defaulted)))})))

(defn gp
  "Main GP function"
  [non-default-argmap]
  (let [argmap (fill-defaults non-default-argmap)
        {:keys [population-size max-generations error-function solution-error-threshold dont-end
                downsample? ds-parent-rate ds-parent-gens ids-type]} argmap]
  ;; print starting args
    (prn {:starting-args (update (update argmap :error-function str)
                                 :instructions
                                 (fn [instrs]
                                   (utils/not-lazy (map #(if (fn? %) (str %) %) instrs))))})
    (println)
  ;;
    (loop [generation 0
           evaluations 0
           population (utils/pmapallv
                       (fn [_] {:plushy (genome/make-random-plushy argmap)})
                       (range population-size)
                       argmap)
           indexed-training-data (if downsample?
                                   (downsample/assign-indices-to-data (downsample/initialize-case-distances argmap) argmap)
                                   (:training-data argmap))]
      (let [training-data (if downsample?
                            (case (:ds-function argmap)
                              :case-maxmin (downsample/select-downsample-maxmin indexed-training-data argmap)
                              :case-maxmin-auto (downsample/select-downsample-maxmin-adaptive indexed-training-data argmap)
                              :case-rand (downsample/select-downsample-random indexed-training-data argmap)
                              (do (prn {:error "Invalid Downsample Function"})
                                  (downsample/select-downsample-random indexed-training-data argmap)))
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
      ;; Did the indvidual pass all cases in ds?
        (when best-individual-passes-ds
          (prn {:semi-success-generation generation}))
        (cond
        ;; If either the best individual on the ds passes all training cases, or best individual on full 
        ;; sample passes all training cases, we verify success on test cases and exit, succeeding
          (if (or (and best-individual-passes-ds
                       (<= (:total-error (error-function argmap indexed-training-data best-individual))
                           solution-error-threshold))
                  (and (not downsample?)
                       (<= (:total-error best-individual)
                           solution-error-threshold)))
            (do (prn {:success-generation generation})
                (prn {:successful-plushy (:plushy best-individual)})
                (prn {:successful-program (genome/plushy->push (:plushy best-individual) argmap)})
                (prn {:total-test-error
                      (:total-error (error-function argmap (:testing-data argmap) best-individual))})
                (when (:simplification? argmap)
                  (let [simplified-plushy (simplification/auto-simplify-plushy (:plushy best-individual) error-function argmap)]
                    (prn {:total-test-error-simplified
                          (:total-error (error-function argmap (:testing-data argmap) {:plushy simplified-plushy}))})
                    (prn {:simplified-plushy simplified-plushy})
                    (prn {:simplified-program (genome/plushy->push simplified-plushy argmap)})))
                (if dont-end false true))
            false)
          (cleanup)
        ;; If we've evolved for as many generations as the parameters allow, exit without succeeding
          (or (and (not downsample?)
                   (>= generation max-generations))
              (and downsample?
                   (>= evaluations (* max-generations population-size (count indexed-training-data)))))
          (cleanup)
        ;; Otherwise, evolve for another generation
          :else (recur (inc generation)
                       (+ evaluations
                          (* population-size (count training-data)) ;every member evaluated on the current sample
                          (if (zero? (mod generation ds-parent-gens))
                            (* (count parent-reps)
                               (- (count indexed-training-data)
                                  (count training-data)))
                            0) ; the parent-reps not evaluted already on down-sample
                          (if best-individual-passes-ds
                            (- (count indexed-training-data) (count training-data))
                            0)) ; if we checked for generalization or not  
                       (if (:elitism argmap) ; elitism maintains the individual with lowest total error
                         (conj (utils/pmapallv (fn [_] (variation/new-individual evaluated-pop argmap))
                                               (range (dec population-size))
                                               argmap)
                               (first evaluated-pop))
                         (utils/pmapallv (fn [_] (variation/new-individual evaluated-pop argmap))
                                         (range population-size)
                                         argmap))
                       (if downsample?
                         (if (zero? (mod generation ds-parent-gens))
                         ; update distances every ds-parent-gens generations
                           (downsample/update-case-distances rep-evaluated-pop
                                                             indexed-training-data
                                                             indexed-training-data
                                                             ids-type
                                                             (/ solution-error-threshold
                                                                (count indexed-training-data)))
                           indexed-training-data)
                         indexed-training-data)))))))
