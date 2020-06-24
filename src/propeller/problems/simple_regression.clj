(ns propeller.problems.simple-regression
  (:require [propeller.genome :refer [plushy->push]]
            [propeller.push.interpreter :refer [interpret-program]]
            [propeller.push.state :refer [empty-state
                                          peek-stack]]
            [tools.math :as math]))

;; =============================================================================
;; Problem: f(x) = 7x^2 - 20x + 13
;; =============================================================================

(defn- target-function-hard
  "Target function: f(x) = 7x^2 - 20x + 13"
  [x]
  (+ (* 7 x x) (* -20 x) 13))

(defn- target-function
  "Target function: f(x) = x^3 + x + 3"
  [x]
  (+ (* x x x) x 3))

(defn regression-error-function
  "Finds the behaviors and errors of an individual. The error is the absolute
  deviation between the target output value and the program's selected behavior,
  or 1000000 if no behavior is produced. The behavior is here defined as the
  final top item on the INTEGER stack."
  [argmap individual]
  (let [program (plushy->push (:plushy individual))
        inputs (range -10 11)
        correct-outputs (map target-function inputs)
        outputs (map (fn [input]
                       (peek-stack
                         (interpret-program
                           program
                           (assoc empty-state :input {:in1 input})
                           (:step-limit argmap))
                         :integer))
                     inputs)
        errors (map (fn [correct-output output]
                      (if (= output :no-stack-item)
                        1000000
                        (math/abs (- correct-output output))))
                    correct-outputs
                    outputs)]
    (assoc individual
      :behaviors outputs
      :errors errors
      :total-error (apply +' errors))))
