(ns propeller.problems.PSB1.count-odds
  (:require [psb2.core :as psb2]
            [propeller.genome :as genome]
            [propeller.push.interpreter :as interpreter]
            [propeller.utils :as utils]
            [propeller.push.instructions :refer [get-stack-instructions]]
            [propeller.push.state :as state]
            [propeller.tools.math :as math]
            [propeller.gp :as gp]
            #?(:cljs [cljs.reader :refer [read-string]])))


(def train-and-test-data (psb2/fetch-examples "data" "count-odds" 200 2000))
(def train-data (:train train-and-test-data))
(def test-data (:test train-and-test-data))

; Random integer between -100 and 100 (from smallest)
(defn random-int [] (- (rand-int 201) 100))

(def instructions
  (utils/not-lazy
   (concat
      ;;; stack-specific instructions
    (get-stack-instructions #{:exec :integer :boolean :vector_integer :print})
      ;;; input instructions
    (list :in1)
      ;;; close
    (list 'close)
      ;;; ERCs (constants)
    (list random-int 0 1 2))))

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
     :training-data           train-data
     :testing-data            test-data
     :case-t-size             (count train-data)
     :ds-parent-rate          0
     :ds-parent-gens          1
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
