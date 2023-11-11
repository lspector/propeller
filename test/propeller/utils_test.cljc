(ns propeller.utils-test
  (:require [clojure.test :as t]
            [propeller.utils :as u]
            [propeller.simplification :as s]
            [propeller.downsample :as ds]))

(t/deftest first-non-nil-test
  (t/is (= 1 (u/first-non-nil '(1 2 3))))
  (t/is (= nil (u/first-non-nil [])))
  (t/is (= nil (u/first-non-nil []))))

(t/deftest indexof-test
  (t/is (= 2 (u/indexof :b '(:z :y :b :q :w))))
  (t/is (= 1 (u/indexof :b '(:a :b :c :b :a))))
  (t/is (= -1 (u/indexof :b '(:a :c :q :r)))))

(t/deftest not-lazy-test
  (t/is (= '(:a :b :c) (u/not-lazy [:a :b :c])))
  (t/is (= '(:a :b :c) (u/not-lazy '(:a :b :c)))))
(t/deftest ensure-list-test
  (t/is (= '(0 1 2 3 4) (u/ensure-list (range 5))))
  (t/is (= '([:a :b :c]) (u/ensure-list [:a :b :c]))))

(t/deftest random-instruction-test
  (t/is
   (letfn [(instruct [] 1)]
     (let [test (u/random-instruction [instruct 2])]
       (if (= 1 test)
         true
         (= 2 test))))))

(t/deftest count-points-test
  (t/is (= 6 (u/count-points '(:a :b (:c :d)))))
  (t/is (= 1 (u/count-points '())))
  (t/is (= 2 (u/count-points '(:a)))))

(t/deftest choose-random-k-test
  (t/testing "choose-random-k"
    (t/testing "should return indices that are a member of the original array"
      (t/is (every? identity (map #(contains? (set (range 10)) %) (s/choose-random-k 3 (range 10))))))
    (t/testing "should return a list of size k"
      (t/is (= (count (s/choose-random-k 7 (range 10))) 7)))))


(t/deftest delete-at-indices-test
  (t/testing "delete-at-indices"
    (t/testing "should actually remove indicated items"
      (t/is (= '(:hi1 :hi2) (s/delete-at-indices '(0 3) '(:hi0 :hi1 :hi2 :hi3)))))
    (t/testing "should work with numerical indices"
      (t/is (= '(:hi1 :hi2 :hi3) (s/delete-at-indices '(0) '(:hi0 :hi1 :hi2 :hi3)))))
    (t/testing "should not delete anything for index out of bounds"
      (t/is (= '(:hi1 :hi2 :hi3) (s/delete-at-indices '(0 10) '(:hi0 :hi1 :hi2 :hi3))))
      (t/is (= '(:hi1 :hi2 :hi3) (s/delete-at-indices '(0 -10) '(:hi0 :hi1 :hi2 :hi3))))
      (t/is (= '(:hi1 :hi2 :hi3) (s/delete-at-indices '(-0 -10) '(:hi0 :hi1 :hi2 :hi3)))))
    (t/testing "should only delete at single index once"
      (t/is (= '(:hi1 :hi2) (s/delete-at-indices '(0 0 0 0 3 3 3) '(:hi0 :hi1 :hi2 :hi3)))))
    (t/testing "should return empty list when deleting from empty list"
      (t/is (= '() (s/delete-at-indices '(0) '()))))
    (t/testing "should be able to delete at arbitrary indices"
      (t/is (= (count (s/delete-at-indices (s/choose-random-k 3 (range 10)) (range 10))) 7)))))

(t/deftest delete-random-k-test
  (t/testing "delete-random-k"
    (t/testing "should remove the correct amount of items"
      (t/is (= (count (s/delete-k-random 3 (range 10))) 7))
      (t/is (= (count (s/delete-k-random 10 (range 10))) 0))
      (t/is (= (count (s/delete-k-random 0 (range 10))) 10)))
    (t/testing "should not fail if k >> size of collection"
      (t/is (= (count (s/delete-k-random 300 (range 10))) 0))
      (t/is (= (s/delete-k-random 300 '(:hi1 :hi2 :hi3)) '())))
    (t/testing "should not fail if the collection is empty"
      (t/is (= (count (s/delete-k-random 300 '())) 0))
      (t/is (= (count (s/delete-k-random 0 '())) 0)))
    (t/testing "should maintain order of the remaining items"
      (t/is (apply < (s/delete-k-random 3 (range 10)))))))

(t/deftest auto-simplify-plushy-test
  (t/testing "auto-simplify-plushy"
    (t/testing "should handle having an empty plushy"
      (t/is (= (s/auto-simplify-plushy '() (fn [argmap data plushy] 0) {:simplification-steps 100 :simplification-k 4 :simplification-verbose? false}) '())))
    (let [plushy '(:exec_dup 1 :integer_add close :in1 :integer_add 0 :in1 :in1 :integer_mult :integer_add)]
      (t/testing "should decrease size of plushy that always has perfect scores"
        (t/is (< (count (s/auto-simplify-plushy plushy (fn [argmap data plushy] 0) {:simplification-steps 100 :simplification-k 4 :simplification-verbose? false})) (count plushy)))
        (t/is (< (count (s/auto-simplify-plushy plushy (fn [argmap data plushy] 0) {:simplification-steps 100 :simplification-k 10 :simplification-verbose? false})) (count plushy)))))))