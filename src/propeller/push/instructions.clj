(ns propeller.push.instructions
  (:use propeller.push.state)
  (:require [tools.character :as char]))

;; =============================================================================
;; PushGP Instructions
;;
;; Instructions must all be either functions that take one Push state and
;; return another, or constant literals.
;;
;; TMH: ERCs?
;; =============================================================================

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

;; Original propel instructions
(def default-instructions
  (list
    'in1
    'integer_+
    'integer_-
    'integer_*
    'integer_%
    'integer_=
    'exec_dup
    'exec_if
    'boolean_and
    'boolean_or
    'boolean_not
    'boolean_=
    'string_=
    'string_take
    'string_drop
    'string_reverse
    'string_concat
    'string_length
    'string_includes?
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
(def opens
  {'exec_dup 1
   'exec_if  2})

;; =============================================================================
;; BOOLEAN
;; =============================================================================

(defn boolean_=
  "Pushes TRUE if the top two BOOLEANs are equal, and FALSE otherwise."
  [state]
  (make-instruction state = [:boolean :boolean] :boolean))

(defn boolean_and
  [state]
  "Pushes the logical AND of the top two BOOLEANs."
  (make-instruction state #(and %1 %2) [:boolean :boolean] :boolean))

(defn boolean_or
  [state]
  "Pushes the logical OR of the top two BOOLEANs."
  (make-instruction state #(or %1 %2) [:boolean :boolean] :boolean))

(defn boolean_not
  [state]
  "Pushes the logical NOT of the top BOOLEAN."
  (make-instruction state not [:boolean] :boolean))

(defn boolean_xor
  [state]
  "Pushes the logical XOR of the top two BOOLEANs."
  (make-instruction state #(or (and %1 (not %2))
                               (and (not %1) %2))
                    [:boolean :boolean]
                    :boolean))

(defn boolean_invert_first_then_and
  [state]
  "Pushes the logical AND of the top two BOOLEANs, after applying NOT to the
  first one."
  (make-instruction state #(and %1 (not %2)) [:boolean :boolean] :boolean))

(defn boolean_invert_second_then_and
  [state]
  "Pushes the logical AND of the top two BOOLEANs, after applying NOT to the
  second one."
  (make-instruction state #(and (not %1) %2) [:boolean :boolean] :boolean))

(defn boolean_fromfloat
  [state]
  "Pushes FALSE if the top FLOAT is 0.0, and TRUE otherwise."
  (make-instruction state #(not (zero? %)) [:float] :boolean))

(defn boolean_frominteger
  [state]
  "Pushes FALSE if the top INTEGER is 0, and TRUE otherwise."
  (make-instruction state #(not (zero? %)) [:integer] :boolean))

;; =============================================================================
;; CHAR
;; =============================================================================

(defn char_isletter
  "Pushes TRUE onto the BOOLEAN stack if the popped character is a letter."
  [state]
  (make-instruction state char/is-letter [:char] :boolean))

(defn char_isdigit
  "Pushes TRUE onto the BOOLEAN stack if the popped character is a digit."
  [state]
  (make-instruction state char/is-digit [:char] :boolean))

(defn char_iswhitespace
  "Pushes TRUE onto the BOOLEAN stack if the popped character is whitespace
  (newline, space, or tab)."
  [state]
  (make-instruction state char/is-whitespace [:char] :boolean))

(defn char_allfromstring
  "Pops the STRING stack and pushes the top element's constituent characters
   onto the CHAR stack, in order. For instance, \"hello\" will result in the
   top of the CHAR stack being o l l e h."
  [state]
  (make-instruction state #(map char %) [:string] :char))

(defn char_frominteger
  "Pops the INTEGER stack and pushes the top element's corresponding ASCII
  value onto the CHAR stack. Integers larger than 128 will be reduced modulo
  128. For instance, 248 will result in x being pushed."
  [state]
  (make-instruction state #(char (mod % 128)) [:integer] :char))

(defn char_fromfloat
  "Pops the FLOAT stack, converts the top item to a whole number, and pushes
  its corresponding ASCII value onto the CHAR stack. Whole numbers larger than
  128 will be reduced modulo 128. For instance, 248.45 will result in x being
  pushed."
  [state]
  (make-instruction state #(char (mod (long %) 128)) [:float] :char))

;; =============================================================================
;; CODE
;; =============================================================================

;; ...to be added

;; =============================================================================
;; EXEC
;; =============================================================================

(defn exec_dup
  [state]
  (if (empty-stack? state :exec)
    state
    (push-to-stack state :exec (first (:exec state)))))

(defn exec_if
  [state]
  (make-instruction state #(if %1 %3 %2) [:boolean :exec :exec] :exec))

;; =============================================================================
;; ENVIRONMENT
;; =============================================================================

;; ...to be added

;; =============================================================================
;; GENETIC TURING MACHINE
;; =============================================================================

;; ...to be added

;; =============================================================================
;; GENOME
;; =============================================================================

;; ...to be added

;; =============================================================================
;; INPUT-OUTPUT
;; =============================================================================

(defn in1
  "Pushes the input labeled :in1 on the inputs map onto the :exec stack."
  [state]
  (push-to-stack state :exec (:in1 (:input state))))

;; =============================================================================
;; INTEGER AND FLOAT
;; =============================================================================

(defn integer_=
  [state]
  (make-instruction state = [:integer :integer] :boolean))

(defn integer_+
  [state]
  (make-instruction state +' [:integer :integer] :integer))

(defn integer_-
  [state]
  (make-instruction state -' [:integer :integer] :integer))

(defn integer_*
  [state]
  (make-instruction state *' [:integer :integer] :integer))

(defn integer_%
  [state]
  (make-instruction state
                    (fn [int1 int2]
                      (if (zero? int2) int1 (quot int1 int2)))
                    [:integer :integer]
                    :integer))

;; =============================================================================
;; RANDOM
;; =============================================================================

(defn boolean_rand
  [state]
  "Pushes a random BOOLEAN."
  (make-instruction state #(rand-nth [true false]) [] :boolean))

;; =============================================================================
;; STRING
;; =============================================================================

(defn string_=
  [state]
  (make-instruction state = [:string :string] :boolean))

(defn string_concat
  [state]
  (make-instruction state #(apply str (concat %1 %2)) [:string :string] :string))

(defn string_drop
  [state]
  (make-instruction state #(apply str (drop %1 %2)) [:integer :string] :string))

(defn string_includes?
  [state]
  (make-instruction state clojure.string/includes? [:string :string] :boolean))

(defn string_length
  [state]
  (make-instruction state count [:string] :integer))

(defn string_reverse
  [state]
  (make-instruction state #(apply str (reverse %)) [:string] :string))

(defn string_take
  [state]
  (make-instruction state #(apply str (take %1 %2)) [:integer :string] :string))

;; =============================================================================
;; TAG
;; =============================================================================

;; ...to be added

;; =============================================================================
;; VECTOR
;; =============================================================================

;; ...to be added

;; =============================================================================
;; ZIP
;; =============================================================================

;; ...to be added
