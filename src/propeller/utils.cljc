(ns propeller.utils
  "Useful functions."
  (:require [clojure.zip :as zip]
            [clojure.repl :as repl]
            [propeller.tools.metrics :as metrics]
            [propeller.tools.math :as math]
            [propeller.push.instructions.parentheses :as parentheses]))

(defn filter-by-index
  "filters a collection by a list of indices"
  [coll idxs]
  ;(prn {:func :filter-by-index :coll coll :idxs idxs})
  (map (partial nth coll) idxs))

(defn drop-nth
  "drops the nth element from a collection"
  [n coll]
  ;(prn {:func :drop-nth :n n :coll coll})
  (concat
   (take n coll)
   (nthrest coll (inc n))))

(defn first-non-nil
  "Returns the first non-nil values from the collection, or returns `nil` if
  the collection is empty or only contains `nil`."
  [coll]
  (first (filter some? coll)))

(defn indexof
  "Returns the first index of an element in a collection. If the element is not
  present in the collection, returns -1."
  [element coll]
  (or (first (keep-indexed #(if (= element %2) %1) coll)) -1))

(defn not-lazy
  "Returns lst if it is not a seq, or a non-lazy version of lst if it is."
  [lst]
  (if (seq? lst)
    (apply list lst)
    lst))

(defn ensure-list
  "Returns a non-lazy list if passed a seq argument. Otherwise, returns a list
  containing the argument."
  [thing]
  (if (seq? thing)
    (not-lazy thing)
    (list thing)))

(defn random-instruction
  "Returns a random instruction from a supplied pool of instructions, evaluating
  ERC-producing functions to a constant literal."
  [instructions argmap]
  (case (or (:closes argmap) :specified) 
    :specified (let [instruction (rand-nth instructions)]
                 (if (fn? instruction)
                   (instruction)
                   instruction))
    :balanced (let [source (remove #(= % 'close) instructions)
                    p (/ (apply + (filter identity
                                          (map #(get parentheses/opens %) source)))
                         (count source))]
                (if (< (rand) p)
                  'close
                  (let [instruction (rand-nth source)]
                    (if (fn? instruction)
                      (instruction)
                      instruction))))
    :none (let [multi-block-instructions (set (filter (fn [i]
                                                        (let [opens (get parentheses/opens i)]
                                                          (and opens (> opens 1))))
                                                      instructions))
                source (remove (set (conj multi-block-instructions 'close)) instructions)
                instruction (rand-nth source)]
            (if (fn? instruction)
              (instruction)
              instruction))))

(defn count-points
  "Returns the number of points in tree, where each atom and each pair of parentheses
   counts as a point."
  [tree]
  (loop [remaining tree
         total 0]
    (cond (not (seq? remaining))
          (inc total)
          ;;
          (empty? remaining)
          (inc total)
          ;;
          (not (seq? (first remaining)))
          (recur (rest remaining)
                 (inc total))
          ;;
          :else
          (recur (concat (first remaining)
                         (rest remaining))
                 (inc total)))))

(defn seq-zip
  "Returns a zipper for nested sequences, given a root sequence"
  {:added "1.0"}
  [root]
  (zip/zipper seq?
              seq
              (fn [node children] (with-meta children (meta node)))
              root))

(defn depth
  "Returns the height of the nested list called tree.
  Borrowed idea from here: https://stackoverflow.com/a/36865180/2023312
  Works by looking at the path from each node in the tree to the root, and
  finding the longest one.
  Note: does not treat an empty list as having any height."
  [tree]
  (loop [zipper (seq-zip tree)
         height 0]
    (if (zip/end? zipper)
      height
      (recur (zip/next zipper)
             (-> zipper
                 zip/path
                 count
                 (max height))))))

(defn pmapallv
  "A utility for concurrent execution of a function. If :single-thread-mode is 
   truthy in the final arg then this acts like mapv of f on the provided colls. 
   Otherwise it acts like pmap but: 1) the colls should be finite, 2) the 
   returned sequence will not be lazy, and will in fact be a vector, and 
   3) calls to f may occur in any order, to maximize multicore processor utilization."
  [f & colls-args]
  #?(:clj (vec (if (:single-thread-mode (last colls-args))
                 (apply mapv f (butlast colls-args))
                 (let [agents (map #(agent % :error-handler
                                           (fn [agnt except]
                                             (repl/pst except 1000)
                                             (System/exit 0)))
                                   (apply map vector (butlast colls-args)))]
                   (dorun (map (fn [a] (send a #(apply f %))) agents))
                   (apply await agents)
                   (doall (mapv deref agents)))))
     :cljs (apply mapv f (butlast colls-args))))

(def PI
  #?(:clj Math/PI
     :cljs js/Math.PI))

(defn log [x]
  #?(:clj  (Math/log x)
     :cljs (js/Math.log x)))

(defn round [x]
  #?(:clj  (Math/round x)
     :cljs (js/Math.round x)))

(defn gaussian-noise-factor
  "Returns gaussian noise of mean 0, std dev 1."
  []
  (* (Math/sqrt (* -2.0 (log (rand))))
      (Math/cos (* 2.0 PI (rand)))))

(defn perturb-with-gaussian-noise
  "Returns n perturbed with std dev sd."
  [sd n]
  (+ n (* sd (gaussian-noise-factor))))

(defn onenum
  "If given a number, returns it. If given a collection, returns a member of the collection.
  Intended for allowing arguments to genetic operators, such as mutation rates, to take
  collections in addition to single values"
  [thing-or-collection]
  (if (coll? thing-or-collection)
    (rand-nth thing-or-collection)
    thing-or-collection))

(defn pretty-map-println
  "Takes a map and prints it, with each key/value pair on its own line."
  [mp]
  (print "{")
  (let [mp-seq (seq mp)
        [first-key first-val] (first mp-seq)]
    (println (pr-str first-key first-val))
    (doseq [[k v] (rest mp-seq)]
      (println (str " " (pr-str k v)))))
  (println "}"))

(defn count-genes
  "A utility for best match crossover (bmx). Returns the number of segments 
   between (and before and after) instances of :gap."
  [plushy]
  (inc (count (filter #(= % :gap) plushy))))

(defn extract-genes
  "A utility for best match crossover (bmx). Returns the segments of the plushy
   before/between/after instances of :gap."
  [plushy]
  (loop [genes []
         current-gene []
         remainder plushy]
    (cond (empty? remainder)
          (conj genes current-gene)
          ;
          (= (first remainder) :gap)
          (recur (conj genes current-gene)
                 []
                 (rest remainder))
          ;
          :else
          (recur genes
                 (conj current-gene (first remainder))
                 (rest remainder)))))

(defn bmx-distance
  "A utility function for bmx. Returns the distance between two plushies
   computed as half of their multiset-distance plus their length difference."
  [p1 p2]
  (+ (* 0.5 (metrics/multiset-distance p1 p2))
     (math/abs (- (count p1) (count p2)))))

(defn remove-empty-genes
  "A utility function for bmx-related genetic operators. Returns the provided
   plushy with any empty genes (regions before/between/after instances of :gap)
   removed."
  [plushy]
  (vec (flatten (interpose :gap (filter #(not (empty? %))
                                        (extract-genes plushy))))))

(defn break-up
  "A utility function for bmx-related genetic operators. Returns the provided
   :gap-free plushy with gaps randomly inserted to ensure that no gene is longer
   than the provided limit. Will break just before an instruction that opens code
   blocks or just after a close, unless there are no opportunities to do so"
  [gene limit]
  (if (> (count gene) limit)
    (let [openers (map first
                       (filter #(and (second %)
                                     (not (zero? (second %))))
                               parentheses/opens))
          i (if (or (some (set openers) (rest gene))
                    (some #{'close} (butlast gene)))
              (rand-nth (filter identity
                                (concat (map-indexed (fn [ix item]
                                                       (if (some #{item} openers)
                                                         (inc ix)
                                                         nil))
                                                     (rest gene))
                                        (map-indexed (fn [ix item]
                                                       (if (= item 'close)
                                                         (inc ix)
                                                         nil))
                                                     (butlast gene)))))
              (inc (rand-int (dec (count gene)))))]
      (concat (break-up (take i gene) limit)
              [:gap]
              (break-up (drop i gene) limit)))
    gene))

(defn enforce-gene-length-limit
  "A utility function for bmx-related genetic operators. Returns the provided
   plushy with any over-length genes broken into non-empty pieces, recursively
   until all genes obey the limit."
  [plushy limit]
  (flatten (interpose :gap
                      (mapv (fn [gene]
                              (break-up gene limit))
                            (extract-genes plushy)))))
