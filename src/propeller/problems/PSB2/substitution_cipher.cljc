(ns propeller.problems.PSB2.substitution-cipher
  (:require [psb2.core :as psb2]
            [propeller.genome :as genome]
            [propeller.push.interpreter :as interpreter]
            [propeller.utils :as utils]
            [propeller.push.utils.helpers :refer [get-stack-instructions]]
            [propeller.push.state :as state]
            [propeller.tools.math :as math]
            [propeller.tools.metrics :as metrics]))

;; Get path from text file
(def PSB2-path (slurp "PSB2_path.txt"))

(def train-and-test-data (psb2/fetch-examples PSB2-path "substitution-cipher" 200 2000))

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
      (list 0 ""))))

; Error function takes from integer stack, calculates error based on absolute value of
; difference between output and correct output.
(defn error-function
  ([argmap individual]
   (error-function argmap individual :train))
  ([argmap individual subset]
   (let [program (genome/plushy->push (:plushy individual) argmap)
         data (get train-and-test-data subset)
         inputs (map (fn [i] (get i :input1)) data)
         correct-outputs (map (fn [i] (get i :output1)) data)
         outputs (map (fn [input]
                        (state/peek-stack
                          (interpreter/interpret-program
                            program
                            (assoc state/empty-state :input {:in1 (get input 0)
                                                             :in2 (get input 1)
                                                             :in3 (get input 2)})
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
                         (metrics/levenshtein-distance correct-output output)))
                     correct-outputs
                     parsed-outputs)]
     (assoc individual
       :behaviors parsed-outputs
       :errors errors
       :total-error #?(:clj  (apply +' errors)
                       :cljs (apply + errors))))))

