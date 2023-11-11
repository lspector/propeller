(ns propeller.problems.complex-regression
  (:require [propeller.genome :as genome]
            [propeller.push.interpreter :as interpreter]
            [propeller.push.state :as state]
            [propeller.tools.math :as math]
            [propeller.gp :as gp]
            #?(:cljs [cljs.reader :refer [read-string]])))

(defn- target-function
  "Target function: f(x) = (x^3 + 1)^3 + 1"
  [x]
  (let [x-new (+ (* x x x) 1)]
    (+ (* x-new x-new x-new) 1)))

(def train-and-test-data
  (let [train-inputs (range -4.0 4.0 0.25)
        test-inputs (range -4.125 4.125 0.25)]
    {:train (map (fn [x] {:input1 (vector x) :output1 (vector (target-function x))}) train-inputs)
     :test (map (fn [x] {:input1 (vector x) :output1 (vector (target-function x))}) test-inputs)}))

(def instructions
  (list :in1
        :float_add
        :float_subtract
        :float_mult
        :float_quot
        :float_eq
        :exec_dup
        :exec_if
        'close
        0
        1))

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
    {:instructions             instructions
     :error-function           error-function
     :training-data            (:train train-and-test-data)
     :testing-data             (:test train-and-test-data)
     :max-generations          5000
     :population-size          500
     :max-initial-plushy-size  100
     :step-limit               200
     :parent-selection         :ds-lexicase
     :ds-function              :case-maxmin
     :downsample-rate          0.1
     :case-t-size              20
     :tournament-size          5
     :umad-rate                0.1
     :variation                {:umad 1.0 :crossover 0.0}
     :elitism                  false}
    (apply hash-map (map #(if (string? %) (read-string %) %) args)))))
