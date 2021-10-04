(ns propeller.push.utils.helpers-test
  (:require [clojure.test :as t]
            [propeller.push.utils.helpers :as h]))

(t/deftest get-literal-type-test
  (t/is (= (h/get-literal-type "abc") :string))
  (t/is (= (h/get-literal-type [1]) :vector_integer))
  (t/is (= (h/get-literal-type false) :boolean))
  (t/is (= (h/get-literal-type 0.0) #?(:clj :float :cljs :integer))))
