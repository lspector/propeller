(ns propeller.problems.PSB2.camel-case
  (:require [psb2.core :as psb2]
            [propeller.genome :as genome]
            [propeller.push.interpreter :as interpreter]
            [propeller.utils :as utils]
            [propeller.push.instructions :refer [get-stack-instructions]]
            [propeller.push.state :as state]
            [propeller.tools.metrics :as metrics]
            [propeller.gp :as gp]
            #?(:cljs [cljs.reader :refer [read-string]])))

; ===========  PROBLEM DESCRIPTION  =====================================
; CAMEL CASE from PSB2
; Take a string in kebab-case and convert all of the words to camelCase.
; Each group of words to convert is delimited by "-", and each grouping
; is separated by a space. For example: "camel-case example-test-string"
; → "camelCase exampleTestString"
;
; Source: https://arxiv.org/pdf/2106.06086.pdf
; =======================================================================

(def train-and-test-data (psb2/fetch-examples "data" "camel-case" 200 2000))

; Visible character ERC
(defn random-char
  []
  (rand-nth (map char (range 97 122))))

; Word generator for string ERC
(defn word-generator
  []
  (let [chars-between #(map char (range (int %1) (inc (int %2))))
        chars (chars-between \a \z)
        word-len (inc (rand-int 5))]
    (apply str (repeatedly word-len #(rand-nth chars)))))

(defn cleanup-length
  [string len]
  (let [result (take len string)]
    (if (or (= (last result) \space)
            (= (last result) \-))
      (apply str (butlast result))
      (apply str result))))

; String ERC
(defn random-input
  [len]
  (loop [result-string (word-generator)]
    (if (>= (count result-string) len)
      (cleanup-length result-string len)
      (recur (str result-string
                  (if (< (rand) 0.66) \- \space)
                  (word-generator))))))

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
      (list \- \space random-char (fn [] (random-input 21))))))


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
