(ns propeller.problems.simple-regression
  (:require [propeller.genome :as genome]
            [propeller.push.interpreter :as interpreter]
            [propeller.push.state :as state]
            [propeller.tools.math :as math]))

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

;; Set of original propel instructions
(def instructions
  (list :in1
        :integer_add
        :integer_subtract
        :integer_mult
        :integer_quot
        :integer_eq
        :exec_dup
        :exec_if
        'close
        0
        1))

(defn train-and-test-data
  [target-function]
  (let [train-inputs (range -10 11)
        test-inputs (concat (range -20 -10) (range 11 21))]
    {:train {:inputs  train-inputs
             :outputs (map target-function train-inputs)}
     :test  {:inputs  test-inputs
             :outputs (map target-function test-inputs)}}))

(defn error-function
  "Finds the behaviors and errors of an individual. The error is the absolute
  deviation between the target output value and the program's selected behavior,
  or 1000000 if no behavior is produced. The behavior is here defined as the
  final top item on the INTEGER stack."
  ([argmap individual]
   (error-function argmap individual :train))
  ([argmap individual subset]
   (let [program (genome/plushy->push (:plushy individual) argmap)
         data (get (train-and-test-data target-function) subset)
         inputs (:inputs data)
         correct-outputs (:outputs data)
         outputs (map (fn [input]
                        (state/peek-stack
                          (interpreter/interpret-program
                            program
                            (assoc state/empty-state :input {:in1 input})
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
       :total-error #?(:clj  (apply +' errors)
                       :cljs (apply + errors))))))
