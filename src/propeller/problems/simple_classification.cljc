(ns propeller.problems.simple-classification
  (:require [propeller.genome :as genome]
            [propeller.push.interpreter :as interpreter]
            [propeller.push.state :as state]
            [propeller.gp :as gp]
            #?(:cljs [cljs.reader :refer [read-string]])))

;; =============================================================================
;; Simple classification
;; =============================================================================

;; Set of original propel instructions
(def instructions
  (list :in1
        :integer_add
        :integer_subtract
        :integer_mult
        :integer_quot
        :integer_mod
        :integer_eq
        :boolean_and
        :boolean_not
        :boolean_invert_first_then_and
        :boolean_invert_second_then_and
        :boolean_from_integer
        true
        false
        'close
        0
        1
        3))

(defn- target-function
  "If number is divisible by 3 but not 7, leave TRUE on the BOOLEAN stack else leave FALSE on the BOOLEAN stack"
  [x]
  (let [div-3 (= 0 (mod x 3))
        div-7 (= 0 (mod x 7))]
    (and div-3 (not div-7))))

(def train-and-test-data
  (let [train-inputs (range 0 100)
        test-inputs (range 100 300)
        train-outputs (map target-function train-inputs)
        test-outputs (map target-function test-inputs)]
    {:train (map (fn [in out] {:input1 (vector in) :output1 (vector out)}) train-inputs train-outputs)
     :test (map (fn [in out] {:input1 (vector in) :output1 (vector  out)}) test-inputs test-outputs)}))

(defn error-function
  "Finds the behaviors and errors of an individual: Error is 0 if the value and
  the program's selected behavior match, or 1 if they differ, or 1000000 if no
  behavior is produced. The behavior is here defined as the final top item on
  the BOOLEAN stack."
  [argmap data individual]
  (let [program (genome/plushy->push (:plushy individual) argmap)
        inputs (map (fn [x] (first (:input1 x))) data)
        correct-outputs (map (fn [x] (first (:output1 x))) data)
        outputs (map (fn [input]
                       (state/peek-stack
                         (interpreter/interpret-program
                           program
                           (assoc state/empty-state :input {:in1 input})
                           (:step-limit argmap))
                         :boolean))
                     inputs)
        errors (map (fn [correct-output output]
                      (if (= output :no-stack-item)
                        1000000
                        (if (= correct-output output)
                          0
                          1)))
                    correct-outputs
                    outputs)]
    (assoc individual
      :behaviors outputs
      :errors errors
      :total-error #?(:clj  (apply +' errors)
                      :cljs (apply + errors)))))

(defn -main
  "Runs the top-level genetic programming function, giving it a map of 
  arguments with defaults that can be overridden from the command line
  or through a passed map."
  [& args]
  (gp/gp
    (merge
      {:instructions            instructions
       :error-function          error-function
       :training-data           (:train train-and-test-data)
       :testing-data            (:test train-and-test-data)
       :max-generations         500
       :population-size         500
       :max-initial-plushy-size 100
       :step-limit              200
       :parent-selection        :lexicase
       :umad-rate               0.1
       :variation               {:umad 1.0 :crossover 0.0}
       :elitism                 false}
      (apply hash-map (map #(if (string? %) (read-string %) %) args)))))
