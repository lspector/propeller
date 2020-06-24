(ns propeller.push.instructions.random
  (:require [propeller.push.instructions :refer [def-instruction
                                                 make-instruction]]))

;; Pushes a random BOOLEAN
(def-instruction
  :boolean_rand
  (fn [state]
    (make-instruction state #(rand-nth [true false]) [] :boolean)))
