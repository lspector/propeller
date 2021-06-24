(ns propeller.problems.PSB2-Problems
  (:require [psb2.core :as psb2]
            [propeller.genome :as genome]
            [propeller.push.interpreter :as interpreter]
            [propeller.utils :as utils]
            [propeller.push.utils.helpers :refer [get-stack-instructions]]
            [propeller.push.state :as state]
            [propeller.tools.math :as math]))

;; Get path from text file
(def PSB2-path (slurp "PSB2_path.txt"))

;; ** worked on GENERAL SOLUTION for PSB2, save for later **
;(defn train-and-test
;  "Returns the train and test sets from PSB2 using Prof. Helmuth's function"
;  ;; Default to 200 train and 2000 test
;  [problem]
;  (psb2/fetch-examples PSB2-path problem 200 2000)
;  ;; To with 2 extra args, can customise train and test set sizes
;  [problem train test]
;  (psb2/fetch-examples PSB2-path problem train test))
;; ** end save for later **

;; train and test set generation: Specific function for fuel-cost,
;; single PSB2 problem implementation
;(defn train-and-test-data
;  "Returns the train and test sets "
;  []
;  (psb2/fetch-examples PSB2-path "fuel-cost" 200 2000))

(def train-and-test-data (psb2/fetch-examples PSB2-path "fuel-cost" 200 2000))



;; Instruction set: ported from number-io, meant for fuel-cost
(def instructions
  (utils/not-lazy
    (concat
      ;; stack-specific instructions
      (get-stack-instructions #{:float :integer :print})
      ;; input instructions
      (list :in1))))
      ;; ERCs (constants)
      ;; (list random-float random-int))))

;; Error function from number-io
(defn error-function
  ([argmap individual]
   (error-function argmap individual :train))
  ([argmap individual subset]
   (let [program (genome/plushy->push (:plushy individual) argmap)
         data (get train-and-test-data subset)
         inputs (map (fn [i] (get i :input1)) (get train-and-test-data subset))
         correct-outputs (map (fn [i] (get i :output1)) (get train-and-test-data subset))
         outputs (map (fn [input]
                        (state/peek-stack
                          (interpreter/interpret-program
                            program
                            (assoc state/empty-state :input {:in1 (first input)}
                                                     :output '(""))
                            (:step-limit argmap))
                          :output))
                      inputs)
         parsed-outputs (map (fn [output]
                               (try (read-string output)
                                    #?(:clj (catch Exception e 1000.0)
                                       :cljs (catch js/Error. e 1000.0))))
                             outputs)
         errors (map (fn [correct-output output]
                       (min 1000.0 (math/abs (- correct-output output))))
                     correct-outputs
                     parsed-outputs)]
     (assoc individual
       :behaviors parsed-outputs
       :errors errors
       :total-error #?(:clj (apply +' errors)
                       :cljs (apply + errors))))))

