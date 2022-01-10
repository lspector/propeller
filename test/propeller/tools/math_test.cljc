(ns propeller.tools.math-test
  (:require [clojure.test :as t]
            [propeller.tools.math :as m]))
(t/deftest not-lazy-test
  (t/is (= 1 (m/abs -1)))
  (t/is (= 1 (m/abs 1)))
  (t/is (= 0 (m/abs 0))))

(t/deftest approx=-test
  (t/is (m/approx= 3 5 2))
  (t/is (not (m/approx= 5 3 1.5)))
  (t/is (m/approx= -1 -5 9)))

(t/deftest ceil-test
  (t/is (= 5.0 (m/ceil 4.9)))
  (t/is (= 5.0 (m/ceil 4.5)))
  (t/is (= -10.0 (m/ceil -10.5 ))))

(t/deftest cos-test
  (t/is (= 1.0 (m/cos 0)))
  (t/is (= -1.0 (m/cos (* 3 m/PI)))))

(t/deftest div
  (t/is ))
