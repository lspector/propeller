(ns propeller.problems.PSB2.fizz-buzz
  (:require [psb2.core :as psb2]
            [propeller.genome :as genome]
            [propeller.push.interpreter :as interpreter]
            [propeller.utils :as utils]
            [propeller.push.utils.helpers :refer [get-stack-instructions]]
            [propeller.push.state :as state]
            [propeller.tools.metrics :as metrics]))

; ===========  PROBLEM DESCRIPTION  =========================
; FIZZ BUZZ from PSB2
; Given an integer x, return "Fizz" if x is
; divisible by 3, "Buzz" if x is divisible by 5, "FizzBuzz" if x
; is divisible by 3 and 5, and a string version of x if none of
; the above hold.
;
; Source: https://arxiv.org/pdf/2106.06086.pdf
; ============================================================

(def train-and-test-data (psb2/fetch-examples "data" "fizz-buzz" 200 2000))

(def instructions
  (utils/not-lazy
    (concat
      ;;; stack-specific instructions
      (get-stack-instructions #{:exec :integer :boolean :string :print})
      ;;; input instructions
      (list :in1)
      ;;; close
      (list 'close)
      ;;; ERCs (constants)
      (list "Fizz" "Buzz" "FizzBuzz" 0 3 5))))

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
                          :string))
                      inputs)
         parsed-outputs (map (fn [output]
                               (try (read-string output)
                                    #?(:clj (catch Exception e 1000.0)
                                       :cljs (catch js/Error. e 1000.0))))
                             outputs)
         errors (map (fn [correct-output output]
                       (if (= output :no-stack-item)
                         10000
                         (metrics/levenshtein-distance (str correct-output) (str output))))
                     correct-outputs
                     parsed-outputs)]
     (assoc individual
       :behaviors parsed-outputs
       :errors errors
       :total-error #?(:clj  (apply +' errors)
                       :cljs (apply + errors)))))


(def arglist
  {:instructions   instructions
   :error-function error-function
   :training-data  (:train train-and-test-data)
   :testing-data   (:test train-and-test-data)})

