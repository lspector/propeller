(ns propeller.simplification
  "To use Propeller's auto-simplification system, simply include the following four command line arguments when running a problem:

```clojure
:simplification? true
```
Toggle auto-simplification
```clojure
:simplification-k 4
```
This is the upper bound for elements deleted from the plushy every step. Every step, a number in [1, k] of elements is deleted from the plushy representation of the solution.
```clojure
:simplification-steps 1000
```
Number of simplification steps to perform
```clojure
:simplification-verbose? true
```
Whether or not to output simplification info into the output of the evolutionary run.
The output with verbose adds the following lines to the output:
```clojure
{:start-plushy-length 42, :k 4}
{:final-plushy-length 13, :final-plushy (:in1 :in1 :integer_quot :in1 :in1 :exec_dup :in1 :integer_mult close :exec_dup :integer_add 1 :integer_add)}
```"
  {:doc/format :markdown}
  (:require [propeller.genome :as genome]
             [propeller.push.interpreter :as interpreter]
            [propeller.push.state :as state]
            [propeller.tools.math :as math]
            ))

(defn choose-random-k
  "Takes k random indices"
  [k indices]
  (take k (shuffle indices)))

(defn delete-at-indices
  "deletes the values at given set of indices"
  [indices plushy]
  (let [sorted-indices (sort > indices)]
    (keep-indexed #(when (not (some #{%1} sorted-indices)) %2) plushy)))

(defn delete-k-random
  "Deletes k random instructions from the plushy"
  [k plushy]
  (delete-at-indices (choose-random-k k (range (count plushy))) plushy))

(defn auto-simplify-plushy
  "simplifies plushy by deleting instructions that have no impact on errors. naive auto-simplification"
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