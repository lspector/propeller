(ns propeller.selection-test
  (:require [clojure.test :as t]
            [propeller.selection :as s]))


(t/deftest roulette-selection-test
  (t/testing "fitness proportionate selection"
    (t/testing "should correctly define the probabilities of selection"
      (t/is (let [ret (s/fitness-proportionate-selection '({:index 0 :total-error 0}
                                                           {:index 1 :total-error 1}
                                                           {:index 2 :total-error 1}
                                                           {:index 3 :total-error 1}) {:empty :argmap})]
              (case (:index ret)
                0 (= (:fitness ret) 1) ;if we selected index 0, check that fitness is correctly calculated to 1
                (= (float (:fitness ret)) 0.5)
                ))))
    (t/testing "should always return the same individual if there is only one"
      (t/testing "desipte it having bad error"
        (t/is (= (:index (s/fitness-proportionate-selection '({:index 99 :total-error 109012390123}) {:empty :argmap}))
                 99)))
      (t/testing "when it has low error"
        (t/is (= (:index (s/fitness-proportionate-selection '({:index 22 :total-error 0}) {:empty :argmap}))
                 22))))))