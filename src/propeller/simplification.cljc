(ns propeller.simplification
  (:require [propeller.genome :as genome]
             [propeller.push.interpreter :as interpreter]
            [propeller.push.state :as state]
            [propeller.tools.math :as math]
            ))

(defn choose-random-k
  [k indices]
  (take k (shuffle indices)))

(defn delete-at-indices
  "deletes the values at given set of indices"
  [indices plushy]
  (let [sorted-indices (sort > indices)]
    (keep-indexed #(when (not (some #{%1} sorted-indices)) %2) plushy)))

(defn delete-k-random
  [k plushy]
  (delete-at-indices (choose-random-k k (range (count plushy))) plushy))

(defn auto-simplify-plushy
  "naive auto-simplification"
  [plushy error-function {:keys [simplification-steps training-data simplification-k simplification-verbose?] :as argmap}]
  (when simplification-verbose? (prn {:start-plushy-length (count plushy) :k simplification-k}))
  (let [initial-errors (:errors (error-function argmap training-data {:plushy plushy}))]
    (loop [step 0 curr-plushy plushy]
      (if (< simplification-steps step)
        (do (when simplification-verbose? (prn {:final-plushy-length (count curr-plushy) :final-plushy curr-plushy})) curr-plushy)
        (let [new-plushy (delete-k-random (rand-int simplification-k) curr-plushy)
              new-plushy-errors (:errors (error-function argmap training-data {:plushy new-plushy}))
              new-equal? (= new-plushy-errors initial-errors)]
          (recur (inc step)
                 (if new-equal? new-plushy curr-plushy)))))))