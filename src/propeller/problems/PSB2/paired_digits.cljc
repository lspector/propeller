(ns propeller.problems.PSB2.paired-digits
  (:require [psb2.core :as psb2]
            [propeller.genome :as genome]
            [propeller.push.interpreter :as interpreter]
            [propeller.utils :as utils]
            [propeller.push.utils.helpers :refer [get-stack-instructions]]
            [propeller.push.state :as state]
            [propeller.tools.math :as math]))

; ===========  PROBLEM DESCRIPTION  =============================
; PAIRED DIGITS from PSB2
; Given a string of digits, return the sum
; of the digits whose following digit is the same.
;
; Source: https://arxiv.org/pdf/2106.06086.pdf
; ===============================================================

(def train-and-test-data (psb2/fetch-examples "data" "paired-digits" 200 2000))

(defn random-int [] (- (rand-int 201) 100))

(defn random-char [] (rand-nth '(\0 \1 \2 \3 \4 \5 \6 \7 \8 \9)))

(def instructions
  (utils/not-lazy
    (concat
      ;;; stack-specific instructions
      (get-stack-instructions #{:exec :integer :boolean :char :string :print})
      ;;; input instructions
      (list :in1)
      ;;; close
      (list 'close)
      ;;; ERCs (constants)
      (list 0 random-int random-char))))

(defn error-function
  [argmap data individual]
  (let [program (genome/plushy->push (:plushy individual) argmap)
        inputs (map (fn [i] (get i :input1)) data)
        correct-outputs (map (fn [i] (get i :output1)) data)
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
