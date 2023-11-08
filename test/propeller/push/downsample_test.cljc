(ns propeller.push.downsample-test
  (:require [clojure.test :as t]
            [propeller.utils :as u]
            [propeller.simplification :as s]
            [propeller.downsample :as ds]))


(t/deftest assign-indices-to-data-test
  (t/testing "assign-indices-to-data"
    (t/testing "should return a map of the same length"
      (t/is (= (count (ds/assign-indices-to-data (range 10) {})) 10))
      (t/is (= (count (ds/assign-indices-to-data (range 0) {})) 0)))
    (t/testing "should return a map where each element has an index key"
      (t/is (every? #(:index %) (ds/assign-indices-to-data (map #(assoc {} :input %) (range 10)) {}))))
    (t/testing "should return distinct indices"
      (t/is (= (map #(:index %) (ds/assign-indices-to-data (range 10) {})) (range 10))))))

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


(t/deftest ids-types-test
  (t/testing "replace-close-zero-with-zero"
    (t/testing "should replace the close to zero values with zero"
      (t/is (= (ds/replace-close-zero-with-zero '(0.1 2 3 4 0.1 2 3 4) 0.2) '(0 2 3 4 0 2 3 4)))
      (t/is (= (ds/replace-close-zero-with-zero '(0.1 0.1) 0.0) '(0.1 0.1)))
      (t/is (= (ds/replace-close-zero-with-zero '(100 100 200) 100) '(0 0 200))))))

(t/deftest update-case-distances-test
  (t/testing "update-case-distances"
    (t/testing "should update correctly when fewer errors than all"
      (t/is (= (ds/update-case-distances '({:errors (0 0)} {:errors (0 0)})
                                         '({:index 3 :distances [2 2 2 2 2]} {:index 4 :distances [2 2 2 2 2]})
                                         '({:index 0 :distances [2 2 2 2 2]} {:index 1 :distances [2 2 2 2 2]} {:index 2 :distances [2 2 2 2 2]} {:index 3 :distances [2 2 2 2 2]} {:index 4 :distances [2 2 2 2 2]})
                                         :solved)
               '({:index 0 :distances [2 2 2 2 2]} {:index 1 :distances [2 2 2 2 2]} {:index 2 :distances [2 2 2 2 2]}
                                                   {:index 3 :distances [2 2 2 0 0]} {:index 4 :distances [2 2 2 0 0]}))))
    (t/testing "should update correctly when same errors as all"
      (t/is (= (ds/update-case-distances '({:errors (0 0 0 0 0)} {:errors (0 0 0 0 0)})
                                         '({:index 0 :distances [2 2 2 2 2]} {:index 1 :distances [2 2 2 2 2]} {:index 2 :distances [2 2 2 2 2]} {:index 3 :distances [2 2 2 2 2]} {:index 4 :distances [2 2 2 2 2]})
                                         '({:index 0 :distances [2 2 2 2 2]} {:index 1 :distances [2 2 2 2 2]} {:index 2 :distances [2 2 2 2 2]} {:index 3 :distances [2 2 2 2 2]} {:index 4 :distances [2 2 2 2 2]})
                                         :solved)
               '({:index 0 :distances [0 0 0 0 0]} {:index 1 :distances [0 0 0 0 0]} {:index 2 :distances [0 0 0 0 0]}
                                                   {:index 3 :distances [0 0 0 0 0]} {:index 4 :distances [0 0 0 0 0]}))))
    (t/testing "should update correctly for elite/not-elite"
      (t/is (= (ds/update-case-distances '({:errors (1 1 1 2 2)} {:errors (2 2 2 1 1)})
                                         '({:index 0 :distances [2 2 2 2 2]} {:index 1 :distances [2 2 2 2 2]} {:index 2 :distances [2 2 2 2 2]} {:index 3 :distances [2 2 2 2 2]} {:index 4 :distances [2 2 2 2 2]})
                                         '({:index 0 :distances [2 2 2 2 2]} {:index 1 :distances [2 2 2 2 2]} {:index 2 :distances [2 2 2 2 2]} {:index 3 :distances [2 2 2 2 2]} {:index 4 :distances [2 2 2 2 2]})
                                         :elite)
               '({:index 0 :distances [0 0 0 2 2]} {:index 1 :distances [0 0 0 2 2]} {:index 2 :distances [0 0 0 2 2]}
                                                   {:index 3 :distances [2 2 2 0 0]} {:index 4 :distances [2 2 2 0 0]})))
      )))

(t/deftest case-maxmin-test
  (t/testing "case-maxmin selects correct downsample"
    (let [selected (ds/select-downsample-maxmin
                    '({:input1 [0] :output1 [10] :index 0 :distances [0 5 0 0 0]}
                      {:input1 [1] :output1 [11] :index 1 :distances [5 0 5 5 5]}
                      {:input1 [2] :output1 [12] :index 2 :distances [0 5 0 0 0]}
                      {:input1 [3] :output1 [13] :index 3 :distances [0 5 0 0 0]}
                      {:input1 [4] :output1 [14] :index 4 :distances [0 5 0 0 0]})
                    {:downsample-rate 0.4})]
      (prn {:selected selected})
      (t/is (or (= (:index (first selected)) 1) (= (:index (second selected)) 1))))))

(t/deftest case-maxmin-adaptive
  (t/testing "case-maxmin-adaptive selects correct downsample simple"
    (let [selected (ds/select-downsample-maxmin-adaptive
                    '({:input1 [0] :output1 [10] :index 0 :distances [0 5 0 0 0]}
                      {:input1 [1] :output1 [11] :index 1 :distances [5 0 5 5 5]}
                      {:input1 [2] :output1 [12] :index 2 :distances [0 5 0 0 0]}
                      {:input1 [3] :output1 [13] :index 3 :distances [0 5 0 0 0]}
                      {:input1 [4] :output1 [14] :index 4 :distances [0 5 0 0 0]})
                    {:case-delta 0})]
      (prn {:selected selected})
      (t/is (or (= (:index (first selected)) 1) (= (:index (second selected)) 1)))
      (t/is (= 2 (count selected))))) 
  (t/testing "case-maxmin-adaptive selects correct downsample when all identical"
    (let [selected (ds/select-downsample-maxmin-adaptive
                    '({:input1 [0] :output1 [10] :index 0 :distances [0 0 0 0 0]}
                      {:input1 [1] :output1 [11] :index 1 :distances [0 0 0 0 0]}
                      {:input1 [2] :output1 [12] :index 2 :distances [0 0 0 0 0]}
                      {:input1 [3] :output1 [13] :index 3 :distances [0 0 0 0 0]}
                      {:input1 [4] :output1 [14] :index 4 :distances [0 0 0 0 0]})
                    {:case-delta 0})]
      (prn {:selected selected})
      (t/is (= 1 (count selected))))))