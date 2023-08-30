(ns propeller.utils
  "Useful functions."
  (:require [clojure.zip :as zip]
            [clojure.repl :as repl]))

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
  [instructions]
  (let [instruction (rand-nth instructions)]
    (if (fn? instruction)
      (instruction)
      instruction)))


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
  "A utility for concurrent execution of a function on items in a collection.
In single-thread-mode this acts like mapv. Otherwise it acts like pmap but: 
1) coll should be finite, 2) the returned sequence will not be lazy, and will
in fact be a vector, 3) calls to f may occur in any order, to maximize
multicore processor utilization, and 4) takes only one coll so far."
  [f coll args]
  #?(:clj (vec (if (:single-thread-mode args)
                 (doall (map f coll))
                 (let [agents (map #(agent % :error-handler
                                           (fn [agnt except] 
                                             (repl/pst except 1000) 
                                             (System/exit 0)))
                                   coll)]
                   (dorun (map #(send % f) agents))
                   (apply await agents)
                   (doall (map deref agents)))))
     :cljs (mapv f coll)))
