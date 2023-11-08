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
            [propeller.utils :as utils]))

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
  [plushy instructions umad-rate]
  (apply concat
         (map #(if (< (rand) umad-rate)
                 (shuffle [% (utils/random-instruction instructions)])
                 [%])
              plushy)))

(defn uniform-replacement
  "Returns plushy with new instructions possibly replacing existing
   instructions."
  [plushy instructions replacement-rate]
  (map #(if (< (rand) replacement-rate)
          (utils/random-instruction instructions)
          %)
       plushy))

(defn uniform-deletion
  "Randomly deletes instructions from plushy at some rate."
  [plushy umad-rate]
  (if (zero? umad-rate)
    plushy
    (remove (fn [_] (< (rand)
                       (/ 1 (+ 1 (/ 1 umad-rate)))))
            plushy)))

(defn ah-normalize
  "A utility for autoconstructive hypervariability mutation. 
   Takes a vector of :protect and :vary and returns a numeric vector
   that conforms to the specified min, max, and mean."
  [v ah-min ah-max ah-mean]
  (let [c (count v)
        protect-count (count (filter #(= % :protected) v))
        vary-count (- c protect-count)
        extremes (mapv #(if (= % :protect) ah-min ah-max) v)
        mean-of-extremes (/ (reduce + extremes) (count extremes))]
    (cond
      ;; all :vary or all :protect, return all ah-mean
      (or (zero? protect-count) (zero? vary-count))
      (repeat (count v) ah-mean)
      ;; mean is too high, lower high values from max
      (> mean-of-extremes ah-mean)
      (let [lowered (/ (- (* ah-mean c)
                          (* ah-min protect-count))
                       vary-count)]
        (mapv #(if (= % ah-max) lowered %) extremes))
      ;; mean is too low, raise low values from min
      (> mean-of-extremes ah-mean)
      (let [raised (/ (- (* ah-mean c)
                         (* ah-max vary-count))
                      protect-count)]
        (mapv #(if (= % ah-min) raised %) extremes))
      ;; mean is just right, return extremes
      :else
      extremes)))

(defn ah-rates
  "A utility for autoconstructive hypervariability mutation. 
   Returns the sequence of rates with which each element of plushy should
   be mutated when using autoconstructive hypervariability."
  [plushy ah-min ah-max ah-mean]
  (loop [i 0
         protected true
         rates []
         remainder plushy]
    (if (empty? remainder)
      (ah-normalize rates ah-min ah-max ah-mean)
      (if (and (not protected)
               (= (first remainder) :protect))
        (recur (inc i)
               true
               (conj rates 1)
               (rest remainder))
        (recur (inc i)
               (if protected
                 (not= (first remainder) :vary)
                 false)
               (conj rates (if protected :protect :vary))
               (rest remainder))))))

(defn ah-uniform-addition
  "Returns plushy with new instructions possibly added before or after each
  existing instruction. Rates are autoconstructively hypervariable."
  [plushy instructions ah-min ah-max ah-mean]
  (apply concat
         (mapv #(if (< (rand) %2)
                  (shuffle [%1 (utils/random-instruction instructions)])
                  [%1])
               plushy
               (ah-rates plushy ah-min ah-max ah-mean))))

(defn ah-uniform-deletion
  "Randomly deletes instructions from plushy at some rate.
   Rates are autoconstructively hypervariable."
  [plushy ah-min ah-max ah-mean]
  (mapv first
        (remove (fn [[_ rate]]
                  (< (rand)
                     (if (zero? rate)
                       0
                       (/ 1 (+ 1 (/ 1 rate))))))
                (mapv vector
                      plushy
                      (ah-rates plushy ah-min ah-max ah-mean)))))

(defn count-genes
  "A utility for autoconstructive crossover. Returns the number of segments 
   between (and before and after) instances of :gene."
  [plushy]
  (inc (count (filter #(= % :gene) plushy))))

(defn extract-genes
  "A utility for autoconstructive crossover. Returns the segments of the plushy
   before/between/after instances of :gene."
  [plushy]
  (loop [genes []
         current-gene []
         remainder plushy]
    (cond (empty? remainder)
          (conj genes current-gene)
          ;
          (= (first remainder) :gene)
          (recur (conj genes current-gene)
                 []
                 (rest remainder))
          ;
          :else
          (recur genes
                 (conj current-gene (first remainder))
                 (rest remainder)))))

(defn autoconstructive-crossover
  "Crosses over two plushies using autoconstructive crossover, one Push instruction at a time.
   Assumes the plushies have the same number of genes."
  [plushy-a plushy-b]
  (let [a-genes (extract-genes plushy-a)
        b-genes (extract-genes plushy-b)]
    (flatten (interpose :gene
                        (mapv #(if (< (rand) 0.5) %1 %2)
                              a-genes
                              b-genes)))))

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
         :autoconstructive-crossover
         (let [plushy1 (:plushy (selection/select-parent pop argmap))
               p1-genes  (count-genes plushy1)
               plushy2 (:plushy (selection/select-parent
                                 (filter #(= (count-genes (:plushy %))
                                             p1-genes)
                                         pop)
                                 argmap))]
           (autoconstructive-crossover plushy1 plushy2))
       ;
         :umad ;; uniform mutation by addition and deleted, see uniform-deletion for the
               ;; adjustment that makes this size neutral on average
         (let [rate (utils/onenum (:umad-rate argmap))]
           (-> (:plushy (selection/select-parent pop argmap))
               (uniform-addition (:instructions argmap) rate)
               (uniform-deletion rate)))
       ;
         :rumad ;; responsive UMAD, uses a deletion rate computed from the actual
                ;; number of additions made
         (let [parent-genome (:plushy (selection/select-parent pop argmap))
               after-addition (uniform-addition parent-genome
                                                (:instructions argmap)
                                                (utils/onenum (:umad-rate argmap)))
               effective-addition-rate (/ (- (count after-addition)
                                             (count parent-genome))
                                          (count parent-genome))]
           (uniform-deletion after-addition effective-addition-rate))
       ;
         :vumad ;; variable umad: :umad-rate is interpreted as the max, and the 
                ;; actual rate is chosen uniformly from the range [0, max)
         (let [rate (rand (utils/onenum (:umad-rate argmap)))]
           (-> (:plushy (selection/select-parent pop argmap))
               (uniform-addition (:instructions argmap) rate)
               (uniform-deletion rate)))
       ;
         :ah-umad ;; autoconstructive hypervariability UMAD
         (let [ah-min (utils/onenum (:ah-umad-min argmap))
               ah-max (utils/onenum (:ah-umad-max argmap))
               ah-mean (utils/onenum (:ah-umad-mean argmap))
               parent-genome (:plushy (selection/select-parent pop argmap))]
           (-> parent-genome
               (ah-uniform-addition (:instructions argmap) ah-min ah-max ah-mean)
               (ah-uniform-deletion ah-min ah-max ah-mean)))
       ;
         :uniform-addition
         (-> (:plushy (selection/select-parent pop argmap))
             (uniform-addition (:instructions argmap)
                               (utils/onenum (:umad-rate argmap))))
       ;
         :uniform-replacement
         (-> (:plushy (selection/select-parent pop argmap))
             (uniform-replacement (:instructions argmap)
                                  (utils/onenum (:replacement-rate argmap))))
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
