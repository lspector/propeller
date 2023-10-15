(ns propeller.variation
  "Propeller includes many kinds of genetic operators to create variation within the population.
  You can specify the rate of the variation genetic operators with the `:variation` map.

# Variation

Propeller includes many kinds of genetic operators to create variation within the population.
You can specify the rate of the variation genetic operators with the `:variation` map.

## Crossover

Crossover genetic operators take two `plushy` representations of Push programs
and exchange genetic material to create a new `plushy`.

| Function                         | Parameters             | Description                                                                                                                                            |
|----------------------------------|------------------------|--------------------------------------------------------------------------------------------------------------------------------------------------------|
| `crossover`                      | `plushy-a` `plushy-b`  | Crosses over two individuals using uniform crossover, one Push instruction at a time. Pads shorter one from the end of the list of instructions.       |
| `tail-aligned-crossover`         | `plushy-a` `plushy-b`  | Crosses over two individuals using uniform crossover, one Push instruction at a time. Pads shorter one from the beginning of the list of instructions. |
| `diploid-crossover`              | `plushy-a` `plushy-b`  | Crosses over two individuals using uniform crossover with pairs of Push instructions. Pads shorter one from the end of the list of instructions.       |
| `tail-aligned-diploid-crossover` | `plushy-a` `plushy-b`  | Crosses over two individuals using uniform crossover with pairs of Push instructions. Pads shorter one from the beginning of the list of instructions. |

## Addition, Deletion, Replacement, Flip

Addition, deletion, replacement, and flip genetic operators take a `plushy` and a rate of occurrence to create a new `plushy`.

| Function                                  | Parameters                                 | Description                                                                                                                     |
|-------------------------------------------|--------------------------------------------|---------------------------------------------------------------------------------------------------------------------------------|
| `uniform-addition`                        | `plushy` `instructions` `umad-rate`        | Returns a plushy with new instructions possibly added before or after each existing instruction.                                |
| `uniform-replacement`                     | `plushy` `instructions` `replacement-rate` | Returns a plushy with new instructions possibly replacing existing instructions.                                                |
| `diploid-uniform-silent-replacement`      | `plushy` `instructions` `replacement-rate` | Returns a plushy with new instructions possibly replacing existing instructions, but only among the silent member of each pair. |
| `diploid-uniform-addition`                | `plushy` `instructions` `umad-rate`        | Returns a plushy with new instructions possibly added before or after each existing pair of instructions.                       |
| `uniform-deletion`                        | `plushy` `umad-rate`                       | Randomly deletes instructions from plushy at some rate.                                                                         |
| `diploid-uniform-deletion`                | `plushy` `flip-rate`                       | Randomly flips pairs in a diploid plushy at some rate.                                                                          |

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

(defn diploid-crossover
  "Crosses over two individuals using uniform crossover with pairs of Push instructions.
   Pads shorter one from the end of the list of instructions."
  [plushy-a plushy-b]
  (let [plushy-a (partition 2 plushy-a)
        plushy-b (partition 2 plushy-b)
        shorter (min-key count plushy-a plushy-b)
        longer (if (= shorter plushy-a)
                 plushy-b
                 plushy-a)
        length-diff (- (count longer) (count shorter))
        shorter-padded (concat shorter (repeat length-diff :crossover-padding))]
    (flatten (remove #(= % :crossover-padding)
                     (map #(if (< (rand) 0.5) %1 %2)
                          shorter-padded
                          longer)))))

(defn tail-aligned-diploid-crossover
  "Crosses over two individuals using uniform crossover with pairs of Push instructions.
   Pads shorter one from the beginning of the list of instructions."
  [plushy-a plushy-b]
  (let [plushy-a (partition 2 plushy-a)
        plushy-b (partition 2 plushy-b)
        shorter (min-key count plushy-a plushy-b)
        longer (if (= shorter plushy-a)
                 plushy-b
                 plushy-a)
        length-diff (- (count longer) (count shorter))
        shorter-padded (concat (repeat length-diff :crossover-padding) shorter)]
    (flatten (remove #(= % :crossover-padding)
                     (map #(if (< (rand) 0.5) %1 %2)
                          shorter-padded
                          longer)))))

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

(defn diploid-uniform-silent-replacement
  "Returns plushy with new instructions possibly replacing existing
   instructions, but only among the silent member of each pair."
  [plushy instructions replacement-rate]
  (interleave (map first (partition 2 plushy))
              (map #(if (< (rand) replacement-rate)
                      (utils/random-instruction instructions)
                      %)
                   (map second (partition 2 plushy)))))

