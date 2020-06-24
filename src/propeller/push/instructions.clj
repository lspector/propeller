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
