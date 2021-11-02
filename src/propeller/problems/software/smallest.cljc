(ns propeller.problems.software.smallest
  (:require [propeller.genome :as genome]
            [propeller.push.interpreter :as interpreter]
            [propeller.push.state :as state]
            [propeller.push.instructions :refer [get-stack-instructions]]
            [propeller.utils :as utils]
            [propeller.gp :as gp]
            #?(:cljs [cljs.reader :refer [read-string]])))

;; =============================================================================
;; Tom Helmuth, thelmuth@cs.umass.edu
;;
;; SMALLEST PROBLEM
;;
;; This problem file defines the following problem:
;; There are two inputs, a float and an int. The program must read them in, find
;; their sum as a float, and print the result as a float.
;;
;; Problem Source: C. Le Goues et al., "The ManyBugs and IntroClass Benchmarks
;; for Automated Repair of C Programs," in IEEE Transactions on Software
;; Engineering, vol. 41, no. 12, pp. 1236-1256, Dec. 1 2015.
;; doi: 10.1109/TSE.2015.2454513
;;
;; NOTE: input stack: in1 (int),
;;                    in2 (int),
;;                    in3 (int),
;;                    in4 (int),
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

; Random integer between -100 and 100
(defn random-int [] (- (rand-int 201) 100))

(def instructions
  (utils/not-lazy
    (concat
      ;; stack-specific instructions
      (get-stack-instructions #{:boolean :exec :integer :print})
      ;; input instructions
      (list :in1 :in2 :in3 :in4)
      ;; ERCs (constants)
      (list random-int))))

(def train-and-test-data
  (let [inputs (vec (repeatedly 1100 #(vector (random-int) (random-int)
                                              (random-int) (random-int))))
        outputs (mapv #(apply min %) inputs)
        train-set {:inputs  (take 100 inputs)
                   :outputs (take 100 outputs)}
        test-set {:inputs  (drop 100 inputs)
                  :outputs (drop 100 outputs)}]
    {:train train-set
     :test  test-set}))

(defn error-function
  [argmap data individual]
  (let [program (genome/plushy->push (:plushy individual) argmap)
        inputs (:inputs data)
        correct-outputs (:outputs data)
        outputs (map (fn [input]
                       (state/peek-stack
                         (interpreter/interpret-program
                           program
                           (assoc state/empty-state :input {:in1 (get input 0)
                                                            :in2 (get input 1)
                                                            :in3 (get input 2)
                                                            :in4 (get input 3)}
                                                    :print '(""))
                           (:step-limit argmap))
                         :print))
                     inputs)
        errors (map (fn [correct-output output]
                      (if (= (str correct-output) output) 0 1))
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
       :max-initial-plushy-size 100
       :step-limit              200
       :parent-selection        :lexicase
       :tournament-size         5
       :umad-rate               0.1
       :variation               {:umad 0.5 :crossover 0.5}
       :elitism                 false}
      (apply hash-map (map #(if (string? %) (read-string %) %) args))))
  (#?(:clj shutdown-agents)))
