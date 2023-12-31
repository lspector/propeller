(ns propeller.variation
  "Propeller includes many kinds of genetic operators to create variation within the population.
  You can specify the rate of the variation genetic operators with the `:variation` map.

# Variation

Propeller includes many kinds of genetic operators to create variation within the population.
You can specify the rate of the variation genetic operators with the `:variation` map.
   
***Some*** of the available genetic operators are described in this documentation. See the code
for others and for details.
   
To add a new genetic operator you must add a case for the operator's keyword in new-individual,
calling existing or new utility functions that should be included earlier in this file.

## Crossover

Crossover genetic operators take two `plushy` representations of Push programs
and exchange genetic material to create a new `plushy`.

| Function                         | Parameters             | Description                                                                                                                                            |
|----------------------------------|------------------------|--------------------------------------------------------------------------------------------------------------------------------------------------------|
| `crossover`                      | `plushy-a` `plushy-b`  | Crosses over two individuals using uniform crossover, one Push instruction at a time. Pads shorter one from the end of the list of instructions.       |
| `tail-aligned-crossover`         | `plushy-a` `plushy-b`  | Crosses over two individuals using uniform crossover, one Push instruction at a time. Pads shorter one from the beginning of the list of instructions. |

## Addition, Deletion, Replacement, Flip

Addition, deletion, replacement, and flip genetic operators take a `plushy` and a rate of occurrence to create a new `plushy`.

| Function                                  | Parameters                                 | Description                                                                                                                     |
|-------------------------------------------|--------------------------------------------|---------------------------------------------------------------------------------------------------------------------------------|
| `uniform-addition`                        | `plushy` `instructions` `umad-rate`        | Returns a plushy with new instructions possibly added before or after each existing instruction.                                |
| `uniform-replacement`                     | `plushy` `instructions` `replacement-rate` | Returns a plushy with new instructions possibly replacing existing instructions.                                                |
| `uniform-deletion`                        | `plushy` `umad-rate`                       | Randomly deletes instructions from plushy at some rate.                                                                         |

## Uniform Mutation by Addition and Deletion

Uniform Mutation by Addition and Deletion (UMAD) is a uniform mutation operator which
first adds genes with some probability before or after every existing gene and then
deletes random genes from the resulting genome. [It has been found](http://cs.hamilton.edu/~thelmuth/Pubs/2018-GECCO-UMAD.pdf) that UMAD, with relatively
high rates of addition and deletion, results in significant increases
in problem-solving performance on a range of program synthesis
benchmark problems. When you run a problem in Propeller, you can specify the `umad-rate` to determine the frequency
of addition and deletion.

`:umad` in the `:variation` map when running a problem will call `uniform-addition` and `uniform-deletion` with the `umad-rate`.
Since `uniform-addition` and `uniform-deletion` are somewhat stochastic, you can use
`:rumad` to ensure that the actual rates of addition and deletion are equal when mutating a genome.

## New Individual

The function `new-individual` returns a new individual produced by selection and variation of individuals in the population based on the genetic operators provided in the `:variation` map."
  {:doc/format :markdown}
  (:require [propeller.selection :as selection]
            [propeller.utils :as utils]
            [propeller.tools.metrics :as metrics]
            [propeller.tools.math :as math]))

(defn crossover
  "Crosses over two individuals using uniform crossover, one Push instruction at a time.
   Pads shorter one from the end of the list of instructions."
  [plushy-a plushy-b]
  (let [shorter (min-key count plushy-a plushy-b)
        longer (if (= shorter plushy-a)
                 plushy-b
                 plushy-a)
        length-diff (- (count longer) (count shorter))
        shorter-padded (concat shorter (repeat length-diff :crossover-padding))]
    (remove #(= % :crossover-padding)
            (map #(if (< (rand) 0.5) %1 %2)
                 shorter-padded
                 longer))))

(defn alternation
  "Alternates between the two parent genomes."
  [genome1 genome2 alternation-rate alignment-deviation]
  (loop [i 0
         use-genome1 (rand-nth [true false])
         result-genome []
         iteration-budget (+ (count genome1) (count genome2))]
    (if (or (>= i (count (if use-genome1 genome1 genome2))) ;; finished current program
            (<= iteration-budget 0)) ;; looping too long
      result-genome ;; Return
      (if (< (rand) alternation-rate)
        (recur (max 0 (+ i (utils/round (* alignment-deviation
                                           (utils/gaussian-noise-factor)))))
               (not use-genome1)
               result-genome
               (dec iteration-budget))
        (recur (inc i)
               use-genome1
               (conj result-genome (nth (if use-genome1 genome1 genome2) i))
               (dec iteration-budget))))))

(defn tail-aligned-crossover
  "Crosses over two individuals using uniform crossover, one Push instruction at a time.
   Pads shorter one from the beginning of the list of instructions."
  [plushy-a plushy-b]
  (let [shorter (min-key count plushy-a plushy-b)
        longer (if (= shorter plushy-a)
                 plushy-b
                 plushy-a)
        length-diff (- (count longer) (count shorter))
        shorter-padded (concat (repeat length-diff :crossover-padding) shorter)]
    (remove #(= % :crossover-padding)
            (map #(if (< (rand) 0.5) %1 %2)
                 shorter-padded
                 longer))))

(defn uniform-addition
  "Returns plushy with new instructions possibly added before or after each
  existing instruction."
  [plushy instructions umad-rate argmap]
  (apply concat
         (map #(if (and (not= % :gap)
                        (< (rand) umad-rate))
                 (shuffle [% (utils/random-instruction instructions argmap)])
                 [%])
              plushy)))

