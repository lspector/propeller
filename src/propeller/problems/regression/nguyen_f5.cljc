;; This file implements a version of the Nguyen-F5 symbolic regression problem, as described in various
;; publications in the genetic programming literature including:
;;
;;   Makke, N., Chawla, S. Interpretable scientific discovery with symbolic regression: a review. 
;;   Artif Intell Rev 57, 2 (2024). https://doi.org/10.1007/s10462-023-10622-0
;;
;; Note however that it may differ in some respects from the problem used elsewhere, for example
;; in the data ranges and gentic programming function sets which are not always fully documented
;; in the literature. For this reason, while this code can be used as an example and for comparing
;; different configurations of the present system, results obtained with this code may not be directly
;; comparable to those published in the literature.


(ns propeller.problems.regression.nguyen-f5
  (:require [propeller.genome :as genome]
            [propeller.push.interpreter :as interpreter]
            [propeller.push.state :as state]
            [propeller.tools.math :as math]
            [propeller.gp :as gp]
            #?(:cljs [cljs.reader :refer [read-string]])))

(defn- target-function
  "Nguyen F5 = sin(x^2)cos(x) - 1"
  [x]
  (- (* (Math/sin (* x x))
        (Math/cos x))
     1))

(def train-and-test-data
  (let [train-inputs (range -4.0 4.0 0.1)
        test-inputs (range -4.0 4.0 0.05)]
    {:train (map (fn [x] {:input1 (vector x) :output1 (vector (target-function x))}) train-inputs)
     :test (map (fn [x] {:input1 (vector x) :output1 (vector (target-function x))}) test-inputs)}))

(def instructions
  (list :in1
        :float_add
        :float_subtract
        :float_mult
        :float_div
        :float_sin
        :float_cos
        :float_tan
        0.0
        1.0))

(defn error-function
  "Finds the behaviors and errors of an individual. The error is the absolute
  deviation between the target output value and the program's selected behavior,
  or 1000000 if no behavior is produced. The behavior is here defined as the
  final top item on the FLOAT stack."
  ([argmap data individual]
   (let [program (genome/plushy->push (:plushy individual) argmap)
         inputs (map (fn [x] (first (:input1 x))) data)
         correct-outputs (map (fn [x] (first (:output1 x))) data)
         outputs (map (fn [input]
                        (state/peek-stack
                         (interpreter/interpret-program
                          program
                          (assoc state/empty-state :input {:in1 input})
                          (:step-limit argmap))
                         :float))
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

(defn -main
  "Runs the top-level genetic programming function, giving it a map of 
  arguments with defaults that can be overridden from the command line
  or through a passed map."
  [& args]
  (gp/gp
   (merge
    {:instructions              instructions
     :error-function            error-function
     :training-data             (:train train-and-test-data)
     :testing-data              (:test train-and-test-data)
     :downsample?               false
     :solution-error-threshold  0.1
     :max-generations           300
     :population-size           1000
     :max-initial-plushy-size   50
     :step-limit                100
     :parent-selection          :epsilon-lexicase
     :umad-rate                 0.05
     :variation                 {:umad 1.0}
     :simplification?           true}
    (apply hash-map (map #(if (string? %) (read-string %) %) args)))))
