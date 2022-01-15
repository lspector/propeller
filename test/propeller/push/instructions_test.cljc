(ns propeller.push.instructions-test
  (:require [clojure.test :as t]
            [propeller.push.instructions :as h]))

(t/deftest get-literal-type-test
  (t/is (= (h/get-literal-type "abc") :string))
  (t/is (= (h/get-literal-type [1]) :vector_integer))
  (t/is (= (h/get-literal-type false) :boolean))
  (t/is (= (h/get-literal-type 0.0) #?(:clj :float :cljs :integer)))
)
(t/deftest get-vector-literal-type-test
  (t/is (= (h/get-vector-literal-type :vector_integer) :integer))
  (t/is (= (h/get-vector-literal-type :vector_boolean) :boolean))
  (t/is (= (h/get-vector-literal-type :vector_float) :float))
  (t/is (= (h/get-vector-literal-type :vector_string)) :string)
  )
;(t/deftest def-instruction-test
;  (t/is ))
