(ns propeller.push.instructions.numeric
  (:require [propeller.push.utils :refer [def-instruction
                                          generate-functions
                                          make-instruction]]
            [propeller.tools.math :as math]))

;; =============================================================================
;; FLOAT and INTEGER Instructions (polymorphic)
;; =============================================================================

;; Pushes TRUE onto the BOOLEAN stack if the second item is greater than the top
;; item, and FALSE otherwise
(defn- _gt
  [stack state]
  (make-instruction state > [stack stack] :boolean))

;; Pushes TRUE onto the BOOLEAN stack if the second item is greater than or
;; equal to the top item, and FALSE otherwise
(defn- _gte
  [stack state]
  (make-instruction state >= [stack stack] :boolean))

;; Pushes TRUE onto the BOOLEAN stack if the second item is less than the top
;; item, and FALSE otherwise
(defn- _lt
  [stack state]
  (make-instruction state < [stack stack] :boolean))

;; Pushes TRUE onto the BOOLEAN stack if the second item is less than or equal
;; to the top item, and FALSE otherwise
(defn- _lte
  [stack state]
  (make-instruction state <= [stack stack] :boolean))

;; Pushes the sum of the top two items onto the same stack
(defn- _add
  [stack state]
  (make-instruction state +' [stack stack] stack))

;; Pushes the difference of the top two items (i.e. the second item minus the
;; top item) onto the same stack
(defn- _subtract
  [stack state]
  (make-instruction state -' [stack stack] stack))

;; Pushes the product of the top two items onto the same stack
(defn- _mult
  [stack state]
  (make-instruction state *' [stack stack] stack))

;; Pushes the quotient of the top two items (i.e. the second item divided by the
;; top item) onto the same stack. If the top item is zero, pushes 1
(defn- _quot
  [stack state]
  (make-instruction state #(if (zero? %2) 1 (quot %1 %2)) [stack stack] stack))

;; Pushes the second item modulo the top item onto the same stack. If the top
;; item is zero, pushes 1. The modulus is computed as the remainder of the
;; quotient, where the quotient has first been truncated towards negative
;; infinity.
(defn- _mod
  [stack state]
  (make-instruction state #(if (zero? %2) 1 (mod %1 %2)) [stack stack] stack))

;; Pushes the maximum of the top two items
(defn- _max
  [stack state]
  (make-instruction state max [stack stack] stack))

;; Pushes the minimum of the top two items
(defn- _min
  [stack state]
  (make-instruction state min [stack stack] stack))

;; Pushes 1 / 1.0 if the top BOOLEAN is TRUE, or 0 / 0.0 if FALSE
(defn- _fromboolean
  [stack state]
  (make-instruction state
                    #((if (= stack :integer) int float) (if % 1 0))
                    [:boolean]
                    stack))

;; Pushes the ASCII value of the top CHAR
(defn- _fromchar
  [stack state]
  (make-instruction state (if (= stack :integer) int float) [:char] stack))

;; Pushes the value of the top STRING, if it can be parsed as a number.
;; Otherwise, acts as a NOOP
(defn- _fromstring
  [stack state]
  (make-instruction state
                    #(try ((if (= stack :integer) int float) (read-string %))
                          (catch Exception e))
                    [:string]
                    stack))

;; Pushes the increment (i.e. +1) of the top item of the stack
(defn- _inc
  [stack state]
  (make-instruction state inc [stack] stack))

;; Pushes the decrement (i.e. -1) of the top item of the stack
(defn- _dec
  [stack state]
  (make-instruction state dec [stack] stack))

;; 2 types x 16 functions = 32 instructions
(generate-functions
  [:float :integer]
  [_gt _gte _lt _lte _add _subtract _mult _quot _mod _max _min _inc _dec
   _fromboolean _fromchar _fromstring])

;; =============================================================================
;; FLOAT Instructions only
;; =============================================================================

;; Pushes the cosine of the top FLOAT
(def-instruction
  :float_cos
  (fn [state]
    (make-instruction state math/cos [:float] :float)))

;; Pushes the sine of the top FLOAT
(def-instruction
  :float_sin
  (fn [state]
    (make-instruction state math/sin [:float] :float)))

;; Pushes the tangent of the top FLOAT
(def-instruction
  :float_tan
  (fn [state]
    (make-instruction state math/tan [:float] :float)))

;; Pushes the floating point version of the top INTEGER
(def-instruction
  :float_frominteger
  (fn [state]
    (make-instruction state float [:integer] :float)))

;; =============================================================================
;; INTEGER Instructions only
;; =============================================================================

;; Pushes the result of truncating the top FLOAT towards negative infinity
(def-instruction
  :integer_fromfloat
  (fn [state]
    (make-instruction state int [:float] :integer)))
