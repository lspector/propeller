(ns propeller.utils-test
  (:require [clojure.test :as t]
            [propeller.utils :as u]
            [propeller.simplification :as s]
            [propeller.downsample :as ds]
            [propeller.hyperselection :as hs]))

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

(t/deftest assign-indices-to-data-test
  (t/testing "assign-indices-to-data"
    (t/testing "should return a map of the same length"
      (t/is (= (count (ds/assign-indices-to-data (range 10))) 10))
      (t/is (= (count (ds/assign-indices-to-data (range 0))) 0)))
    (t/testing "should return a map where each element has an index key"
      (t/is (every? #(:index %) (ds/assign-indices-to-data (map #(assoc {} :input %) (range 10))))))
    (t/testing "should return distinct indices"
      (t/is (= (map #(:index %) (ds/assign-indices-to-data (range 10))) (range 10))))))

(t/deftest select-downsample-random-test
  (t/testing "select-downsample-random"
    (t/testing "should select the correct amount of elements"
      (t/is (= (count (ds/select-downsample-random (range 10) {:downsample-rate 0.1})) 1))
      (t/is (= (count (ds/select-downsample-random (range 10) {:downsample-rate 0.2})) 2))
      (t/is (= (count (ds/select-downsample-random (range 10) {:downsample-rate 0.5})) 5)))
    (t/testing "should not return duplicate items (when called with set of numbers)"
      (t/is (= (count (set (ds/select-downsample-random (range 10) {:downsample-rate 0.1}))) 1))
      (t/is (= (count (set (ds/select-downsample-random (range 10) {:downsample-rate 0.2}))) 2))
      (t/is (= (count (set (ds/select-downsample-random (range 10) {:downsample-rate 0.5}))) 5)))
    (t/testing "should round down the number of elements selected if not whole"
      (t/is (= (count (ds/select-downsample-random (range 3) {:downsample-rate 0.5})) 1))
      (t/is (= (count (ds/select-downsample-random (range 1) {:downsample-rate 0.5})) 0)))
    (t/testing "should not return more elements than available"
      (t/is (= (count (ds/select-downsample-random (range 10) {:downsample-rate 2})) 10))
      (t/is (= (count (ds/select-downsample-random (range 10) {:downsample-rate 1.5})) 10)))))

(t/deftest get-distance-between-cases-test
  (t/testing "get-distance-between-cases"
    (t/testing "should return correct distance"
      (t/is (= 3 (ds/get-distance-between-cases '((0 1 1) (0 1 1) (1 0 1)) 0 1))))
    (t/testing "should return 0 for the distance of a case to itself"
      (t/is (= 0 (ds/get-distance-between-cases '((0 1 1) (0 1 1) (1 0 1)) 0 0))))
    (t/testing "should work for non binary values (0 is solved)"
      (t/is (= 1 (ds/get-distance-between-cases '((0 2 2) (0 2 2) (1 0 50)) 1 2))))
    (t/testing "should return the max distance if one of the cases does not exist"
      (t/is (= 3 (ds/get-distance-between-cases '((0 1 1) (0 1 1) (1 0 1)) 0 4))))))

(t/deftest merge-map-lists-at-index-test
  (t/testing "merge-map-lists-at-index"
    (t/testing "works properly"
      (t/is (= '({:index 0 :a 3 :b 2} {:index 1 :a 2 :b 3}) (ds/merge-map-lists-at-index '({:index 0 :a 3 :b 2} {:index 1 :a 1 :b 2}) '({:index 1 :a 2 :b 3})))))
    (t/testing "doesn't change big list if no indices match"
      (t/is (= '({:index 0 :a 3 :b 2} {:index 1 :a 1 :b 2}) (ds/merge-map-lists-at-index '({:index 0 :a 3 :b 2} {:index 1 :a 1 :b 2}) '({:index 3 :a 2 :b 3})))))
    (t/testing "doesn't fail on empty list"
      (t/is (= '() (ds/merge-map-lists-at-index '() '()))))
    (t/testing "shouldn't fail merging non-empty with empty"
      (t/is (= '({:index 0 :a 3 :b 2} {:index 1 :a 1 :b 2}) (ds/merge-map-lists-at-index '({:index 0 :a 3 :b 2} {:index 1 :a 1 :b 2}) '()))))))

(t/deftest update-at-indices-test
  (t/testing "update-at-indices"
    (t/testing "should update at correct indices"
      (t/is (= (ds/update-at-indices [1 2 3 4] [5] [0]) [5 2 3 4]))
      (t/is (= (ds/update-at-indices [1 2 3 4] [5] [0]) [5 2 3 4])))
    (t/testing "should update nothing if index list is empty"
      (t/is (= (ds/update-at-indices [6 5 4 0 0] [] []) [6 5 4 0 0])))
    (t/testing "should update nothing if index list is out of bounds"
      (t/is (= (ds/update-at-indices [6 5 4 0 0] [4 5 1] [-1 5 6]) [6 5 4 0 0])))
    (t/testing "should update only when indices are available (length mismatch)"
      (t/is (= (ds/update-at-indices [6 5 4 0 0] [1 2 3 4] [0 1]) [1 2 4 0 0])))
    (t/testing "should not care about index order"
      (t/is (= (ds/update-at-indices [6 5 4 0 0] [2 1] [1 0]) [1 2 4 0 0])))
    (t/testing "should work when input is a list"
      (t/is (= (ds/update-at-indices '(6 5 4 0 0) '(2 1) '(1 0)) [1 2 4 0 0])))))

(t/deftest update-case-distances-test
  (t/testing "update-case-distances"
    (t/testing "should ..."
      (t/is (= (ds/update-case-distances '({:errors (0 0)} {:errors (0 0)})
                                         '({:index 3 :distances [2 2 2 2 2]} {:index 4 :distances [2 2 2 2 2]})
                                         '({:index 0 :distances [2 2 2 2 2]} {:index 1 :distances [2 2 2 2 2]} {:index 2 :distances [2 2 2 2 2]} {:index 3 :distances [2 2 2 2 2]} {:index 4 :distances [2 2 2 2 2]}))
               '({:index 0 :distances [2 2 2 2 2]} {:index 1 :distances [2 2 2 2 2]} {:index 2 :distances [2 2 2 2 2]}
                                                   {:index 3 :distances [2 2 2 0 0]} {:index 4 :distances [2 2 2 0 0]}))))))

(t/deftest case-maxmin-test
  (t/testing "case-maxmin selects correct downsample"
    (let [selected (ds/select-downsample-maxmin
                    '({:input1 [0] :output1 [10] :index 0 :distances [0 5 0 0 0]}
                      {:input1 [1] :output1 [11] :index 1 :distances [5 0 5 5 5]}
                      {:input1 [2] :output1 [12] :index 2 :distances [0 5 0 0 0]}
                      {:input1 [3] :output1 [13] :index 3 :distances [0 5 0 0 0]}
                      {:input1 [4] :output1 [14] :index 4 :distances [0 5 0 0 0]})
                    {:downsample-rate 0.4 :case-t-size 5})]
      (prn {:selected selected})
      (t/is (or (= (:index (first selected)) 1) (= (:index (second selected)) 1))))))


(t/deftest hyperselection-test
  (let [parents1 '({:blah 3 :index 1} {:blah 3 :index 1}
                   {:blah 3 :index 1} {:blah 3 :index 2})
        parents2 '({:plushy 2 :index 0} {:blah 3 :index 2}
                   {:blah 3 :index 3} {:index 4})
        emptyparents '({:blah 1} {:blah 1} {:blah 1})]
    (t/testing "sum-list-map-indices function works correctly"
      (t/is (= {1 3, 2 1} (hs/sum-list-map-indices parents1)))
      (t/is (= {0 1, 2 1, 3 1, 4 1} (hs/sum-list-map-indices parents2))))
    (t/testing "ordered-freqs function works correctly"
      (t/is (= '(3 1) (hs/ordered-freqs (hs/sum-list-map-indices parents1))))
      (t/is (= '(1 1 1 1) (hs/ordered-freqs (hs/sum-list-map-indices parents2)))))
    (t/testing "hyperselection-track works correctly"
      (t/is (= '(0.75 0.25) (hs/hyperselection-track parents1)))
      (t/is (= '(0.25 0.25 0.25 0.25) (hs/hyperselection-track parents2))))
    (t/testing "reindex-pop works correctly"
      (t/is (= '({:blah 3 :index 0} {:blah 3 :index 1}
                 {:blah 3 :index 2} {:blah 3 :index 3}) (hs/reindex-pop parents1)))
      (t/is (= '({:plushy 2 :index 0} {:blah 3 :index 1}
                 {:blah 3 :index 2} {:index 3}) (hs/reindex-pop parents2)))
      (t/is (= '({:blah 1 :index 0} {:blah 1 :index 1} {:blah 1 :index 2}) (hs/reindex-pop emptyparents))))))
