(ns propeller.problems.valiant
  "Possibly impossible to solve with genetic programming. Stems from the work of Leslie Valiant and involves
  determining the parity of an unknown subsequence of a larger sequence of bits."
  {:doc/format :markdown}
  (:require [propeller.genome :as genome]
            [propeller.push.interpreter :as interpreter]
            [propeller.push.state :as state]
            [propeller.gp :as gp]
            #?(:cljs [cljs.reader :refer [read-string]])))

(def ^:no-doc num-vars 100)                                          ;10) ;100)                                          ;1000)
(def ^:no-doc num-inputs 50)                                         ;5) ; 50)                                         ;500)
(def ^:no-doc num-train 500)                                         ;5000)
(def ^:no-doc num-test 200)

(def train-and-test-data
  "Inputs are `num-train` random boolean values and outputs are the
  even parity of a subset of input variables."
  (let [input-indices (take num-inputs (shuffle (range num-vars)))
        rand-vars (fn [] (vec (repeatedly num-vars #(< (rand) 0.5))))
        even-parity? (fn [vars]
                       (even? (count (filter #(= % true)
                                             (map #(nth vars %)
                                                  input-indices)))))
        train-inputs (repeatedly num-train rand-vars)
        test-inputs (repeatedly num-test rand-vars)]
    {:train (map (fn [x] {:input1 x :output1 (vector (even-parity? x))}) train-inputs)
     :test (map (fn [x] {:input1 x :output1 (vector (even-parity? x))}) test-inputs)}))

;even-parity? takes in a list of variables and returns true if the number of true values in the input variables,
;as determined by the input-indices is even, and false otherwise.

(def instructions
  "A list of instructions which includes keyword strings
  with the format \"in + i\" where i is a number from 0 to num-vars-1
  concatenated with boolean and exec_if instructions and close."
  (vec (concat (for [i (range num-vars)] (keyword (str "in" i)))
               (take num-inputs
                     (cycle [:boolean_xor
                             :boolean_or
                             :boolean_and
                             :boolean_not
                             :exec_if
                             'close
                             ])))))

(defn error-function
  "Finds the behaviors and errors of an individual:
  Error is 0 if the value and the program’s selected behavior
  match, or 1 if they differ.
  The behavior is here defined as the final top item on the BOOLEAN stack."
  [argmap data individual]
  (let [program (genome/plushy->push (:plushy individual) argmap)
        inputs (map (fn [x] (:input1 x)) data)
        correct-outputs (map (fn [x] (first (:output1 x))) data)
        outputs (map (fn [input]
                       (state/peek-stack
                         (interpreter/interpret-program
                           program
                           (assoc state/empty-state
                             :input (zipmap (for [i (range (count input))]
                                              (keyword (str "in" i)))
                                            input))
                           (:step-limit argmap))
                         :boolean))
                     inputs)
        errors (map #(if (= %1 %2) 0 1)
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
       :training-data           (:train train-and-test-data)
       :testing-data            (:test train-and-test-data)
       :max-generations         500
       :population-size         500
       :max-initial-plushy-size 100
       :step-limit              200
       :parent-selection        :lexicase
       :tournament-size         5
       :umad-rate               0.1
       :variation               {:umad 0.5 :crossover 0.5}
       :elitism                 false}
      (apply hash-map (map #(if (string? %) (read-string %) %) args)))))
