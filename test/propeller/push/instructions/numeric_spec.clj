(ns propeller.push.instructions.numeric-spec
  (:require
    [clojure.test.check.generators :as gen]
    [clojure.test.check.properties :as prop]
    [clojure.test.check.clojure-test :as ct :refer [defspec]]
    [propeller.push.state :as state]
    [propeller.push.instructions :as instructions]
    [propeller.tools.math :as m]
    [propeller.tools.character :as c]
    [propeller.push.instructions.numeric :as numeric-instructions]
    [propeller.push.interpreter :as interpreter]))

(defn check-integer-gt
  [value1 value2]
  (let [start-state (-> state/empty-state
                        (state/push-to-stack :integer value1)
                        (state/push-to-stack :integer value2))
        end-state ((:integer_gt @instructions/instruction-table) start-state)
        expected-result (> value1 value2)]
    (= expected-result
       (state/peek-stack end-state :boolean))))
(defspec integer-gt-spec 100
         (prop/for-all [int1 gen/small-integer
                        int2 gen/small-integer]
                       (check-integer-gt int1 int2)))

;(defn check-float-gt
;  [value1 value2]
;  (let [start-state (-> state/empty-state
;                        (state/push-to-stack :float value1)
;                        (state/push-to-stack :float value2))
;        end-state ((:float_gt @instructions/instruction-table) start-state)
;        expected-result (> value1 value2)]
;    (println "Value 1:" value1)
;    (println "Value 2:" value2)
;    (println "Expected Result: " expected-result)
;    (println "Actual Result: "(state/peek-stack end-state :boolean))
;    (println "")
;    (= expected-result
;               (state/peek-stack end-state :boolean))))


;(defspec float-gt-spec 100
;         (prop/for-all [float1 (gen/double* {:infinite? false, :NaN? false, :min -1000000, :max 1000000})
;                        float2 (gen/double* {:infinite? false, :NaN? false, :min -1000000, :max 1000000})]
;                     (if (and (> (m/abs float1) 0.00001) (> (m/abs float2) 0.00001))
;                       (check-float-gt float1 float2))))

(defn check-integer-add
  [value1 value2]
  (let [start-state (-> state/empty-state
                        (state/push-to-stack :integer value1)
                        (state/push-to-stack :integer value2))
        end-state ((:integer_add @instructions/instruction-table) start-state)
        expected-result (+ value1 value2)]
    (= expected-result
       (state/peek-stack end-state :integer))))
(defspec integer-add-spec 100
         (prop/for-all [int1 gen/small-integer
                        int2 gen/small-integer]
                       (check-integer-add int1 int2)))

(defn check-float-add
  [value1 value2]
  (let [start-state (-> state/empty-state
                        (state/push-to-stack :float value1)
                        (state/push-to-stack :float value2))
        end-state ((:float_add @instructions/instruction-table) start-state)
        expected-result (+ value1 value2)]
    (m/approx= expected-result
       (state/peek-stack end-state :float) 0.0001)))

(defspec float-add-spec 100
         (prop/for-all [float1 (gen/double* {:infinite? false, :NaN? false, :min (/ -1000000 2), :max (/ 1000000 2)})
                        float2 (gen/double* {:infinite? false, :NaN? false, :min (/ -1000000 2), :max (/ 1000000 2)})]
                       (check-float-add float1 float2)))


(defn check-integer-subtract
  [value1 value2]
  (let [start-state (-> state/empty-state
                        (state/push-to-stack :integer value1)
                        (state/push-to-stack :integer value2))
        end-state ((:integer_subtract @instructions/instruction-table) start-state)
        expected-result (- value1 value2)]
    (= expected-result
       (state/peek-stack end-state :integer))))

(defspec integer-subtract-spec 100
         (prop/for-all [int1 gen/small-integer
                        int2 gen/small-integer]
                       (check-integer-subtract int1 int2)))

(defn check-float-subtract
  [value1 value2]
  (let [start-state (-> state/empty-state
                        (state/push-to-stack :float value1)
                        (state/push-to-stack :float value2))
        end-state ((:float_subtract @instructions/instruction-table) start-state)
        expected-result (- value1 value2)]
    (m/approx= expected-result
               (state/peek-stack end-state :float) 0.0001)))

