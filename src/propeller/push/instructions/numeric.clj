(ns propeller.push.instructions.numeric
  (:require [propeller.push.instructions :refer [make-instruction
                                                 def-instruction]]))

(def-instruction
  :integer_=
  (fn [state]
    (make-instruction state = [:integer :integer] :boolean)))

(def-instruction
  :integer_+
  (fn [state]
    (make-instruction state +' [:integer :integer] :integer)))

(def-instruction
  :integer_-
  (fn [state]
    (make-instruction state -' [:integer :integer] :integer)))

(def-instruction
  :integer_*
  (fn [state]
    (make-instruction state *' [:integer :integer] :integer)))

(def-instruction
  :integer_%
  (fn [state]
    (make-instruction state
                      (fn [int1 int2]
                        (if (zero? int2) int1 (quot int1 int2)))
                      [:integer :integer]
                      :integer)))
