(ns propeller.problems.PSB2.snow-day
  (:require [psb2.core :as psb2]
            [propeller.genome :as genome]
            [propeller.push.interpreter :as interpreter]
            [propeller.utils :as utils]
            [propeller.push.utils.helpers :refer [get-stack-instructions]]
            [propeller.push.state :as state]
            [propeller.tools.math :as math]))

; ===========  PROBLEM DESCRIPTION  ===============================
; SNOW DAY from PSB2
; Given an integer representing a number
; of hours and 3 floats representing how much snow is on the
; ground, the rate of snow fall, and the proportion of snow
; melting per hour, return the amount of snow on the ground
; after the amount of hours given. Each hour is considered a
; discrete event of adding snow and then melting, not a continuous
; process.
;
; Source: https://arxiv.org/pdf/2106.06086.pdf
; ==================================================================

(def train-and-test-data (psb2/fetch-examples "data" "snow-day" 200 2000))

(defn map-vals-input
  "Returns all the input values of a map (specific helper method for bouncing-balls)"
  [i]
  (vals (select-keys i [:input1 :input2 :input3 :input4])))

(defn map-vals-output
  "Returns the output values of a map (specific helper method for bouncing-balls)"
  [i]
  (get i :output1))

(def instructions
  (utils/not-lazy
    (concat
      ;;; stack-specific instructions
      (get-stack-instructions #{:exec :integer :float :boolean :print})
      ;;; input instructions
      (list :in1 :in2 :in3 :in4)
      ;;; close
      (list 'close)
      ;;; ERCs (constants)
      (list 0 1 -1 0.0 1.0 -1.0))))

(defn error-function
  [argmap data individual]
  (let [program (genome/plushy->push (:plushy individual) argmap)
        inputs (map (fn [i] (map-vals-input i)) data)
        correct-outputs (map (fn [i] (map-vals-output i)) data)
        outputs (map (fn [input]
                       (state/peek-stack
                         (interpreter/interpret-program
                           program
                           (assoc state/empty-state :input {:in1 (nth input 0)
                                                            :in2 (nth input 1)
                                                            :in3 (nth input 2)
                                                            :in4 (nth input 3)})
                           (:step-limit argmap))
                         :float))
                     inputs)
        errors (map (fn [correct-output output]
                      (if (= output :no-stack-item)
                        1000000.0
                        (min 1000.0 (math/abs (- correct-output output)))))
                    correct-outputs
                    outputs)]
    (assoc individual
      :behaviors outputs
      :errors errors
      :total-error #?(:clj  (apply +' errors)
                      :cljs (apply + errors)))))

(def arglist
  {:instructions   instructions
   :error-function error-function
   :training-data  (:train train-and-test-data)
   :testing-data   (:test train-and-test-data)})