(defn uniform-replacement
  "Returns plushy with new instructions possibly replacing existing
   instructions."
  [plushy instructions replacement-rate argmap]
  (map #(if (< (rand) replacement-rate)
          (utils/random-instruction instructions argmap)
          %)
       plushy))

(defn uniform-deletion
  "Randomly deletes instructions from plushy at a rate that is adjusted
   relative to the rate used for addition."
  [plushy umad-rate]
  (if (zero? umad-rate)
    plushy
    (let [adjusted-rate (/ 1 (+ 1 (/ 1 umad-rate)))]
      (remove (fn [item]
                (and (not= item :gap)
                     (< (rand) adjusted-rate)))
              plushy))))

(defn uniform-gap-addition
  "Returns plushy with new instances of :gap possibly added within each gene,
   each of which is a subsequence of the plushy."
  [plushy gap-change-prob]
  (if (zero? gap-change-prob)
    plushy
    (flatten (interpose :gap
                        (apply concat
                               (mapv (fn [gene]
                                       (if (< (rand) gap-change-prob)
                                         (let [n (rand-int (inc (count gene)))]
                                           [(take n gene) (drop n gene)])
                                         [gene]))
                                     (utils/extract-genes plushy)))))))

(defn uniform-gap-deletion
  "Randomly deletes instances of :gap from plushy at a rate that is adjusted
   relative to the rate used for gap addition."
  [plushy gap-change-prob]
  (if (zero? gap-change-prob)
    plushy
    (let [adjusted-rate (/ 1 (+ 1 (/ 1 gap-change-prob)))]
      (remove (fn [item]
                (and (= item :gap)
                     (< (rand) adjusted-rate)))
              plushy))))

(defn bmx
  "Crosses over two plushies using best match crossover (bmx)."
  [plushy-a plushy-b rate max-distance argmap]
  (let [a-genes (utils/extract-genes plushy-a)
        b-genes (utils/extract-genes plushy-b)]
    (if (:ssx-not-bmx argmap)
      (flatten (interpose :gap 
                          (mapv (fn [a-gene b-gene]
                                  (if (and b-gene
                                           (< (rand) rate))
                                    b-gene
                                    a-gene))
                                a-genes
                                (concat b-genes (repeat false)))))
      (flatten
       (interpose :gap
                  (mapv (fn [a-gene]
                          (if (< (rand) rate)
                            (let [match-info (map (fn [b-gene]
                                                    {:distance (utils/bmx-distance a-gene b-gene)
                                                     :gene b-gene})
                                                  b-genes)
                                  candidates (filter (fn [info]
                                                       (<= (:distance info) max-distance))
                                                     match-info)]
                              (if (empty? candidates)
                                a-gene
                                (:gene (apply min-key :distance candidates))))
                            a-gene))
                        a-genes))))))

