(ns propeller.problems.PSB2.substitution-cipher
  (:require [psb2.core :as psb2]
            [propeller.genome :as genome]
            [propeller.push.interpreter :as interpreter]
            [propeller.utils :as utils]
            [propeller.push.instructions :refer [get-stack-instructions]]
            [propeller.push.state :as state]
            [propeller.tools.metrics :as metrics]
            [propeller.gp :as gp]
            #?(:cljs [cljs.reader :refer [read-string]])))

; ===========  PROBLEM DESCRIPTION  =========================
; SUBSTITUTION CIPHER from PSB2
; This problem gives 3 strings.
; The first two represent a cipher, mapping each character in
; one string to the one at the same index in the other string.
; The program must apply this cipher to the third string and
; return the deciphered message.
;
; Source: https://arxiv.org/pdf/2106.06086.pdf
; ============================================================

(def train-and-test-data (psb2/fetch-examples "data" "substitution-cipher" 200 2000))

(defn map-vals-input
  "Returns all the input values of a map"
  [i]
  (vals (select-keys i [:input1 :input2 :input3])))

(defn map-vals-output
  "Returns the output values of a map"
  [i]
  (vals (select-keys i [:output1])))

(def instructions
  (utils/not-lazy
    (concat
      ;;; stack-specific instructions
      (get-stack-instructions #{:exec :integer :boolean :char :string :print})
      ;;; input instructions
      (list :in1 :in2 :in3)
      ;;; close
      (list 'close)
      ;;; ERCs (constants)
      (list 0 ""))))

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
                                                            :in3 (nth input 2)})
                           (:step-limit argmap))
                         :string))
                     inputs)
        errors (map (fn [correct-output output]
                      (if (= output :no-stack-item)
                        10000
                        (metrics/levenshtein-distance correct-output output)))
                    correct-outputs
                    outputs)]
    (assoc individual
      :behaviors outputs
      :errors errors
      :total-error #?(:clj  (apply +' errors)
                      :cljs (apply + errors)))))

(defn -main
  "Runs propel-gp, giving it a map of arguments."
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
      (apply hash-map (map #(if (string? %) (read-string %) %) args))))
  (#?(:clj shutdown-agents)))
