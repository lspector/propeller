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
    (keep-indexed #(if (not (some #{%1} sorted-indices)) %2) plushy)))

(defn delete-k-random
  [k plushy]
  (delete-at-indices (choose-random-k k (range (count plushy))) plushy))

(defn auto-simplify-plushy
  "naive auto-simplification with simple k annealing"
  [argmap plushy steps error-function training-data start-k decrease-k-on-failure-prob verbose?]
  (if verbose? (prn "Starting Auto-Simplification" {:current-plushy-length (count plushy) :k start-k :decrease-k-on-failure-prob decrease-k-on-failure-prob}))
  (let [initial-errors (:errors (error-function argmap training-data {:plushy plushy}))]
    (loop [step 0 curr-plushy plushy k start-k]
      (if (and verbose? (= (mod step 50) 0)) (pr {:step step :k k} " "))
      (if (< steps step)
        (do (if verbose? (prn "Finished Auto-Simplification" {:final-plushy-length (count curr-plushy) :final-plushy curr-plushy})) curr-plushy)
        (let [new-plushy (delete-k-random k curr-plushy)
              new-plushy-errors (:errors (error-function argmap training-data {:plushy new-plushy}))
              new-equal? (= new-plushy-errors initial-errors)]
          (print-str new-plushy new-plushy-errors new-equal?)
          (recur (inc step)
                 (if new-equal? new-plushy curr-plushy)
                 (if new-equal? k (if (and (> k 1) (< (rand) decrease-k-on-failure-prob)) (dec k) k))))))))