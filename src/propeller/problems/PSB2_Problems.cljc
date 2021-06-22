(ns propeller.problems.PSB2-Problems
  (:require [psb2.core :as psb2]))

;; Get path from text file
(def PSB2-path (slurp "PSB2_path.txt"))

(defn train-and-test
  "Returns the train and test sets from PSB2 using Prof. Helmuth's function"
  ;; Default to 200 train and 2000 test
  [problem]
  (psb2/fetch-examples PSB2-path problem 200 2000)
  ;; To with 2 extra args, can customise train and test set sizes
  [problem train test]
  (psb2/fetch-examples PSB2-path problem train test))

