(ns propeller.push.instructions.code
  (:require [propeller.push.instructions :refer [def-instruction]]
            [propeller.push.state :as state]
            [propeller.push.utils :refer [make-instruction]]))

(def-instruction
  :exec_dup
  (fn [state]
    (if (state/empty-stack? state :exec)
      state
      (state/push-to-stack state :exec (first (:exec state))))))

(def-instruction
  :exec_if
  (fn [state]
    (make-instruction state #(if %1 %3 %2) [:boolean :exec :exec] :exec)))
