(ns propeller.tools.calculus-test
  (:require [clojure.test :as t]
            [propeller.tools.calculus :as c]))
(t/deftest deriv-test
  (t/is (letfn [(cubic [x] (let [a (double x)] (* a a a)))]
          (< (max (- (c/deriv cubic 2) 12) (- (- (c/deriv cubic 2) 12))) 0.001)))
  )

