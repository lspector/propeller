(ns propeller.tools.metrics
  (:require [propeller.tools.math :as math]))

(defn mean
  "Returns the mean of a collection."
  [coll]
  (if (empty? coll) 0 (math/div (apply + coll) (count coll))))

(defn median
  "Returns the median of a collection."
  [coll]
  (let [sorted-coll (sort coll)
        count (count sorted-coll)
        midpoint (quot count 2)]
    (if (odd? count)
      (nth sorted-coll midpoint)
      (let [below (nth sorted-coll (dec midpoint))
            above (nth sorted-coll midpoint)]
        (mean [below above])))))

(defn hamming-distance
  "Calculates the Hamming distance between two sequences, including strings."
  [seq1 seq2]
  (apply + (map #(if (= %1 %2) 0 1) seq1 seq2)))

(defn levenshtein-distance
  "Levenshtein Distance - http://en.wikipedia.org/wiki/Levenshtein_distance
  In Information Theory and Computer Science, the Levenshtein distance is a
  metric for measuring the amount of difference between two sequences. This
  is a functional implementation of the Levenshtein edit distance with as
  little mutability as possible. Still maintains the O(nm) guarantee."
  [a b & {p :predicate :or {p =}}]
  (cond
    (empty? a) (count b)
    (empty? b) (count a)
    :else (peek
            (reduce
              ;; we use a simple reduction to convert the previous row into the
              ;; next-row using the compute-next-row which takes a current
              ;; element, the previous-row computed so far, and the predicate
              ;; to compare for equality
              (fn [prev-row current-element]
                (compute-next-row prev-row current-element b p))
              ;; we need to initialize the prev-row with the edit distance
              ;; between the various prefixes of b and the empty string
              (range (inc (count b)))
              a))))

(defn sequence-similarity
  "Returns a number between 0 and 1, indicating how similar the sequences are
  as a normalized, inverted Levenshtein distance, with 1 indicating identity
  and 0 indicating no similarity."
  [seq1 seq2]
  (if (and (empty? seq1) (empty? seq2))
    1
    (let [distance (levenshtein-distance seq1 seq2)
          max-distance (max (count seq1) (count seq2))]
      (math/div (- max-distance distance) max-distance))))
