(ns propeller.push.instructions.input-output
  "INPUT and OUTPUT Instructions.
  Additional instructions can be found at [Additional Instructions](Additional_Instructions.md)."
  (:require [propeller.push.state :as state]
            [propeller.push.instructions :refer [def-instruction
                                                 generate-instructions]]))

;; =============================================================================
;; INPUT Instructions
;; =============================================================================

;; Allows Push to handle input instructions of the form :inN, e.g. :in2, taking
;; elements thus labeled from the :input map and pushing them onto the :exec
;; stack.
(defn handle-input-instruction
  "Allows Push to handle input instructions of the form :inN, e.g. :in2, taking
  elements thus labeled from the :input map and pushing them onto the :exec
  stack."
  [state instruction]
  (if (contains? (:input state) instruction)
    (let [input (instruction (:input state))]
      (state/push-to-stack state :exec input))
    (throw #?(:clj  (Exception. (str "Undefined instruction " instruction))
              :cljs (js/Error
                      (str "Undefined instruction " instruction))))))

;; =============================================================================
;; OUTPUT Instructions
;; =============================================================================

;; Prints new line
(def-instruction
  :print_newline
  ^{:stacks [:print]}
  (fn [state]
    (let [current-output (state/peek-stack state :print)
          popped-state (state/pop-stack state :print)]
      (state/push-to-stack popped-state :print (str current-output \newline)))))

(def _print
  "Instruction to print output."
  ^{:stacks [:print]
    :name "_print"}
  (fn [stack state]
    (if (state/empty-stack? state stack)
      state
      (let [top-item (state/peek-stack state stack)
            top-item-str (if (or (string? top-item)
                                 (char? top-item))
                           top-item
                           (pr-str top-item))
            current-output (state/peek-stack state :print)
            popped-state (state/pop-stack (state/pop-stack state stack) :print)]
        (state/push-to-stack popped-state
                             :print
                             (str current-output top-item-str))))))

(generate-instructions
  [:boolean :char :code :exec :float :integer :string
   :vector_boolean :vector_float :vector_integer :vector_string]
  [_print])
