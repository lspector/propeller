(ns propeller.push.utils.helpers
  (:require [clojure.set]
            [propeller.push.core :as push]
            [propeller.push.state :as state]
            [propeller.utils :as u]
            #?(:cljs [goog.string :as gstring])
            #?(:cljs [goog.string.format])))

;; A utility function for making Push instructions. Takes a state, a function
;; to apply to the args, the stacks to take the args from, and the stack to
;; return the result to. Applies the function to the args (popped from the
;; given stacks), and pushes the result onto the return-stack.
;;
;; If the function returns :ignore-instruction, then we will return the
;; initial state unchanged. This allows instructions to fail gracefully
;; without consuming stack values.
(defn make-instruction
  [state function arg-stacks return-stack]
  (let [popped-args (state/get-args-from-stacks state arg-stacks)]
    (if (= popped-args :not-enough-args)
      state
      (let [result (apply function (:args popped-args))
            new-state (:state popped-args)]
        (if (= result :ignore-instruction)
          state
          (state/push-to-stack new-state return-stack result))))))

;; Given a set of stacks, returns all instructions that operate on those stacks
;; only. Won't include random instructions unless :random is in the set as well
(defn get-stack-instructions
  [stacks]
  (doseq [[instruction-name function] @push/instruction-table]
    (assert
      (:stacks (meta function))
      #?(:clj  (format
                 "ERROR: Instruction %s does not have :stacks defined in metadata."
                 (name instruction-name))
         :cljs (gstring/format
                 "ERROR: Instruction %s does not have :stacks defined in metadata."
                 (name instruction-name)))))
  (for [[instruction-name function] @push/instruction-table
        :when (clojure.set/subset? (:stacks (meta function)) stacks)]
    instruction-name))


#?(:clj
   (def cls->type
     {Boolean    :boolean
      Short      :integer
      Integer    :integer
      Long       :integer
      BigInteger :integer
      Double     :float
      BigDecimal :float
      Float      :float
      Character  :char
      String     :string}))

#?(:cljs
   (def pred->type
     [[boolean? :boolean]
      [int? :integer]
      [float? :float]
      [string? :string]
      [char? :char]]))

(defn get-literal-type
  "If a piece of data is a literal, return its corresponding stack name
   e.g. `:integer`. Otherwise, return `nil`."
  [data]
  (or (when (vector? data)
        (if (empty? data)
          :generic-vector
          (keyword (str "vector_" (name (get-literal-type (u/first-non-nil data)))))))
      #?(:clj  (cls->type (type data))
         :cljs (loop [remaining pred->type]
                 (let [[pred d-type] (first remaining)]
                   (cond
                     (empty? remaining) nil
                     (pred data) d-type
                     :else (recur (rest remaining))))))))

(defn get-vector-literal-type
  "Returns the literal stack corresponding to some vector stack."
  [vector-stack]
  (get state/vec-stacks vector-stack))
