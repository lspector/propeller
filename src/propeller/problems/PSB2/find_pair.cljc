(ns propeller.problems.PSB2.find-pair
  (:require [psb2.core :as psb2]
            [propeller.genome :as genome]
            [propeller.push.interpreter :as interpreter]
            [propeller.problems.data-creation :as dc]
            [propeller.utils :as utils]
            [propeller.push.instructions :refer [def-instruction get-stack-instructions]]
            [propeller.push.state :as state]
            [propeller.tools.math :as math]
            [propeller.gp :as gp]
            #?(:cljs [cljs.reader :refer [read-string]])))

(def train-and-test-data (psb2/fetch-examples "data" "find-pair" 200 2000))
(def train-data (:train train-and-test-data))
(def test-data (:test train-and-test-data))

(defn random-int [] (- (rand-int 201) 100))

(defn map-vals-input
  "Returns all the input values of a map"
  [i]
  (vals (select-keys i [:input1 :input2])))

(defn map-vals-output
  "Returns the output values of a map"
  [i]
  (vals (select-keys i [:output1 :output2])))

(def-instruction :output-one
  ^{:stacks #{:integer :output}}
  (fn [state]
    (if (empty? (:integer state))
      state
      (let [top-int (state/peek-stack state :integer)]
        (assoc-in state [:output :out1] top-int)))))

(def-instruction :output-two
  ^{:stacks #{:integer :output}}
  (fn [state]
    (if (empty? (:integer state))
      state
      (let [top-int (state/peek-stack state :integer)]
        (assoc-in state [:output :out2] top-int)))))

(def instructions
  (utils/not-lazy
   (concat
      ;;; stack-specific instructions
    (get-stack-instructions #{:exec :integer :vector_integer :boolean})
    (list :output-one :output-two)
      ;;; input instructions
    (list :in1 :in2)
      ;;; close
    (list 'close)
      ;;; ERCs (constants)
    (list -1 0 1 2 random-int))))

(defn error-function
  [argmap data individual]
  (let [program (genome/plushy->push (:plushy individual) argmap)
        inputs (map (fn [i] (map-vals-input i)) data)
        correct-outputs (map (fn [i] (map-vals-output i)) data)
        outputs (map (fn [input]
                       (:output
                        (interpreter/interpret-program
                         program
                         (assoc state/empty-state :input {:in1 (nth input 0)
                                                          :in2 (nth input 1)})
                         (:step-limit argmap))))
                     inputs)
        outputs-1 (map #(:out1 %) outputs)
        outputs-2 (map #(:out2 %) outputs)
        ;_ (prn {:o1 outputs-1 :o2 outputs-2})
        errors (map (fn [correct-output output-1 output-2]
                      (if (not (and (number? output-2) (number? output-1)))
                        100000
                        (+  (math/abs (- (first correct-output) output-1))
                            (math/abs (- (second correct-output) output-2)))))
                    correct-outputs outputs-1 outputs-2)]
    (assoc individual
           :behavior outputs
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
    (apply hash-map (map #(if (string? %) (read-string %) %) args))))
  (#?(:clj shutdown-agents)))