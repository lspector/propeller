(ns propeller.push.instructions.code
  (:require [propeller.utils :as utils]
            [propeller.push.state :as state]
            [propeller.push.utils :refer [def-instruction
                                          generate-instructions
                                          make-instruction]]))

;; =============================================================================
;; Polymorphic Instructions
;; =============================================================================

(def _noop
  ^{:stacks #{}}
  (fn [stack state] state))

(def _do*range
  ^{:stacks #{:exec :integer}}
  (fn [stack state] state))

(def _noop
  ^{:stacks #{}}
  (fn [stack state] state))

(def _noop
  ^{:stacks #{}}
  (fn [stack state] state))

(generate-instructions
  [:exec :code]
  [_noop])


;; =============================================================================
;; CODE Instructions
;; =============================================================================

;; Concatenates the top two instructions on the :code stack and pushes the
;; result back onto the stack
(def-instruction
  :code_append
  ^{:stacks #{:code}}
  (fn [state]
    (make-instruction state
                      #(utils/not-lazy
                         (concat (utils/ensure-list %2)
                                 (utils/ensure-list %1)))
                      [:code :code]
                      :code)))

(def-instruction
  :code_atom
  ^{:stacks #{:code}}
  (fn [state]
    ()))

(def-instruction
  :code_car
  ^{:stacks #{:code}}
  (fn [state]
    ()))

(def-instruction
  :code_cdr
  ^{:stacks #{:code}}
  (fn [state]
    ()))

(def-instruction
  :code_cons
  ^{:stacks #{:code}}
  (fn [state]
    ()))

(def-instruction
  :code_do
  ^{:stacks #{:code}}
  (fn [state]
    ()))

(def-instruction
  :code_do*
  ^{:stacks #{:code}}
  (fn [state]
    ()))

(def-instruction
  :code_append
  ^{:stacks #{:code}}
  (fn [state]
    ()))

;; =============================================================================
;; EXEC Instructions
;; =============================================================================

(def-instruction
  :exec_dup
  ^{:stacks #{:exec}}
  (fn [state]
    (if (state/empty-stack? state :exec)
      state
      (state/push-to-stack state :exec (first (:exec state))))))

(def-instruction
  :exec_if
  ^{:stacks #{:boolean :exec}}
  (fn [state]
    (make-instruction state #(if %1 %3 %2) [:boolean :exec :exec] :exec)))
