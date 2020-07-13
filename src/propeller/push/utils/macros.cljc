(ns propeller.push.utils.macros
  (:require [propeller.push.core :as push]
            [propeller.push.state :as state]
            [propeller.push.utils.helpers :refer [get-vector-literal-type]]))

;; Defines a Push instruction as a keyword-function pair, and adds it to the
;; instruction table
(defmacro def-instruction
  [instruction definition]
  `(swap! push/instruction-table assoc '~instruction ~definition))

;; Given a sequence of stacks, e.g. [:float :integer], and a sequence of suffix
;; function strings, e.g. [_add, _mult, _eq], automates the generation of all
;; possible combination instructions, which here would be :float_add, :float_mult,
;; :float_eq, :integer_add, :integer_mult, and :integer_eq, also transferring
;; and updating the generic function's stack-type metadata. For some vector
;; instructions, the placeholder :elem will be replaced with the stack of the
;; corresponding element type (e.g. for :vector_integer, with :integer)
(defmacro generate-instructions [stacks functions]
  `(do ~@(for [stack stacks
               func functions
               :let [instruction-name (keyword (str (name stack) func))
                     old-stack-data `(:stacks (meta ~func))
                     vec-stack-data `(set
                                       (replace
                                         {:elem (get-vector-literal-type ~stack)}
                                         ~old-stack-data))
                     new-stack-data `(conj ~vec-stack-data ~stack)
                     metadata `(assoc-in (meta ~func) [:stacks] ~new-stack-data)
                     new-func `(with-meta (partial ~func ~stack) ~metadata)]]
           `(def-instruction ~instruction-name ~new-func))))