(defspec float-subtract-spec 100
         (prop/for-all [float1 (gen/double* {:infinite? false, :NaN? false, :min  (/ -1000000 2), :max (/ 1000000 2)})
                        float2 (gen/double* {:infinite? false, :NaN? false, :min  (/ -1000000 2), :max (/ 1000000 2)})]
                       (check-float-subtract float1 float2)))

(defn check-integer-mult
  [value1 value2]
  (let [start-state (-> state/empty-state
                        (state/push-to-stack :integer value1)
                        (state/push-to-stack :integer value2))
        end-state ((:integer_mult @instructions/instruction-table) start-state)
        expected-result (* value1 value2)]
    (= expected-result
       (state/peek-stack end-state :integer))))

(defspec integer-mult-spec 100
         (prop/for-all [int1 gen/small-integer
                        int2 gen/small-integer]
                       (check-integer-mult int1 int2)))


(defn check-float-mult
  [value1 value2]
  (let [start-state (-> state/empty-state
                        (state/push-to-stack :float value1)
                        (state/push-to-stack :float value2))
        end-state ((:float_mult @instructions/instruction-table) start-state)
        expected-result (* value1 value2)]
    (m/approx= expected-result
               (state/peek-stack end-state :float) 0.0001)))

(defspec float-mult-spec 100
         (prop/for-all [float1 (gen/double* {:infinite? false, :NaN? false, :min (* -1 (m/sqrt 1000000)), :max (m/sqrt 1000000)})
                        float2 (gen/double* {:infinite? false, :NaN? false, :min (* -1 (m/sqrt 1000000)), :max (m/sqrt 1000000)})]
                       (check-float-mult float1 float2)))

(defn check-integer-quot
  [value1 value2]
  (if (= value2 0)
    true
  (let [start-state (-> state/empty-state
                        (state/push-to-stack :integer value1)
                        (state/push-to-stack :integer value2))
        end-state ((:integer_quot @instructions/instruction-table) start-state)
        expected-result (quot value1 value2)]
    (= expected-result
       (state/peek-stack end-state :integer )))))

(defspec integer-quot-spec 100
         (prop/for-all [int1 gen/small-integer
                        int2 gen/small-integer]
                       (check-integer-quot int1 int2)))

(defn check-float-quot
  [value1 value2]
  (if (= value2 0.0)
    true
  (let [start-state (-> state/empty-state
                        (state/push-to-stack :float value1)
                        (state/push-to-stack :float value2))
        end-state ((:float_quot @instructions/instruction-table) start-state)
        expected-result (quot value1 value2)]
    (= expected-result
               (state/peek-stack end-state :float)))))

(defspec float-quot-spec 100
         (prop/for-all [float1 (gen/double* {:infinite? false, :NaN? false, :min   -1000000, :max 1000000})
                        float2 (gen/double* {:infinite? false, :NaN? false, :min   1.0, :max 1000000})]
                       (check-float-quot float1 float2)))

(defn check-integer-mod
  [value1 value2]
  (if (= value2 0)
   true
  (let [start-state (-> state/empty-state
                        (state/push-to-stack :integer value1)
                        (state/push-to-stack :integer value2))
        end-state ((:integer_mod @instructions/instruction-table) start-state)
        expected-result (mod value1 value2)]
    (= expected-result
       (state/peek-stack end-state :integer)))))

(defspec integer-mod-spec 100
         (prop/for-all [int1 gen/small-integer
                        int2 gen/small-integer]
                         (check-integer-mod int1 int2)))

(defn check-float-mod
  [value1 value2]
  (if (m/approx= value1 0.0 0.00001)
    true
    (let [start-state (-> state/empty-state
                          (state/push-to-stack :float value1)
                          (state/push-to-stack :float value2))
          end-state ((:float_mod @instructions/instruction-table) start-state)
          expected-result (mod value1 value2)]
      (m/approx= expected-result
         (state/peek-stack end-state :float) 0.001))))


(defspec float-mod-spec 100
         (prop/for-all [float1 (gen/double* {:infinite? false, :NaN? false, :min   -1000000, :max 1000000})
                        float2 (gen/double* {:infinite? false, :NaN? false, :min   1.0, :max 1000000})]
                       (check-float-mod float1 float2)))
