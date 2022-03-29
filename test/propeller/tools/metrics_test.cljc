(ns propeller.tools.metrics-test
  (:require [clojure.test :as t]
            [propeller.tools.metrics :as m]
            [propeller.tools.math :as a]))

(t/deftest argmax-test
  (t/is (= (m/argmax '(1 2 3 4))  3))
  (t/is (= 1 (nth '(1 1 1 1) (m/argmax '(1 1 1 1)))))
  (t/is (= 3 (nth '(1 3 3 1) (m/argmax '(1 3 3 1)))))
  (t/is (= 0 (m/argmax '(1)))))

(t/deftest mean-of-colls-test
  (t/is (= (m/mean-of-colls '((1 2 3 4) (4 3 2 1))) '(2.5 2.5 2.5 2.5)))
  (t/is (= (m/mean-of-colls '((1 2 3) (4 3 2 1))) '(2.5 2.5 2.5)))
  (t/is (= (m/mean-of-colls '((1))) '(1.0))))

(t/deftest min-of-colls-test
  (t/is (= (m/min-of-colls '((1 2 3 4) (4 3 2 1))) '(1 2 2 1)))
  (t/is (= (m/min-of-colls '((1 2 3) (4 3 2 1))) '(1 2 2)))
  (t/is (= (m/min-of-colls '((1))) '(1))))

(t/deftest mean-test
  (t/is (= (m/mean '(1 2 3 4)) 2.5))
  (t/is (= (m/mean '()) 0)))

(t/deftest median-test
  (t/is (= (m/median '(1 2 3 4 5)) 3))
  (t/is (= (m/median '(1 2 3 4)) 2.5))
  ;(t/is (= (m/median '()) 0.0))
  )

(t/deftest levenshtein-distance-test
  (t/is (= (m/levenshtein-distance "kitten" "sipping") 5))
  (t/is (= (m/levenshtein-distance "" "hello") 5)))

(t/deftest sequence-similarity-test
  (t/is (a/approx= (m/sequence-similarity "kitten" "sipping") 0.2857 0.001))
  (t/is (= (m/sequence-similarity "" "") 1)))
