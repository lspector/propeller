(ns propeller.push.instructions.vector-spec
  (:require
   [clojure.test.check.generators :as gen]
   [clojure.test.check.properties :as prop]
   [clojure.test.check.clojure-test :as ct :refer [defspec]]
   [propeller.push.state :as state]
   [propeller.push.instructions.vector :as vector]))

(def gen-type-pairs
  [['gen/small-integer "integer"]
   ['gen/double "float"]
   ['gen/boolean "boolean"]
   ['gen/string "string"]])

(defn generator-for-arg-type
  [arg-type generator]
  (case arg-type
    :boolean 'gen/boolean
    :integer 'gen/small-integer
    :float   'gen/double
    :string  'gen/string
    ; This is for "generic" vectors where the element is provided by
    ; the `generator` argument.
    :vector  `(gen/vector ~generator)
    :item    generator
    :vector_boolean '(gen/vector gen/boolean)
    :vector_integer '(gen/vector gen/small-integer)
    :vector_float   '(gen/vector gen/double)
    :vector_string  '(gen/vector gen/string)))

(defmacro gen-specs
  [spec-name check-fn & arg-types]
  (let [symbol-names (repeatedly (count arg-types) gensym)]
    `(do ~@(for [[generator value-type] gen-type-pairs
                 :let [name (symbol (str spec-name "-spec-" value-type))]]
             `(defspec ~name
                (prop/for-all
                 [~@(mapcat
                     (fn [symbol-name arg-type]
                       [symbol-name (generator-for-arg-type arg-type generator)])
                     symbol-names
                     arg-types)]
                 (~check-fn ~value-type ~@symbol-names)))))))

;;; vector/_contains

(defn check-contains
  "Creates an otherwise empty Push state with the given vector on the
   appropriate vector stack (assumed to be :vector_<value-type>), and
   the given value on the appropriate stack (determined by value-type).
   It then runs the vector/_contains instruction, and confirms that the
   result (on the :boolean stack) is the expected value."
  [value-type vect value]
  (let [stack-type (keyword (str "vector_" value-type))
        start-state (state/push-to-stack
                     (state/push-to-stack state/empty-state
                                          stack-type
                                          vect)
                     (keyword value-type) value)
        end-state (vector/_contains stack-type start-state)
        expected-result (not= (.indexOf vect value) -1)]
    (= expected-result
       (state/peek-stack end-state :boolean))))

(gen-specs "contains" check-contains :vector :item)

;;; vector/_emptyvector

(defn check-empty-vector
  [value-type vect]
  (let [stack-type (keyword (str "vector_" value-type))
        start-state (state/push-to-stack state/empty-state
                                         stack-type
                                         vect)
        end-state (vector/_emptyvector stack-type start-state)]
    (= (empty? vect)
       (state/peek-stack end-state :boolean))))

(gen-specs "empty-vector" check-empty-vector :vector)

;;; vector/_first

(defn check-first
  [value-type vect]
  (let [stack-type (keyword (str "vector_" value-type))
        start-state (state/push-to-stack state/empty-state
                                         stack-type
                                         vect)
        end-state (vector/_first stack-type start-state)]
    (or
     (and (empty? vect)
          (= (state/peek-stack end-state stack-type)
             vect))
     (and
      (= (first vect)
         (state/peek-stack end-state (keyword value-type)))
      (state/empty-stack? end-state stack-type)))))

(gen-specs "first" check-first :vector)

;;; vector/_last

(defn check-last
  [value-type vect]
  (let [stack-type (keyword (str "vector_" value-type))
        start-state (state/push-to-stack state/empty-state
                                         stack-type
                                         vect)
        end-state (vector/_last stack-type start-state)]
    (or
     (and (empty? vect)
          (= (state/peek-stack end-state stack-type)
             vect))
     (and
      (= (last vect)
         (state/peek-stack end-state (keyword value-type)))
      (state/empty-stack? end-state stack-type)))))

(gen-specs "last" check-last :vector)

;;; vector/_indexof

(defn check-indexof
  "Creates an otherwise empty Push state with the given vector on the
   appropriate vector stack (assumed to be :vector_<value-type>), and
   the given value on the appropriate stack (determined by value-type).
   It then runs the vector/_indexof instruction, and confirms that the
   result (on the :integer stack) is the expected value."
  [value-type vect value]
  (let [stack-type (keyword (str "vector_" value-type))
        start-state (state/push-to-stack
                     (state/push-to-stack state/empty-state
                                          stack-type
                                          vect)
                     (keyword value-type) value)
        end-state (vector/_indexof stack-type start-state)
        expected-index (.indexOf vect value)]
    (= expected-index
       (state/peek-stack end-state :integer))))

(gen-specs "indexof" check-indexof :vector :item)

;;; vector/_concat

(defn check-concat
  "Creates an otherwise empty Push state with the two given vectors on the
   appropriate vector stack (assumed to be :vector_<value-type>).
   It then runs the vector/_concat instruction, and confirms that the
   result (on the :vector_<value-type> stack) is the expected value.
   The order of concatenation is that the top of the stack will be
   _second_ in the concatenation, i.e., its elements will come _after_
   the elements in the vector one below it in the stack."
  [value-type first-vect second-vect]
  (let [stack-type (keyword (str "vector_" value-type))
        start-state (state/push-to-stack
                     (state/push-to-stack state/empty-state
                                          stack-type
                                          first-vect)
                     stack-type second-vect)
        end-state (vector/_concat stack-type start-state)]
    (= (concat second-vect first-vect)
       (state/peek-stack end-state stack-type))))

(gen-specs "concat" check-concat :vector :vector)

;;; vector/_subvec

(defn clean-subvec-bounds
  [start stop vect-size]
  (let [start (max 0 start)
        stop (max 0 stop)
        start (min start vect-size)
        stop (min stop vect-size)
        stop (max start stop)]
    [start stop]))

(defn check-subvec
  "Creates an otherwise empty Push state with the given vector on the
   appropriate vector stack (assumed to be :vector_<value-type>), and
   the given values on the integer stack.
   It then runs the vector/_subvec instruction, and confirms that the
   result (on the :vector_<value-type> stack) is the expected value."
  [value-type vect start stop]
  (let [stack-type (keyword (str "vector_" value-type))
        start-state (state/push-to-stack
                     (state/push-to-stack
                      (state/push-to-stack state/empty-state
                                           stack-type
                                           vect)
                      :integer start)
                     :integer stop)
        end-state (vector/_subvec stack-type start-state)
        [cleaned-start cleaned-stop] (clean-subvec-bounds start stop (count vect))
        expected-subvec (subvec vect cleaned-start cleaned-stop)]
    (= expected-subvec
       (state/peek-stack end-state stack-type))))

(gen-specs "subvec" check-subvec :vector :integer :integer)
