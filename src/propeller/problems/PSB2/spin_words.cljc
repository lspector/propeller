(ns propeller.problems.PSB2.spin-words
  (:require [psb2.core :as psb2]
            [propeller.genome :as genome]
            [propeller.push.interpreter :as interpreter]
            [propeller.utils :as utils]
            [propeller.push.utils.helpers :refer [get-stack-instructions]]
            [propeller.push.state :as state]
            [propeller.tools.metrics :as metrics]))

; ===========  PROBLEM DESCRIPTION  ==============================
; SPIN WORDS from PSB2
; Given a string of one or more words
; (separated by spaces), reverse all of the words that are five
; or more letters long and return the resulting string.
;
; Source: https://arxiv.org/pdf/2106.06086.pdf
; ================================================================

(def train-and-test-data (psb2/fetch-examples "data" "spin-words" 200 2000))

; Visible character ERC
(defn random-char
  []
  (rand-nth (map char (range 97 122))))

;; WORK ON THIS TOMORROW;
;; SPIN WORDS STRING ERC; 

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
      (list 4 5 \space random-char ))))


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
                                   #?(:clj  (catch Exception e 1000.0)
                                      :cljs (catch js/Error. e 1000.0))))
                            outputs)
        errors (map (fn [correct-output output]
                      (if (= output :no-stack-item)
                        10000
                        (metrics/levenshtein-distance correct-output output)))
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

