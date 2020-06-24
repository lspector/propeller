(ns propeller.push.instructions
  (:require [propeller.push.state :refer [get-args-from-stacks
                                          push-to-stack]]))

;; =============================================================================
;; PushGP Instructions
;;
;; Instructions are represented as keywords, and stored in an atom.
;;
;; Instructions must all be either functions that take one Push state and
;; return another, or constant literals.
;;
;; TMH: ERCs?
;; =============================================================================

;; Set of original propel instructions
(def default-instructions
  (list :in1
        :integer_+
        :integer_-
        :integer_*
        :integer_%
        :integer_=
        :exec_dup
        :exec_if
        :boolean_and
        :boolean_or
        :boolean_not
        :boolean_=
        :string_=
        :string_take
        :string_drop
        :string_reverse
        :string_concat
        :string_length
        :string_includes?
        'close
        0
        1
        true
        false
        ""
        "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
        "A"
        "C"
        "G"
        "T"))

(def instruction-table (atom (hash-map)))

(defmacro def-instruction
  [instruction definition]
  `(swap! instruction-table assoc '~instruction ~definition))

;; Number of blocks opened by instructions (default = 0)
(def opens {:exec_dup 1
            :exec_if  2})

(defn make-instruction
  "A utility function for making Push instructions. Takes a state, a function
  to apply to the args, the stacks to take the args from, and the stack to
  return the result to. Applies the function to the args (popped from the
  given stacks), and pushes the result onto the return-stack."
  [state function arg-stacks return-stack]
  (let [popped-args (get-args-from-stacks state arg-stacks)]
    (if (= popped-args :not-enough-args)
      state
      (let [result (apply function (:args popped-args))
            new-state (:state popped-args)]
        (push-to-stack new-state return-stack result)))))
