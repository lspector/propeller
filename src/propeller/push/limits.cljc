(ns propeller.push.limits
  "Values used by the Push instructions to keep the stack sizes within reasonable limits
  and values used by the Push instructions to keep computed values within reasonable size limits."
  (:require [propeller.utils :as u]))

;; =============================================================================
;; Values used by the Push instructions to keep the stack sizes within
;; reasonable limits.
;; =============================================================================

;; Limits the number of items that can be duplicated onto a stack at once.
;; We might want to extend this to limit all the different that things may be
;; placed on a stack.
(def max-stack-items
  "Limits the number of items that can be duplicated onto a stack at once.
We might want to extend this to limit all the different that things may
be placed on a stack."
  100)

;; =============================================================================
;; Values used by the Push instructions to keep computed values within
;; reasonable size limits.
;; =============================================================================

;; Used as the maximum magnitude of any integer/float
(def max-number-magnitude
  "Used as the maximum magnitude of any integer/float."
  1.0E6)

;; Used as the minimum magnitude of any float
(def min-number-magnitude
  "Used as the minimum magnitude of any float."
  1.0E-6)

;; Used to ensure that strings don't get too large
(def max-string-length "Used to ensure that strings don't get too large." 1000)

;; Used to ensure that vectors don't get too large
(def max-vector-length "Used to ensure that vectors don't get too large." 1000)

;; Used to ensure that total
;; Set as dynamic for testing purposes.
(def ^:dynamic max-code-points "Used to ensure that total code points don't get too large. Set as dynamic for testing purposes." 100)

;; Used to ensure that the depth of nesting for Push expressions doesn't get too deep.
;; Set as dynamic for testing purposes.
(def ^:dynamic max-code-depth "Used to ensure that the depth of nesting for Push expressions doesn't get too deep. Set as dynamic for testing purposes." 200)

;; Returns a version of the number n that is within reasonable size bounds
(defn limit-number
  "Returns a version of the number n that is within reasonable size bounds."
  [n]
  (if (int? n)
    (cond
      (> n max-number-magnitude) (long max-number-magnitude)
      (< n (- max-number-magnitude)) (long (- max-number-magnitude))
      :else n)
    (cond
      (#?(:clj Double/isNaN
          :cljs js/isNaN) n) 0.0
      (or (= n #?(:clj Double/POSITIVE_INFINITY
                  :cljs js/Infinity))
          (> n max-number-magnitude)) max-number-magnitude
      (or (= n #?(:clj Double/NEGATIVE_INFINITY
                  :cljs js/-Infinity))
          (< n (- max-number-magnitude))) (- max-number-magnitude)
      (< (- min-number-magnitude) n min-number-magnitude) 0.0
      :else n)))

(defn limit-string
  "Limits string length to max-string-length."
  [s]
  (apply str (take max-string-length s)))

(defn limit-vector
  "Limits vector length to max-vector-length."
  [v]
  (vec (take max-vector-length v)))

(defn limit-code
  "Limits code to max-code-points and max-code-depth."
  [code]
  (if (or (> (u/count-points code) max-code-points)
          (> (u/depth code) max-code-depth))
    '() ;; Code that exceeds the limit is discarded.
    code))
