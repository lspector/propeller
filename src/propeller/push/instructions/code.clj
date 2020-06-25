(ns propeller.push.instructions.code
  (:require [propeller.push.state :as state]
            [propeller.push.utils :refer [def-instruction
                                          make-instruction]]))

;; =============================================================================
;; CODE and EXEC Instructions
;; =============================================================================

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
