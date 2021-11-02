(ns propeller.utils-test
  (:require [clojure.test :as t]
            [propeller.utils :as u]))

(t/deftest count-points-test
  (t/is (= 6 (u/count-points '(:a :b (:c :d)))))
  (t/is (= 1 (u/count-points '())))
  (t/is (= 2 (u/count-points '(:a)))))
