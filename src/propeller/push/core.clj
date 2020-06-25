(ns propeller.push.core)

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

(def instruction-table (atom (hash-map)))

;; Set of original propel instructions
(def default-instructions
  (list :in1
        :integer_add
        :integer_subtract
        :integer_mult
        :integer_quot
        :integer_eq
        :exec_dup
        :exec_if
        :boolean_and
        :boolean_or
        :boolean_not
        :boolean_eq
        :string_eq
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

;; Number of blocks opened by instructions (default = 0)
(def opens {:exec_dup 1
            :exec_if  2})