(defn diploid-uniform-addition
  "Returns plushy with new instructions possibly added before or after each
  existing instruction."
  [plushy instructions umad-rate]
  (flatten
   (map (fn [pair]
          (if (< (rand) umad-rate)
            (shuffle [pair (repeatedly 2 #(utils/random-instruction instructions))])
            [pair]))
        (partition 2 plushy))))

(defn uniform-deletion
  "Randomly deletes instructions from plushy at some rate."
  [plushy umad-rate]
  (if (zero? umad-rate)
    plushy
    (remove (fn [_] (< (rand)
                       (/ 1 (+ 1 (/ 1 umad-rate)))))
            plushy)))

(defn diploid-uniform-deletion
  "Randomly deletes instructions from plushy at some rate."
  [plushy umad-rate]
  (flatten (remove (fn [_] (< (rand)
                              (/ 1 (+ 1 (/ 1 umad-rate)))))
                   (partition 2 plushy))))

(defn diploid-flip
  "Randomly flips pairs in a diploid plushy at some rate."
  [plushy flip-rate]
  (flatten (map #(if (< (rand) flip-rate)
                   (reverse %)
                   %)
                (partition 2 plushy))))

(defn ah-rates
  "Returns the sequence of rates with which each element of plushy should
   be mutated when using autoconstructive hypervariability."
  [plushy protect-rate hypervariable-rate]
  (loop [i 0
         protected true
         rates []
         remainder plushy]
    (if (empty? remainder)
      rates
      (if (and (not protected)
               (= (first remainder) :protect))
        (recur i
               true
               rates
               remainder)
        (recur (inc i)
               (if protected
                 (not= (first remainder) :vary)
                 false)
               (conj rates (if protected protect-rate hypervariable-rate))
               (rest remainder))))))

(defn ah-uniform-addition
  "Returns plushy with new instructions possibly added before or after each
  existing instruction. Rates are autoconstructively hypervariable."
  [plushy instructions protect-rate hypervariable-rate]
  (apply concat
         (map #(if (< (rand) %2)
                 (shuffle [%1 (utils/random-instruction instructions)])
                 [%1])
              plushy
              (ah-rates plushy protect-rate hypervariable-rate))))

(defn ah-uniform-deletion
  "Randomly deletes instructions from plushy at some rate.
   Rates are autoconstructively hypervariable."
  [plushy protect-rate hypervariable-rate]
  (map first
       (remove (fn [[_ rate]]
                 (< (rand)
                    (if (zero? rate)
                      0
                      (/ 1 (+ 1 (/ 1 rate))))))
               (map vector
                    plushy
                    (ah-rates plushy protect-rate hypervariable-rate)))))

(defn new-individual
  "Returns a new individual produced by selection and variation of
  individuals in the population."
  [pop argmap]
  (let [umad-parent (selection/select-parent pop argmap)
        parent-ind (:index umad-parent)] ;this is a hack to log hyperselection, only works for umad
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
       :umad
       (let [rate (utils/onenum (:umad-rate argmap))]
         (-> (:plushy (selection/select-parent pop argmap))
             (uniform-addition (:instructions argmap) rate)
             (uniform-deletion rate)))
       ; uniform mutation by addition and deletion is a uniform mutation operator which
       ;first adds genes with some probability before or after every existing gene and then
       ;deletes random genes from the resulting genome
       ;
       :rumad
       (let [parent-genome (:plushy (selection/select-parent pop argmap))
             after-addition (uniform-addition parent-genome
                                              (:instructions argmap)
                                              (utils/onenum (:umad-rate argmap)))
             effective-addition-rate (/ (- (count after-addition)
                                           (count parent-genome))
                                        (count parent-genome))]
         (uniform-deletion after-addition effective-addition-rate))
       ; Adds and deletes instructions in the parent genome with the same rate
       ;
       :vumad ;; variable umad: :umad-rate is interpreted as max, actual uniform 0-max
       (let [rate (rand (utils/onenum (:umad-rate argmap)))]
         (-> (:plushy (selection/select-parent pop argmap))
             (uniform-addition (:instructions argmap) rate)
             (uniform-deletion rate)))
       ;
       :ah-umad ;; autoconstructive hypervariability UMAD
       (let [protect-rate (utils/onenum (:ah-umad-protect-rate argmap))
             vary-rate (utils/onenum (:ah-umad-vary-rate argmap))
             tourn-size (utils/onenum (:ah-umad-tournament-size argmap))
             parent-genome (:plushy (selection/select-parent pop argmap))
             offspring (repeatedly
                        tourn-size
                        #(-> parent-genome
                             (ah-uniform-addition (:instructions argmap) protect-rate vary-rate)
                             (ah-uniform-deletion protect-rate vary-rate)))
             hypervariabilities (map #(reduce + (ah-rates % 0 1)) offspring)]
         (second (last (sort-by first (map vector hypervariabilities offspring)))))
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
       :diploid-uniform-silent-replacement
       (-> (:plushy (selection/select-parent pop argmap))
           (diploid-uniform-silent-replacement (:instructions argmap)
                                               (utils/onenum (:replacement-rate argmap))))
       ;
       :uniform-deletion
       (-> (:plushy (selection/select-parent pop argmap))
           (uniform-deletion (utils/onenum (:umad-rate argmap))))
       ;
       :diploid-crossover
       (diploid-crossover
        (:plushy (selection/select-parent pop argmap))
        (:plushy (selection/select-parent pop argmap)))
       ;
       :tail-aligned-diploid-crossover
       (tail-aligned-diploid-crossover
        (:plushy (selection/select-parent pop argmap))
        (:plushy (selection/select-parent pop argmap)))
       ;
       :diploid-umad
       (let [rate (utils/onenum (:umad-rate argmap))]
         (-> (:plushy (selection/select-parent pop argmap))
             (diploid-uniform-addition (:instructions argmap) rate)
             (diploid-uniform-deletion rate)))
       ;
       :diploid-vumad ;; variable umad: :umad-rate is interpreted as max, actual uniform 0-max
       (let [rate (rand (utils/onenum (:umad-rate argmap)))]
         (-> (:plushy (selection/select-parent pop argmap))
             (diploid-uniform-addition (:instructions argmap) rate)
             (diploid-uniform-deletion rate)))
       ;
       :diploid-uniform-addition
       (-> (:plushy (selection/select-parent pop argmap))
           (diploid-uniform-addition (:instructions argmap)
                                     (utils/onenum (:umad-rate argmap))))
       ;
       :diploid-uniform-deletion
       (-> (:plushy (selection/select-parent pop argmap))
           (diploid-uniform-deletion (utils/onenum (:umad-rate argmap))))
       ;
       :diploid-flip
       (-> (:plushy (selection/select-parent pop argmap))
           (diploid-flip (utils/onenum (:diploid-flip-rate argmap))))
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
                        (str "No match in new-individual for " op))))))}))