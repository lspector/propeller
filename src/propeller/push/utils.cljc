(ns propeller.push.utils
  (:require [clojure.set]
            [propeller.push.core :as push]
            [propeller.push.state :as state]))

(defmacro def-instruction
  [instruction definition]
  `(swap! push/instruction-table assoc '~instruction ~definition))

;; A utility function for making Push instructions. Takes a state, a function
;; to apply to the args, the stacks to take the args from, and the stack to
;; return the result to. Applies the function to the args (popped from the
;; given stacks), and pushes the result onto the return-stack
(defn make-instruction
  [state function arg-stacks return-stack]
  (let [popped-args (state/get-args-from-stacks state arg-stacks)]
    (if (= popped-args :not-enough-args)
      state
      (let [result (apply function (:args popped-args))
            new-state (:state popped-args)]
        (state/push-to-stack new-state return-stack result)))))

;; Given a sequence of stacks, e.g. [:float :integer], and a sequence of suffix
;; function strings, e.g. [_add, _mult, _eq], automates the generation of all
;; possible combination instructions, which here would be :float_add, :float_mult,
;; :float_eq, :integer_add, :integer_mult, and :integer_eq, also transferring
;; and updating the generic function's stack-type metadata
(defmacro generate-instructions [stacks functions]
  `(do ~@(for [stack stacks
               func functions
               :let [instruction-name (keyword (str (name stack) func))
                     metadata `(update-in (meta ~func) [:stacks] #(conj % ~stack))
                     new-func `(with-meta (partial ~func ~stack) ~metadata)]]
           `(def-instruction ~instruction-name ~new-func))))

;; Given a set of stacks, returns all instructions that operate on those stacks
;; only. This won't include random or parenthesis-altering instructions unless
;; :random or :parentheses respectively are in the stacks set
(defn get-stack-instructions
  [stacks]
  (doseq [[instruction-name function] @push/instruction-table]
    (assert
      (:stacks (meta function))
      (format "ERROR: Instruction %s does not have :stacks defined in metadata."
              (name instruction-name))))
  (for [[instruction-name function] @push/instruction-table
        :when (clojure.set/subset? (:stacks (meta function)) stacks)]
    instruction-name))

;; If a piece of data is a literal, return its corresponding stack name, e.g.
;; :integer. Otherwise, return nil"
(defn get-literal-type
  [data]
  (let [literals {:boolean        (fn [thing] (or (true? thing) (false? thing)))
                  :char           char?
                  :float          float?
                  :integer        integer?
                  :string         string?
                  :vector_boolean (fn [thing] (and (vector? thing)
                                                   (or (true? (first thing))
                                                       (false? (first thing)))))
                  :vector_float   (fn [thing] (and (vector? thing)
                                                   (float? (first thing))))
                  :vector_integer (fn [thing] (and (vector? thing)
                                                   (integer? (first thing))))
                  :vector_string  (fn [thing] (and (vector? thing)
                                                   (string? (first thing))))
                  :generic-vector (fn [thing] (= [] thing))}]
    (first (for [[stack function] literals
                 :when (function data)]
             stack))))

;; Pretty-prints a Push state, for logging or debugging purposes
(defn print-state
  [state]
  (doseq [stack (keys state/empty-state)]
    (printf "%-15s = " stack)
    (prn (if (get state stack) (get state stack) '()))
    (flush)))
