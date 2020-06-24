(ns propeller.push.instructions.input-output
  (:require [propeller.push.state :as state]
            [propeller.push.instructions :refer [def-instruction]]))

;; Pushes the input labeled :in1 on the inputs map onto the :exec stack
(def-instruction
  :in1
  (fn [state]
    (state/push-to-stack state :exec (:in1 (:input state)))))
