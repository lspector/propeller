(ns propeller.push.utils.limits-test
  (:require [clojure.test :as t]
            [propeller.push.utils.limits :as l]))

(t/deftest limit-number-test
  (t/is (= (l/limit-number (inc l/max-number-magnitude))
           l/max-number-magnitude))
  (t/is (l/limit-number 1.0E-10)
        l/min-number-magnitude))

(t/deftest limit-string-test
  (t/is (= (l/limit-string (apply str (repeat (inc l/max-string-length) "!")))
           (apply str (repeat l/max-string-length "!")))))

(t/deftest limit-vector-test
  (t/is (= (l/limit-vector (vec (repeat (inc l/max-vector-length) true)))
           (vec (repeat l/max-vector-length true)))))

(t/deftest limit-code-test
  (binding [l/max-code-points 8]
    (t/is (= (l/limit-code '(:a (:b (:c) :d :e :f) :g :h))
             '()))
    (t/is (= (l/limit-code '(:a :b :c))
             '(:a :b :c))))
  (binding [l/max-code-depth 2]
    (t/is (= (l/limit-code '(:a (:b (:c) :d :e :f) :g :h))
             '()))
    (t/is (= (l/limit-code '(:a :b :c))
             '(:a :b :c)))))
