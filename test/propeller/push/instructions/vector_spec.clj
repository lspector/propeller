;(ns propeller.push.instructions.vector-spec
;  (:require
;   [clojure.test.check.generators :as gen]
;   [clojure.test.check.properties :as prop]
;   [clojure.test.check.clojure-test :as ct :refer [defspec]]
;   [propeller.push.state :as state]
;   [propeller.push.instructions.vector :as vector]
;   [propeller.push.interpreter :as interpreter]))
;
;(def gen-type-pairs
;  [['gen/small-integer "integer"]
;   ['gen/double "float"]
;   ['gen/boolean "boolean"]
;   ['gen/string "string"]])
;
;(defn generator-for-arg-type
;  [arg-type generator]
;  (case arg-type
;    :boolean 'gen/boolean
;    :integer 'gen/small-integer
;    :float   'gen/double
;    :string  'gen/string
;    ; This is for "generic" vectors where the element is provided by
;    ; the `generator` argument.
;    :vector  `(gen/vector ~generator)
;    :item    generator
;    :vector_boolean '(gen/vector gen/boolean)
;    :vector_integer '(gen/vector gen/small-integer)
;    :vector_float   '(gen/vector gen/double)
;    :vector_string  '(gen/vector gen/string)))
;
;(defmacro gen-specs
;  [spec-name check-fn & arg-types]
;  (let [symbol-names (repeatedly (count arg-types) gensym)]
;    `(do ~@(for [[generator value-type] gen-type-pairs
;                 :let [name (symbol (str spec-name "-spec-" value-type))]]
;             `(defspec ~name
;                (prop/for-all
;                 [~@(mapcat
;                     (fn [symbol-name arg-type]
;                       [symbol-name (generator-for-arg-type arg-type generator)])
;                     symbol-names
;                     arg-types)]
;                 (~check-fn ~value-type ~@symbol-names)))))))
;
;;;; vector/_butlast
;
;(defn check-butlast
;  [value-type vect]
;  (let [stack-type (keyword (str "vector_" value-type))
;        start-state (state/push-to-stack state/empty-state
;                                         stack-type
;                                         vect)
;        end-state (vector/_butlast stack-type start-state)
;        expected-result (vec (butlast vect))]
;    (= expected-result
;       (state/peek-stack end-state stack-type))))
;
;(gen-specs "butlast" check-butlast :vector)
;
;;;; vector/_concat
;
;(defn check-concat
;  "Creates an otherwise empty Push state with the two given vectors on the
;   appropriate vector stack (assumed to be :vector_<value-type>).
;   It then runs the vector/_concat instruction, and confirms that the
;   result (on the :vector_<value-type> stack) is the expected value.
;   The order of concatenation is that the top of the stack will be
;   _second_ in the concatenation, i.e., its elements will come _after_
;   the elements in the vector one below it in the stack."
;  [value-type first-vect second-vect]
;  (let [stack-type (keyword (str "vector_" value-type))
;        start-state (state/push-to-stack
;                     (state/push-to-stack state/empty-state
;                                          stack-type
;                                          first-vect)
;                     stack-type second-vect)
;        end-state (vector/_concat stack-type start-state)]
;    (= (concat second-vect first-vect)
;       (state/peek-stack end-state stack-type))))
;
;(gen-specs "concat" check-concat :vector :vector)
;
;;;; vecotr/_conj
;
;(defn check-conj
;  [value-type vect value]
;  (let [stack-type (keyword (str "vector_" value-type))
;        start-state (state/push-to-stack
;                     (state/push-to-stack state/empty-state
;                                          stack-type
;                                          vect)
;                     (keyword (str value-type))
;                     value)
;        end-state (vector/_conj stack-type start-state)
;        expected-result (conj vect value)]
;    (= expected-result
;       (state/peek-stack end-state stack-type))))
;
;(gen-specs "conj" check-conj :vector :item)
;
;;;; vector/_contains
;
;(defn check-contains
;  "Creates an otherwise empty Push state with the given vector on the
;   appropriate vector stack (assumed to be :vector_<value-type>), and
;   the given value on the appropriate stack (determined by value-type).
;   It then runs the vector/_contains instruction, and confirms that the
;   result (on the :boolean stack) is the expected value."
;  [value-type vect value]
;  (let [stack-type (keyword (str "vector_" value-type))
;        start-state (state/push-to-stack
;                     (state/push-to-stack state/empty-state
;                                          stack-type
;                                          vect)
;                     (keyword value-type) value)
;        end-state (vector/_contains stack-type start-state)
;        expected-result (not= (.indexOf vect value) -1)]
;    (= expected-result
;       (state/peek-stack end-state :boolean))))
;
;(gen-specs "contains" check-contains :vector :item)
;
;;;; vector/_emptyvector
;
;(defn check-empty-vector
;  [value-type vect]
;  (let [stack-type (keyword (str "vector_" value-type))
;        start-state (state/push-to-stack state/empty-state
;                                         stack-type
;                                         vect)
;        end-state (vector/_emptyvector stack-type start-state)]
;    (= (empty? vect)
;       (state/peek-stack end-state :boolean))))
;
;(gen-specs "empty-vector" check-empty-vector :vector)
;
;;;; vector/_first
;
;(defn check-first
;  [value-type vect]
;  (let [stack-type (keyword (str "vector_" value-type))
;        start-state (state/push-to-stack state/empty-state
;                                         stack-type
;                                         vect)
;        end-state (vector/_first stack-type start-state)]
;    (or
;     (and (empty? vect)
;          (= (state/peek-stack end-state stack-type)
;             vect))
;     (and
;      (= (first vect)
;         (state/peek-stack end-state (keyword value-type)))
;      (state/empty-stack? end-state stack-type)))))
;
;(gen-specs "first" check-first :vector)
;
;;;; vector/_indexof
;
;(defn check-indexof
;  "Creates an otherwise empty Push state with the given vector on the
;   appropriate vector stack (assumed to be :vector_<value-type>), and
;   the given value on the appropriate stack (determined by value-type).
;   It then runs the vector/_indexof instruction, and confirms that the
;   result (on the :integer stack) is the expected value."
;  [value-type vect value]
;  (let [stack-type (keyword (str "vector_" value-type))
;        start-state (state/push-to-stack
;                     (state/push-to-stack state/empty-state
;                                          stack-type
;                                          vect)
;                     (keyword value-type) value)
;        end-state (vector/_indexof stack-type start-state)
;        expected-index (.indexOf vect value)]
;    (= expected-index
;       (state/peek-stack end-state :integer))))
;
;(gen-specs "indexof" check-indexof :vector :item)
;
;;;; vector/_iterate
;
;(defn check-iterate
;  [value-type vect]
;  (let [stack-type (keyword (str "vector_" value-type))
;        print-instr (keyword (str value-type "_print"))
;        iter-instr (keyword (str "vector_" value-type "_iterate"))
;        program [iter-instr print-instr]
;        start-state (-> state/empty-state
;                        (state/push-to-stack stack-type vect)
;                        (state/push-to-stack :output ""))
;        ; 4 times the vector length should be enough for this iteration, perhaps even
;        ; more than we strictly need.
;        end-state (interpreter/interpret-program program start-state (* 4 (count vect)))
;        ; pr-str adds escaped quote marks, which causes tests to fail because _print
;        ; treats strings and characters specially and does not call pr-str on them.
;        to-str-fn (if (= value-type "string") identity pr-str)
;        expected-result (apply str (map to-str-fn vect))]
;    (= expected-result
;       (state/peek-stack end-state :output))))
;
;(gen-specs "iterate" check-iterate :vector)
;
;;;; vector/_last
;
;(defn check-last
;  [value-type vect]
;  (let [stack-type (keyword (str "vector_" value-type))
;        start-state (state/push-to-stack state/empty-state
;                                         stack-type
;                                         vect)
;        end-state (vector/_last stack-type start-state)]
;    (or
;     (and (empty? vect)
;          (= (state/peek-stack end-state stack-type)
;             vect))
;     (and
;      (= (last vect)
;         (state/peek-stack end-state (keyword value-type)))
;      (state/empty-stack? end-state stack-type)))))
;
;(gen-specs "last" check-last :vector)
;
;;;; vector/_length
;
;(defn check-length
;  [value-type vect]
;  (let [stack-type (keyword (str "vector_" value-type))
;        start-state (state/push-to-stack state/empty-state
;                                         stack-type
;                                         vect)
;        end-state (vector/_length stack-type start-state)
;        expected-result (count vect)]
;    (= expected-result
;       (state/peek-stack end-state :integer))))
;
;(gen-specs "length" check-length :vector)
;
;;;; vector/_nth
;
;(defn check-nth
;  [value-type vect n]
;  (let [stack-type (keyword (str "vector_" value-type))
;        start-state (state/push-to-stack
;                     (state/push-to-stack state/empty-state
;                                          stack-type
;                                          vect)
;                     :integer
;                     n)
;        end-state (vector/_nth stack-type start-state)]
;    (or
;     (and (empty? vect)
;          (= (state/peek-stack end-state stack-type)
;             vect))
;     (and
;          (= (get vect (mod n (count vect)))
;             (state/peek-stack end-state (keyword value-type)))))))
;
;(gen-specs "nth" check-nth :vector :integer)
;
;;;; vector/_occurrencesof
;
;(defn check-occurrencesof
;  [value-type vect value]
;  (let [stack-type (keyword (str "vector_" value-type))
;        start-state (state/push-to-stack
;                     (state/push-to-stack state/empty-state
;                                          stack-type
;                                          vect)
;                     (keyword value-type)
;                     value)
;        end-state (vector/_occurrencesof stack-type start-state)
;        expected-result (count (filterv #(= value %) vect))]
;    (= expected-result
;       (state/peek-stack end-state :integer))))
;
;(gen-specs "occurrencesof" check-occurrencesof :vector :item)
;
;;;; vector/_pushall
;
;(defn check-pushall
;  [value-type vect]
;  (let [stack-type (keyword (str "vector_" value-type))
;        start-state (state/push-to-stack state/empty-state
;                                         stack-type
;                                         vect)
;        end-state (vector/_pushall stack-type start-state)
;        value-stack (keyword value-type)
;        vect-length (count vect)]
;    (and
;       (=
;          (vec (state/peek-stack-many end-state value-stack vect-length))
;          vect)
;       (state/empty-stack?
;          (state/pop-stack-many end-state value-stack vect-length)
;          value-stack))))
;
;(gen-specs "pushall" check-pushall :vector)
;
;;;; vector/_remove
;
;(defn check-remove
;  [value-type vect value]
;  (let [stack-type (keyword (str "vector_" value-type))
;        start-state (state/push-to-stack
;                     (state/push-to-stack state/empty-state
;                                          stack-type
;                                          vect)
;                     (keyword value-type)
;                     value)
;        end-state (vector/_remove stack-type start-state)]
;    (= []
;       (filterv #(= % value) (state/peek-stack end-state stack-type)))))
;
;(gen-specs "remove" check-remove :vector :item)
;
;;;; vector/_replace
;
;(defn check-replace
;  [value-type vect toreplace replacement]
;  (let [stack-type (keyword (str "vector_" value-type))
;        value-stack (keyword value-type)
;        start-state (state/push-to-stack
;                     (state/push-to-stack
;                      (state/push-to-stack state/empty-state
;                                           stack-type
;                                           vect)
;                      value-stack
;                      toreplace)
;                     value-stack
;                     replacement)
;        end-state (vector/_replace stack-type start-state)
;        expected-result (replace {toreplace replacement} vect)]
;    (= expected-result
;       (state/peek-stack end-state stack-type))))
;
;(gen-specs "replace" check-replace :vector :item :item)
;
;;;; vector/_replacefirst
;
;(defn check-replacefirst
;  [value-type vect toreplace replacement]
;  (let [stack-type (keyword (str "vector_" value-type))
;        value-stack (keyword value-type)
;        start-state (state/push-to-stack
;                     (state/push-to-stack
;                      (state/push-to-stack state/empty-state
;                                           stack-type
;                                           vect)
;                      value-stack
;                      toreplace)
;                     value-stack
;                     replacement)
;        end-state (vector/_replacefirst stack-type start-state)
;        end-vector (state/peek-stack end-state stack-type)
;        replacement-index (.indexOf vect toreplace)]
;    (or
;     (and (= replacement-index -1)
;          (state/empty-stack? end-state value-stack)
;          (= vect end-vector))
;     (and (state/empty-stack? end-state value-stack)
;          (= end-vector (assoc vect replacement-index replacement))))))
;
;(gen-specs "replacefirst" check-replacefirst :vector :item :item)
;
;;;; vector/_rest
;
;(defn check-rest
;  [value-type vect]
;  (let [stack-type (keyword (str "vector_" value-type))
;        start-state (state/push-to-stack state/empty-state
;                                         stack-type
;                                         vect)
;        end-state (vector/_rest stack-type start-state)
;        expected-result (vec (rest vect))]
;    (= expected-result
;       (state/peek-stack end-state stack-type))))
;
;(gen-specs "rest" check-rest :vector)
;
;;;; vector/_reverse
;
;(defn check-reverse
;  [value-type vect]
;  (let [stack-type (keyword (str "vector_" value-type))
;        start-state (state/push-to-stack state/empty-state
;                                         stack-type
;                                         vect)
;        end-state (vector/_reverse stack-type start-state)
;        expected-result (vec (reverse vect))]
;    (= expected-result
;       (state/peek-stack end-state stack-type))))
;
;(gen-specs "reverse" check-reverse :vector)
;
;;;; vector/_set
;
;(defn check-set
;  [value-type vect value n]
;  (let [stack-type (keyword (str "vector_" value-type))
;        value-stack (keyword value-type)
;        start-state (state/push-to-stack
;                      (state/push-to-stack
;                        (state/push-to-stack state/empty-state
;                                             stack-type
;                                             vect)
;                        value-stack
;                        value)
;                      :integer
;                      n)
;        end-state (vector/_set stack-type start-state)]
;    (or
;     (and
;      (empty? vect)
;      (not (state/empty-stack? end-state :integer))
;      (not (state/empty-stack? end-state value-stack))
;      (= vect (state/peek-stack end-state stack-type)))
;     (and
;      (= (state/peek-stack end-state stack-type)
;         (assoc vect (mod n (count vect)) value))
;      (state/empty-stack? end-state :integer)
;      (state/empty-stack? end-state value-stack)))))
;
;(gen-specs "set" check-set :vector :item :integer)
;
;;;; vector/_subvec
;
;(defn clean-subvec-bounds
;  [start stop vect-size]
;  (let [start (max 0 start)
;        stop (max 0 stop)
;        start (min start vect-size)
;        stop (min stop vect-size)
;        stop (max start stop)]
;    [start stop]))
;
;(defn check-subvec
;  "Creates an otherwise empty Push state with the given vector on the
;   appropriate vector stack (assumed to be :vector_<value-type>), and
;   the given values on the integer stack.
;   It then runs the vector/_subvec instruction, and confirms that the
;   result (on the :vector_<value-type> stack) is the expected value."
;  [value-type vect start stop]
;  (let [stack-type (keyword (str "vector_" value-type))
;        start-state (state/push-to-stack
;                     (state/push-to-stack
;                      (state/push-to-stack state/empty-state
;                                           stack-type
;                                           vect)
;                      :integer start)
;                     :integer stop)
;        end-state (vector/_subvec stack-type start-state)
;        [cleaned-start cleaned-stop] (clean-subvec-bounds start stop (count vect))
;        expected-subvec (subvec vect cleaned-start cleaned-stop)]
;    (= expected-subvec
;       (state/peek-stack end-state stack-type))))
;
;(gen-specs "subvec" check-subvec :vector :integer :integer)
;
;;;; vector/_take
;
;(defn check-take
;  [value-type vect n]
;  (let [stack-type (keyword (str "vector_" value-type))
;        start-state (state/push-to-stack
;                     (state/push-to-stack state/empty-state
;                                          stack-type
;                                          vect)
;                     :integer
;                     n)
;        end-state (vector/_take stack-type start-state)
;        expected-result (vec (take n vect))]
;    (= expected-result
;       (state/peek-stack end-state stack-type))))
;
;(gen-specs "take" check-take :vector :integer)
