(ns propeller.push.instructions-test
  (:require [clojure.test :as t]
            [propeller.push.instructions :as h]))

(t/deftest get-literal-type-test
  (t/is (= (h/get-literal-type "abc") :string))
  (t/is (= (h/get-literal-type [1]) :vector_integer))
  (t/is (= (h/get-literal-type false) :boolean))
  (t/is (= (h/get-literal-type 0.0) #?(:clj :float :cljs :integer))))
