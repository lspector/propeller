(ns propeller.push.instructions.input-output
  #?(:cljs (:require-macros
             [propeller.push.utils.macros :refer [def-instruction
                                                  generate-instructions]]))
  (:require [propeller.push.state :as state]
            [propeller.push.utils.helpers :refer [make-instruction]]
            [propeller.push.utils.macros :refer [def-instruction
                                                 generate-instructions]]))

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
    (throw #?(:clj (Exception. (str "Undefined input instruction " instruction))
              :cljs (js/Error
                      (str "Undefined input instruction " instruction))))))

;; =============================================================================
;; OUTPUT Instructions
;; =============================================================================

(def-instruction
  :print_newline
  ^{:stacks [:print]}
  (fn [state]
    (let [current-output (state/peek-stack state :output)
          popped-state (state/pop-stack state :output)]
      (state/push-to-stack popped-state :output (str current-output \newline)))))

(def _print
  ^{:stacks [:print]}
  (fn [stack state]
    (if (state/empty-stack? state stack)
      state
      (let [top-item (state/peek-stack state stack)
            top-item-str (if (or (string? top-item)
                                 (char? top-item))
                           top-item
                           (pr-str top-item))
            current-output (state/peek-stack state :output)
            popped-state (state/pop-stack (state/pop-stack state stack) :output)]
        (state/push-to-stack popped-state
                             :output
                             (str current-output top-item-str))))))

(generate-instructions
  [:boolean :char :code :exec :float :integer :string
   :vector_boolean :vector_float :vector_integer :vector_string]
  [_print])
