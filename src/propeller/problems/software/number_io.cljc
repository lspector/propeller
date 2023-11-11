(ns propeller.problems.software.number-io
  "Number IO from iJava (http://ijava.cs.umass.edu/)

     This problem file defines the following problem:
There are two inputs, a float and an int. The program must read them in, find
their sum as a float, and print the result as a float.
     "
  (:require [propeller.genome :as genome]
            [propeller.push.interpreter :as interpreter]
            [propeller.push.state :as state]
            [propeller.push.instructions :refer [get-stack-instructions]]
            [propeller.utils :as utils]
            [propeller.tools.math :as math]
            [propeller.gp :as gp]
            #?(:cljs [cljs.reader :refer [read-string]])))

;; =============================================================================
;; Tom Helmuth, thelmuth@cs.umass.edu
;;
;; NUMBER IO PROBLEM
;;
;; This problem file defines the following problem:
;; There are two inputs, a float and an int. The program must read them in, find
;; their sum as a float, and print the result as a float.
;;
;; Problem Source: iJava (http://ijava.cs.umass.edu/)
;;
;; NOTE: input stack: in1 (float),
;;                    in2 (int)
;;       output stack: printed output
;; =============================================================================

;; =============================================================================
;; DATA DOMAINS
;;
;; A list of data domains. Each domain is a map containing a "set" of inputs
;; and two integers representing how many cases from the set should be used as
;; training and testing cases respectively. Each "set" of inputs is either a
;; list or a function that, when called, will create a random element of the set
;; =============================================================================

;; Random float between -100.0 and 100.0
(defn random-float "Random float between -100.0 and 100.0" [] (- (* (rand) 200) 100.0))

; Random integer between -100 and 100
(defn random-int "Random integer between -100 and 100" [] (- (rand-int 201) 100))

(def instructions
  "Stack-specific instructions, input instructions, close, and constants"
  (utils/not-lazy
    (concat
      ;; stack-specific instructions
      (get-stack-instructions #{:float :integer :print})
      ;; input instructions
      (list :in1 :in2)
      ;; ERCs (constants)
      (list random-float random-int))))

(def train-and-test-data
  "Inputs are random integers and random floats and outputs are the sum as a float."
  (let [inputs (vec (repeatedly 1025 #(vector (random-int) (random-float))))
        outputs (mapv #(apply + %) inputs)
        train-set {:inputs  (take 25 inputs)
                   :outputs (take 25 outputs)}
        test-set {:inputs  (drop 25 inputs)
                  :outputs (drop 25 outputs)}]
    {:train train-set
     :test  test-set}))

(defn error-function
  "Finds the behaviors and errors of an individual: Error is the absolute difference between
  program output and the correct output.
  The behavior is here defined as the final top item on
  the PRINT stack."
  [argmap data individual]
  (let [program (genome/plushy->push (:plushy individual) argmap)
        inputs (:inputs data)
        correct-outputs (:outputs data)
        outputs (map (fn [input]
                       (state/peek-stack
                         (interpreter/interpret-program
                           program
                           (assoc state/empty-state :input {:in1 (first input)
                                                            :in2 (last input)}
                                                    :print '(""))
                           (:step-limit argmap))
                         :print))
                     inputs)
        parsed-outputs (map (fn [output]
                              (try (read-string output)
                                   #?(:clj  (catch Exception e 1000.0)
                                      :cljs (catch js/Error. e 1000.0))))
                            outputs)
        errors (map (fn [correct-output output]
                      (min 1000.0 (math/abs (- correct-output output))))
                    correct-outputs
                    parsed-outputs)]
    (assoc individual
      :behaviors parsed-outputs
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
       :max-generations         300
       :population-size         1000
       :max-initial-plushy-size 100
       :step-limit              200
       :parent-selection        :lexicase
       :tournament-size         5
       :umad-rate               0.1
       :variation               {:umad 0.5 :crossover 0.5}
       :elitism                 false}
      (apply hash-map (map #(if (string? %) (read-string %) %) args)))))
