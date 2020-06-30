(ns propeller.push.instructions.input-output
  (:require [propeller.push.state :as state]
            [propeller.push.utils :refer [def-instruction]]))

;; =============================================================================
;; INPUT Instructions
;; =============================================================================

;; Allows Push to handle input instructions of the form :inN, e.g. :in2, taking
;; elements thus labeled from the :input stack and pushing them onto the :exec
;; stack. We can tell whether a particular inN instruction is valid if N-1
;; values are on the input stack.
(defn handle-input-instruction
  [state instruction]
  (if-let [input (instruction (:input state))]
    (state/push-to-stack state :exec input)
    (throw (Exception. (str "Undefined input instruction " instruction)))))

;; =============================================================================
;; OUTPUT and PRINT Instructions
;; =============================================================================