(defn check-integer-max
  [value1 value2]
  (let [start-state (-> state/empty-state
                        (state/push-to-stack :integer value1)
                        (state/push-to-stack :integer value2))
        end-state ((:integer_max @instructions/instruction-table) start-state)
        expected-result (max value2 value1)]
    (= expected-result
       (state/peek-stack end-state :integer))))

(defspec integer-max-spec 100
         (prop/for-all [int1 gen/small-integer
                        int2 gen/small-integer]
                       (check-integer-max int1 int2)))

(defn check-float-max
  [value1 value2]
  (let [start-state (-> state/empty-state
                        (state/push-to-stack :float value1)
                        (state/push-to-stack :float value2))
        end-state ((:float_max @instructions/instruction-table) start-state)
        expected-result (max value1 value2)]
    (m/approx= expected-result
               (state/peek-stack end-state :float) 0.0001)))

(defspec float-max-spec 100
         (prop/for-all [float1 (gen/double* {:infinite? false, :NaN? false, :min -1000000, :max 1000000})
                        float2 (gen/double* {:infinite? false, :NaN? false, :min -1000000, :max 1000000})]
                       (check-float-max float1 float2)))

(defn check-integer-min
  [value1 value2]
  (let [start-state (-> state/empty-state
                        (state/push-to-stack :integer value1)
                        (state/push-to-stack :integer value2))
        end-state ((:integer_min @instructions/instruction-table) start-state)
        expected-result (min value2 value1)]
    (= expected-result
       (state/peek-stack end-state :integer))))

(defspec integer-min-spec 100
         (prop/for-all [int1 gen/small-integer
                        int2 gen/small-integer]
                       (check-integer-min int1 int2)))
(defn check-float-min
  [value1 value2]
  (let [start-state (-> state/empty-state
                        (state/push-to-stack :float value1)
                        (state/push-to-stack :float value2))
        end-state ((:float_min @instructions/instruction-table) start-state)
        expected-result (min value1 value2)]
    (m/approx= expected-result
               (state/peek-stack end-state :float) 0.0001)))

(defspec float-min-spec 100
         (prop/for-all [float1 (gen/double* {:infinite? false, :NaN? false, :min -1000000, :max 1000000})
                        float2 (gen/double* {:infinite? false, :NaN? false, :min -1000000, :max 1000000})]
                       (check-float-min float1 float2)))

(defn check-integer-from-boolean
  [value1]
  (let [start-state (-> state/empty-state
                        (state/push-to-stack :boolean value1))
        end-state ((:integer_from_boolean @instructions/instruction-table) start-state)
        expected-result (if value1
                           1
                           0)]
    (= expected-result
       (state/peek-stack end-state :integer))))

(defspec integer-from-boolean-spec 100
         (prop/for-all [bool1 gen/boolean]
                       (check-integer-from-boolean bool1)))

(defn check-float-from-boolean
  [value1]
  (let [start-state (-> state/empty-state
                        (state/push-to-stack :boolean value1))
        end-state ((:float_from_boolean @instructions/instruction-table) start-state)
        expected-result (if value1
                          1.0
                          0.0)]
    (= expected-result
       (state/peek-stack end-state :float))))

(defspec float-from-boolean-spec 100
         (prop/for-all [bool1 gen/boolean]
                       (check-float-from-boolean bool1)))


(defn check-integer-from-char
  [value1]
  (let [start-state (-> state/empty-state
                        (state/push-to-stack :char value1))
        end-state ((:integer_from_char @instructions/instruction-table) start-state)
        expected-result (c/get-ascii value1)]
    (= expected-result
       (state/peek-stack end-state :integer))))

(defspec integer-from-char-spec 100
         (prop/for-all [char1 gen/char]
                       (check-integer-from-char char1)))

;(defn check-float-from-char
;  [value1]
;  (let [start-state (-> state/empty-state
;                        (state/push-to-stack :char value1))
;        end-state ((:float_from_char @instructions/instruction-table) start-state)
;        expected-result (float (c/get-ascii value1))]
;    (= expected-result
;       (state/peek-stack end-state :float))))
;
;(defspec float-from-char-spec 100
;         (prop/for-all [char1 gen/char]
;                       (check-float-from-char char1)))


;
;  (println "Expected Result: " expected-result)
;      (println "Actual Result: "(state/peek-stack end-state :float))
;      (println "Value 1:" value1)
;      (println "Value 2:" value2)
;
;
