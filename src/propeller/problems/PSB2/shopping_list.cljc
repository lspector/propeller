(ns propeller.problems.PSB2.shopping-list
  "SHOPPING LIST from PSB2

Given a vector of floats representing
the prices of various shopping goods and another vector of
floats representing the percent discount of each of those
goods, return the total price of the shopping trip after applying the discount to each item.

Source: https://arxiv.org/pdf/2106.06086.pdf"
  {:doc/format :markdown}
  (:require [psb2.core :as psb2]
            [propeller.genome :as genome]
            [propeller.push.interpreter :as interpreter]
            [propeller.utils :as utils]
            [propeller.push.instructions :refer [get-stack-instructions]]
            [propeller.push.state :as state]
            [propeller.tools.math :as math]
            [propeller.gp :as gp]
            #?(:cljs [cljs.reader :refer [read-string]])))


(def train-and-test-data "Data taken from https://zenodo.org/record/5084812" (psb2/fetch-examples "data" "shopping-list" 200 2000))

(defn random-float "Random float between -100 and 100" [] (- (rand 201) 100))

(defn map-vals-input
  "Returns all the input values of a map"
  [i]
  (vals (select-keys i [:input1 :input2])))

(defn map-vals-output
  "Returns the output values of a map "
  [i]
  (get i :output1))

(def instructions
  "Stack-specific instructions, input instructions, close, and constants"
  (utils/not-lazy
    (concat
      ;;; stack-specific instructions
      (get-stack-instructions #{:exec :integer :float :boolean :print})
      ;;; input instructions
      (list :in1 :in2)
      ;;; close
      (list 'close)
      ;;; ERCs (constants)
      (list 0.0 100.0 random-float))))

(defn error-function
  "Finds the behaviors and errors of an individual: Error is 0 if the value and
  the program's selected behavior match, or 1 if they differ, or 1000000 if no
  behavior is produced. The behavior is here defined as the final top item on
  the FLOAT stack."
  [argmap data individual]
  (let [program (genome/plushy->push (:plushy individual) argmap)
        inputs (map (fn [i] (map-vals-input i)) data)
        correct-outputs (map (fn [i] (map-vals-output i)) data)
        outputs (map (fn [input]
                       (state/peek-stack
                         (interpreter/interpret-program
                           program
                           (assoc state/empty-state :input {:in1 (nth input 0)
                                                            :in2 (nth input 1)})
                           (:step-limit argmap))
                         :float))
                     inputs)
        errors (map (fn [correct-output output]
                      (if (= output :no-stack-item)
                        1000000.0
                        (math/abs (- correct-output output))))
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
       :max-generations         300
       :population-size         1000
       :max-initial-plushy-size 250
       :step-limit              2000
       :parent-selection        :lexicase
       :tournament-size         5
       :umad-rate               0.1
       :variation               {:umad 1.0 :crossover 0.0}
       :elitism                 false}
      (apply hash-map (map #(if (string? %) (read-string %) %) args)))))