(defn new-individual
  "Returns a new individual produced by selection and variation of
  individuals in the population."
  [pop argmap]
  {:plushy
   (let [r (rand)
         op (loop [accum 0.0
                   ops-probs (vec (:variation argmap))]
              (if (empty? ops-probs)
                :reproduction
                (let [[op1 prob1] (first ops-probs)]
                  (if (>= (+ accum prob1) r)
                    op1
                    (recur (+ accum prob1)
                           (rest ops-probs))))))]
     (case op
       :crossover
       (crossover
        (:plushy (selection/select-parent pop argmap))
        (:plushy (selection/select-parent pop argmap)))
       ;
       :tail-aligned-crossover
       (tail-aligned-crossover
        (:plushy (selection/select-parent pop argmap))
        (:plushy (selection/select-parent pop argmap)))
       ;
       :bmx ;; best match crossover
       (let [parent1 (selection/select-parent pop argmap)
             parent2 (let [pool (if (:bmx-same-gene-count argmap)
                                  (let [n (utils/count-genes (:plushy parent1))]
                                    (filter #(= n (utils/count-genes (:plushy %))) pop))
                                  pop)]
                       (if (:bmx-complementary? argmap)
                         (selection/select-parent
                          pool
                          (assoc argmap
                                 :initial-cases
                                 (reverse (:selection-cases parent1))))
                         (selection/select-parent pool argmap)))
             plushy1 (:plushy parent1)
             plushy2 (:plushy parent2)
             bmx-exchange-rate (utils/onenum (or (:bmx-exchange-rate argmap) 0.5))
             gap-change-prob (utils/onenum (:bmx-gap-change-probability argmap))
             max-distance (utils/onenum (:bmx-maximum-distance argmap))]
         (->  (bmx plushy1 plushy2 bmx-exchange-rate max-distance argmap)
              (uniform-gap-addition gap-change-prob)
              (uniform-gap-deletion gap-change-prob)
              (utils/remove-empty-genes)
              (utils/enforce-gene-length-limit (:bmx-gene-length-limit argmap))))
       ;
       :umad ;; uniform mutation by addition and deletion, see uniform-deletion for the
               ;; adjustment that makes this size neutral on average
       (let [rate (utils/onenum (:umad-rate argmap))]
         (-> (:plushy (selection/select-parent pop argmap))
             (uniform-addition (:instructions argmap) rate argmap)
             (uniform-deletion rate)))
       ;
       :bmx-umad ;; applies umad to the results of bmx
       (let [umad-rate (utils/onenum (:umad-rate argmap))
             gap-change-prob (utils/onenum (:bmx-gap-change-probability argmap))]
         (->  (let [parent1 (selection/select-parent pop argmap)
                    parent2 (let [pool (if (:bmx-same-gene-count argmap)
                                         (let [n (utils/count-genes (:plushy parent1))]
                                           (filter #(= n (utils/count-genes (:plushy %))) pop))
                                         pop)]
                              (if (:bmx-complementary? argmap)
                                (selection/select-parent  pool
                                                          (assoc argmap
                                                                 :initial-cases
                                                                 (reverse (:selection-cases parent1))))
                                (selection/select-parent pool argmap)))
                    plushy1 (:plushy parent1)
                    plushy2 (:plushy parent2)
                    bmx-exchange-rate (utils/onenum (or (:bmx-exchange-rate argmap) 0.5))
                    max-distance (utils/onenum (:bmx-maximum-distance argmap))]
                (bmx plushy1 plushy2 bmx-exchange-rate max-distance argmap))
              (uniform-gap-addition gap-change-prob)
              (uniform-gap-deletion gap-change-prob)
              (uniform-addition (:instructions argmap) umad-rate argmap)
              (uniform-deletion umad-rate)
              (utils/remove-empty-genes)
              (utils/enforce-gene-length-limit (:bmx-gene-length-limit argmap))))
       ;
       :rumad ;; responsive UMAD, uses a deletion rate computed from the actual
                ;; number of additions made
       (let [parent-genome (:plushy (selection/select-parent pop argmap))
             after-addition (uniform-addition parent-genome
                                              (:instructions argmap)
                                              (utils/onenum (:umad-rate argmap))
                                              argmap)
             effective-addition-rate (/ (- (count after-addition)
                                           (count parent-genome))
                                        (count parent-genome))]
         (uniform-deletion after-addition effective-addition-rate))
       ;
       :vumad ;; variable umad: :umad-rate is interpreted as the max, and the 
                ;; actual rate is chosen uniformly from the range [0, max)
       (let [rate (rand (utils/onenum (:umad-rate argmap)))]
         (-> (:plushy (selection/select-parent pop argmap))
             (uniform-addition (:instructions argmap) rate argmap)
             (uniform-deletion rate)))
       ;
       :uniform-addition
       (-> (:plushy (selection/select-parent pop argmap))
           (uniform-addition (:instructions argmap)
                             (utils/onenum (:umad-rate argmap))
                             argmap))
       ;
       :uniform-replacement
       (-> (:plushy (selection/select-parent pop argmap))
           (uniform-replacement (:instructions argmap)
                                (utils/onenum (:replacement-rate argmap))
                                argmap))
       ;
       :uniform-deletion
       (-> (:plushy (selection/select-parent pop argmap))
           (uniform-deletion (utils/onenum (:umad-rate argmap))))
       ;
       :alternation
       (alternation (:plushy (selection/select-parent pop argmap))
                    (:plushy (selection/select-parent pop argmap))
                    (utils/onenum (or (:alternation-rate argmap) 0))
                    (utils/onenum (or (:alignment-deviation argmap) 0)))
       ;
       :reproduction
       (:plushy (selection/select-parent pop argmap))
       ;
       :else
       (throw #?(:clj  (Exception. (str "No match in new-individual for " op))
                 :cljs (js/Error
                        (str "No match in new-individual for " op))))))})
