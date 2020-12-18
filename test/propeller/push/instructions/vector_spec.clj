(ns propeller.push.instructions.vector-spec
  (:require
   [clojure.test.check.generators :as gen]
   [clojure.test.check.properties :as prop]
   [clojure.test.check.clojure-test :as ct :refer [defspec]]
   [propeller.push.state :as state]
   [propeller.push.instructions.vector :as vector]))

;;; vector/_emptyvector

(defn check-empty-vector
  [generator value-type]
  (let [stack-type (keyword (str "vector_" value-type))]
    (prop/for-all [vect (gen/vector generator)]
                  (let [start-state (state/push-to-stack state/empty-state
                                                         stack-type
                                                         vect)
                        end-state (vector/_emptyvector stack-type start-state)]
                    (= (empty? vect)
                       (state/peek-stack end-state :boolean))))))

(defmacro empty-vector-spec
  [generator value-type]
    `(defspec ~(symbol (str "empty-vector-spec-" value-type))
       100
       (check-empty-vector ~generator ~value-type)))

(empty-vector-spec gen/small-integer "integer")
(empty-vector-spec gen/double "float")
(empty-vector-spec gen/boolean "boolean")
(empty-vector-spec gen/string "string")

;;; vector/_first

(defn check-first
  [generator value-type]
  (let [stack-type (keyword (str "vector_" value-type))]
    (prop/for-all [vect (gen/vector generator)]
                  (let [start-state (state/push-to-stack state/empty-state
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
                      (state/empty-stack? end-state stack-type)))))))

(defmacro first-spec
  [generator value-type]
  `(defspec ~(symbol (str "first-spec-" value-type))
     100
     (check-first ~generator ~value-type)))

(first-spec gen/small-integer "integer")
(first-spec gen/double "float")
(first-spec gen/boolean "boolean")
(first-spec gen/string "string")

;;; vector/_indexof

(defn check-expected-index
  "Creates an otherwise empty Push state with the given vector on the
   appropriate vector stack (assumed to be :vector_<value-type>), and
   the given value on the appropriate stack (determined by value-type).
   It then runs the vector/_indexof instruction, and confirms that the
   result (on the :integer stack) is the expected value."
  [vect value value-type]
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

(defmacro indexof-spec
  [generator value-type]
  `(do
     (defspec ~(symbol (str "indexof-spec-" value-type))
       ; Should this be smaller for booleans? (Ditto for below.)
       100
       (prop/for-all [vect# (gen/vector ~generator)
                      value# ~generator]
                     (check-expected-index vect# value# ~value-type)))
       ; For float and string vectors, it's rather rare to actually have a random value that
       ; appears in the vector, so we don't consistently test the case where it should 
       ; return -1. So maybe we do need a separate test for those? 
     (defspec ~(symbol (str "indexof-spec-has-value-" value-type))
       100
       (prop/for-all [vect# (gen/not-empty (gen/vector ~generator))]
                     (check-expected-index vect# (rand-nth vect#) ~value-type)))))

(indexof-spec gen/small-integer "integer")
(indexof-spec gen/double "float")
(indexof-spec gen/boolean "boolean")
(indexof-spec gen/string "string")

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
  [vect start stop value-type]
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

(defmacro subvec-spec
  [generator value-type]
  `(defspec ~(symbol (str "subvec-spec-" value-type))
     (prop/for-all [vect# (gen/vector ~generator)
                    start# gen/small-integer
                    stop# gen/small-integer]
                   (check-subvec vect# start# stop# ~value-type))))

(subvec-spec gen/small-integer "integer")
(subvec-spec gen/double "float")
(subvec-spec gen/boolean "boolean")
(subvec-spec gen/string "string")