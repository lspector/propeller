(ns propeller.push.instructions.char
  (:require [propeller.push.utils :refer [def-instruction
                                          make-instruction]]
            [propeller.tools.character :as char]))

;; =============================================================================
;; CHAR Instructions
;; =============================================================================

;; Pushes TRUE onto the BOOLEAN stack if the popped character is a letter
(def-instruction
  :char_isletter
  (fn [state]
    (make-instruction state char/is-letter [:char] :boolean)))

;; Pushes TRUE onto the BOOLEAN stack if the popped character is a digit
(def-instruction
  :char_isdigit
  (fn [state]
    (make-instruction state char/is-digit [:char] :boolean)))

;; Pushes TRUE onto the BOOLEAN stack if the popped character is whitespace
;; (newline, space, or tab)
(def-instruction
  :char_iswhitespace
  (fn [state]
    (make-instruction state char/is-whitespace [:char] :boolean)))

;; Pops the FLOAT stack, converts the top item to a whole number, and pushes
;; its corresponding ASCII value onto the CHAR stack. Whole numbers larger than
;; 128 will be reduced modulo 128. For instance, 248.45 will result in x being
;; pushed.
(def-instruction
  :char_fromfloat
  (fn [state]
    (make-instruction state #(char (mod (long %) 128)) [:float] :char)))

;; Pops the INTEGER stack and pushes the top element's corresponding ASCII
;; value onto the CHAR stack. Integers larger than 128 will be reduced modulo
;; 128. For instance, 248 will result in x being pushed
(def-instruction
  :char_frominteger
  (fn [state]
    (make-instruction state #(char (mod % 128)) [:integer] :char)))

;; Pops the STRING stack and pushes the top element's constituent characters
;; onto the CHAR stack, in order. For instance, "hello" will result in the
;; top of the CHAR stack being o l l e h
(def-instruction
  :char_allfromstring
  (fn [state]
    (make-instruction state #(map char %) [:string] :char)))
