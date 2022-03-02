(ns propeller.problems.valiant
  (:require [propeller.genome :as genome]
            [propeller.push.interpreter :as interpreter]
            [propeller.push.state :as state]
            [propeller.gp :as gp]
            #?(:cljs [cljs.reader :refer [read-string]])))

(def num-vars 100)                                          ;10) ;100)                                          ;1000)
(def num-inputs 50)                                         ;5) ; 50)                                         ;500)
(def num-train 500)                                         ;5000)
(def num-test 200)

(def train-and-test-data
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

(def instructions
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
  "Runs propel-gp, giving it a map of arguments."
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
      (apply hash-map (map #(if (string? %) (read-string %) %) args))))
  (#?(:clj shutdown-agents)))
