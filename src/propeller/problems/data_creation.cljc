(ns propeller.problems.data-creation
  (:require [psb2.core :as psb2]
            [clojure.data.csv :as csv]
            [clojure.java.io :as io]))

(def problem "fizz-buzz")

(defn generate-data [problem train-or-test]
  (let [train-and-test-data (psb2/fetch-examples "data" problem 200 1000)
        cleaned-data (map #(vector (:input1 %) (:output1 %)) ((keyword train-or-test) train-and-test-data))]
    (with-open [writer (io/writer (str problem "-" train-or-test ".csv"))]
      (csv/write-csv writer
                     (doall cleaned-data)))))

(defn generate-data-for-problem [problem]
  (map (partial generate-data problem) '["test" "train"]))

(defn generate-data-for-all-problems []
  (map (partial generate-data-for-problem) '["fuel-cost"
                                 "fizz-buzz"
                                 "gcd"
                                 "find-pair"
                                 "small-or-large"
                                 "scrabble-score"
                                 "grade"
                                 "count-odds"]))

(generate-data-for-all-problems)