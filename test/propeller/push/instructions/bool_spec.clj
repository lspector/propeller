(ns propeller.push.instructions.bool-spec
  (:require
    ;[clojure.boolean :as bool]
    [clojure.test.check.generators :as gen]
    [clojure.test.check.properties :as prop]
    [clojure.test.check.clojure-test :as ct :refer [defspec]]
    [propeller.push.state :as state]
    [propeller.push.instructions :as instructions]
    [propeller.push.instructions.bool :as boolean-instructions]
    [propeller.push.interpreter :as interpreter]))

;;boolean/and
(defn check-and
  [value1 value2]
  (let [start-state (-> state/empty-state
                        (state/push-to-stack :boolean value1)
                        (state/push-to-stack :boolean value2))
        end-state ((:boolean_and @instructions/instruction-table) start-state)
        expected-result (and value1 value2)]
    (= expected-result
       (state/peek-stack end-state :boolean))))
(defspec and-spec 100
         (prop/for-all [bool1 gen/boolean
                        bool2 gen/boolean]
                       (check-and bool1 bool2)))

;; boolean/or
(defn check-or
  [value1 value2]
  (let [start-state (-> state/empty-state
                        (state/push-to-stack :boolean value1)
                        (state/push-to-stack :boolean value2))
        end-state ((:boolean_or @instructions/instruction-table) start-state)
        expected-result (or value1 value2)]
    (= expected-result
       (state/peek-stack end-state :boolean))))

(defspec or-spec 100
         (prop/for-all [bool1 gen/boolean
                        bool2 gen/boolean]
                       (check-or bool1 bool2)))

;; boolean/not
(defn check-not
  [value1]
  (let [start-state (-> state/empty-state
                        (state/push-to-stack :boolean value1))
        end-state ((:boolean_not @instructions/instruction-table) start-state)
        expected-result (not value1)]
    (= expected-result
       (state/peek-stack end-state :boolean))))

(defspec not-spec 100
         (prop/for-all [bool1 gen/boolean]
                       (check-not bool1)))

;; boolean/xor
(defn xor [bool1 bool2]
  (not (= bool1 bool2)))

(defn check-xor
  [value1 value2]
  (let [start-state (-> state/empty-state
                        (state/push-to-stack :boolean value1)
                        (state/push-to-stack :boolean value2))
        end-state ((:boolean_xor @instructions/instruction-table) start-state)
        expected-result (xor value1 value2)]
    (= expected-result
       (state/peek-stack end-state :boolean))))

(defspec xor-spec 100
         (prop/for-all [bool1 gen/boolean
                        bool2 gen/boolean]
                       (check-xor bool1 bool2)))

;; boolean/invert-first-then-and
(defn check-invert-first-then-and
  [value1 value2]
  (let [start-state (-> state/empty-state
                        (state/push-to-stack :boolean value1)
                        (state/push-to-stack :boolean value2))
        end-state ((:boolean_invert_first_then_and @instructions/instruction-table) start-state)
        expected-result (and (not value1) value2)]
    (= expected-result
       (state/peek-stack end-state :boolean))))

(defspec invert-first-then-and-spec 100
         (prop/for-all [bool1 gen/boolean
                        bool2 gen/boolean]
                       (check-invert-first-then-and bool1 bool2)))

;; boolean/invert-second-then-and
(defn check-invert-second-then-and
  [value1 value2]
  (let [start-state (-> state/empty-state
                        (state/push-to-stack :boolean value1)
                        (state/push-to-stack :boolean value2))
        end-state ((:boolean_invert_second_then_and @instructions/instruction-table) start-state)
        expected-result (and  value1 (not value2))]
    (= expected-result
       (state/peek-stack end-state :boolean))))

(defspec invert-second-then-and-spec 100
         (prop/for-all [bool1 gen/boolean
                        bool2 gen/boolean]
                       (check-invert-second-then-and bool1 bool2)))

;;  boolean/from-float
(defn check-boolean-from-float
  [value1]
  (let [start-state (-> state/empty-state
                        (state/push-to-stack :float value1))
        end-state ((:boolean_from_float @instructions/instruction-table) start-state)
        expected-result (not (= value1 0.0))]
    (= expected-result
       (state/peek-stack end-state :boolean))))

(defspec boolean-from-integer-spec 100
         (prop/for-all [int1 gen/double]
                       (check-not int1)))
;;  boolean/from-integer
(defn check-boolean-from-float
  [value1]
  (let [start-state (-> state/empty-state
                        (state/push-to-stack :integer value1))
        end-state ((:boolean_from_integer @instructions/instruction-table) start-state)
        expected-result (not (= value1 0))]
    (= expected-result
       (state/peek-stack end-state :boolean))))

(defspec boolean-from-integer-spec 100
         (prop/for-all [integer1 gen/double]
                       (check-not integer1)))

