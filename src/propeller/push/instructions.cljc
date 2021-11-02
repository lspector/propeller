(ns propeller.push.instructions
  (:require [clojure.set]
            [propeller.push.state :as state]
            [propeller.utils :as u]
            #?(:cljs [goog.string :as gstring])
            #?(:cljs [goog.string.format])))

;; PushGP instructions are represented as keywords, and stored in an atom. They
;; can be either constant literals or functions that take and return a Push state
(def instruction-table (atom (hash-map)))

;; Number of blocks opened by instructions (default = 0)
(def opens {:exec_dup 1
            :exec_dup_times 1
            :exec_dup_items 0 ; explicitly set to 0 to make it clear that this is intended
            :exec_eq 0 ; explicitly set to 0 to make it clear that this is intended
            :exec_pop 1
            :exec_rot 3
            :exec_shove 1
            :exec_swap 2
            :exec_yank 0 ; explicitly set to 0 to make it clear that this is intended
            :exec_yank_dup 0 ; explicitly set to 0 to make it clear that this is intended
            :exec_deep_dup 0 ; explicitly set to 0 to make it clear that this is intended
            :exec_print 1
            :exec_if  2
            :exec_when 1
            :exec_while 1
            :exec_do_while 1
            :exec_do_range 1
            :exec_do_count 1
            :exec_do_times 1
            :exec_k 2
            :exec_s 3
            :exec_y 1
            :string_iterate 1
            :vector_boolean_iterate 1
            :vector_string_iterate 1
            :vector_integer_iterate 1
            :vector_float_iterate 1
            })


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

(defn def-instruction
  "Defines a Push instruction as a keyword-function pair, and adds it to the
   instruction table"
  [instruction function]
  (swap! instruction-table assoc instruction function))

(defn make-metadata
  "Given a generic function, e.g. _dup, and a stack type to instantiate it for,
   e.g. :char, returns the appropriate stack metadata for that function instance"
  [function stack]
  (->> (:stacks (meta function))
       (replace {:elem (get-vector-literal-type stack)})
       (cons stack)
       set
       (assoc-in (meta function) [:stacks])
       (#(dissoc % :name))))

(defn generate-instructions
  "Given a sequence of stacks, e.g. [:float :integer], and a sequence of suffix
   function strings, e.g. [_add, _mult, _eq], automates the generation of all
   possible combination instructions, which here would be :float_add, :float_mult,
   :float_eq, :integer_add, :integer_mult, and :integer_eq, also transferring
   and updating the generic function's stack-type metadata. For some vector
   instructions, the placeholder :elem will be replaced with the stack of the
   corresponding element type (e.g. for :vector_integer, with :integer)"
  [stacks functions]
  (doseq [stack stacks
          func functions]
    (let [instruction-name (keyword (str (name stack) (:name (meta func))))
          metadata (make-metadata func stack)
          new-func (with-meta (partial func stack) metadata)]
      (def-instruction instruction-name new-func))))


(defn make-instruction
  "A utility function for making Push instructions. Takes a state, a function
   to apply to the args, the stacks to take the args from, and the stack to
   return the result to. Applies the function to the args (popped from the
   given stacks), and pushes the result onto the return-stack.

   If the function returns :ignore-instruction, then we will return the
   initial state unchanged. This allows instructions to fail gracefully
   without consuming stack values."
  [state function arg-stacks return-stack]
  (let [popped-args (state/get-args-from-stacks state arg-stacks)]
    (if (= popped-args :not-enough-args)
      state
      (let [result (apply function (:args popped-args))
            new-state (:state popped-args)]
        (if (= result :ignore-instruction)
          state
          (state/push-to-stack new-state return-stack result))))))

(defn get-stack-instructions
  "Given a set of stacks, returns all instructions that operate on those stacks
   only. Won't include random instructions unless :random is in the set as well"
  [stacks]
  (doseq [[instruction-name function] @instruction-table]
    (assert
     (:stacks (meta function))
     #?(:clj  (format
               "ERROR: Instruction %s does not have :stacks defined in metadata."
               (name instruction-name))
        :cljs (gstring/format
               "ERROR: Instruction %s does not have :stacks defined in metadata."
               (name instruction-name)))))
  (for [[instruction-name function] @instruction-table
        :when (clojure.set/subset? (:stacks (meta function)) stacks)]
    instruction-name))

