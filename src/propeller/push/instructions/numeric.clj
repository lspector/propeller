(ns propeller.push.instructions.numeric
  (:require [propeller.push.instructions :refer [def-instruction]]
            [propeller.push.utils :refer [generate-functions
                                          make-instruction]]
            [tools.math :as math]))

;; =============================================================================
;; FLOAT and INTEGER (polymorphic)
;; =============================================================================

;; Pushes TRUE onto the BOOLEAN stack if the top two items are equal, and
;; FALSE otherwise
(defn- _=
  [stack state]
  (make-instruction state = [stack stack] :boolean))

;; Pushes TRUE onto the BOOLEAN stack if the second item is greater than the top
;; item, and FALSE otherwise
(defn- _>
  [stack state]
  (make-instruction state > [stack stack] :boolean))

;; Pushes TRUE onto the BOOLEAN stack if the second item is less than the top
;; item, and FALSE otherwise
(defn- _<
  [stack state]
  (make-instruction state < [stack stack] :boolean))

;; Pushes the sum of the top two items onto the same stack
(defn- _+
  [stack state]
  (make-instruction state +' [stack stack] stack))

;; Pushes the difference of the top two items (i.e. the second item minus the
;; top item) onto the same stack
(defn- _-
  [stack state]
  (make-instruction state -' [stack stack] stack))

;; Pushes the product of the top two items onto the same stack
(defn- _*
  [stack state]
  (make-instruction state *' [stack stack] stack))

;; Pushes the quotient of the top two items (i.e. the second item divided by the
;; top item) onto the same stack. If the top item is zero, acts as a NOOP
(defn- _quot
  [stack state]
  (make-instruction state
                    #(if (zero? %2)
                       (list %1 %2) ; push both items back
                       (quot %1 %2))
                    [stack stack]
                    stack))

;; Pushes the second item modulo the top item onto the same stack. If the top
;; item is zero, acts as a NOOP. The modulus is computed as the remainder of the
;; quotient, where the quotient has first been truncated towards negative
;; infinity.
(defn- _%
  [stack state]
  (make-instruction state
                    #(if (zero? %2)
                       (list %1 %2) ; push both items back
                       (mod %1 %2))
                    [stack stack]
                    stack))

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
                    #((if (= stack :float) float int) (if % 1 0))
                    [:boolean]
                    stack))

;; Automate type-specific function generation. All resulting functions take a
;; Push state as their only argument. For FLOAT and INTEGER, create one of each
;; of the 11 following type-specific functions: =, >, <, +, -, *, QUOT, %, MAX,
;; MIN, and FROMBOOLEAN. (22 functions total, with syntax e.g. integer_=,
;; float_min etc.)
(generate-functions
  [:float :integer]
  [_=, _>, _<, _+, _-, _*, _quot, _%, _max, _min, _fromboolean])

;; =============================================================================
;; FLOAT only
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

;; Pushes the tangent of the top FLOAT
(def-instruction
  :float_tan
  (fn [state]
    (make-instruction state math/tan [:float] :float)))

;; Pushes a floating point version of the top INTEGER
(def-instruction
  :float_frominteger
  (fn [state]
    (make-instruction state math/tan [:float] :float)))

;; =============================================================================
;; INTEGER only
;; =============================================================================

;; Pushes the result of truncating the top FLOAT towards negative infinity
(def-instruction
  :integer_fromfloat
  (fn [state]
    (make-instruction state int [:float] :integer)))
