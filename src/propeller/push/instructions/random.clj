(ns propeller.push.instructions.random
  (:require [propeller.push.instructions :refer [def-instruction]]))

;;; Pushes a random BOOLEAN
;(def-instruction
;  :boolean_rand
;  (fn [state]
;    (make-instruction state #(rand-nth [true false]) [] :boolean)))
;
;(defn- _rand
;  "For an INTEGER stack type, pushes a newly generated random INTEGER that is
;  greater than or equal to MIN-RANDOM-INTEGER and less than or equal to
;  MAX-RANDOM-INTEGER. Analogous for a FLOAT stack type, with its corresponding
;  MAX-RANDOM-FLOAT and MIN-RANDOM-FLOAT."
;  [state stack-type]
;  (let [data-type (keyword stack-type)]
;    ()))
