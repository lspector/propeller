(ns propeller.problems.PSB1.grade
  (:require
   [psb2.core :as psb2]
   [propeller.genome :as genome]
   [propeller.push.interpreter :as interpreter]
   [propeller.push.state :as state]
   [propeller.push.instructions :refer [get-stack-instructions]]
   [propeller.utils :as utils]
   [propeller.tools.metrics :as metrics]
   [propeller.gp :as gp]
   #?(:cljs [cljs.reader :refer [read-string]])))


; Based on the grade PSB1 problem, this verion only requires an output of a single character
;"“Student has a ”, “ grade.”, “A”, “B”, “C”, “D”, “F”, integer ERC"

(def train-and-test-data (psb2/fetch-examples "data" "grade" 200 2000))
(def train-data (:train train-and-test-data))
(def test-data (:test train-and-test-data))

(defn map-vals-input
  "Returns all the input values of a map"
  [i]
  (vals (select-keys i [:input1 :input2 :input3 :input4 :input5])))

(defn get-output
  "returns the outputs of the grade function with JUST the letter grade"
  [i]
  (str (nth i 14)))

; Random integer between -100 and 100
(defn random-int [] (- (rand-int 201) 100))

(def instructions
  (utils/not-lazy
   (concat
      ;; stack-specific instructions
    (get-stack-instructions #{:boolean :exec :integer :string :print})
      ;; input instructions
    (list :in1 :in2 :in3 :in4 :in5)
    ;;close
    (list 'close)
      ;; ERCs (constants)
    (list "A" "B" "C" "D" "F" random-int))))

(defn error-function
  [argmap data individual]
  (let [program (genome/plushy->push (:plushy individual) argmap)
        inputs (map (fn [i] (map-vals-input i)) data)
        correct-outputs (map (fn [i] (get-output (get i :output1))) data)
        outputs (map (fn [input]
                       (state/peek-stack
                        (interpreter/interpret-program
                         program
                         (assoc state/empty-state :input {:in1 (nth input 0)
                                                          :in2 (nth input 1)
                                                          :in3 (nth input 2)
                                                          :in4 (nth input 3)
                                                          :in5 (nth input 4)})
                         (:step-limit argmap))
                        :print))
                     inputs)
        errors (map (fn [correct-output output]
                      (if (= output :no-stack-item)
                        1000000.0
                        (metrics/levenshtein-distance correct-output output)))
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
