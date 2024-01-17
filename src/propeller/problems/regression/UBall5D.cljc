(ns propeller.problems.regression.UBall5D
  (:require [propeller.genome :as genome]
            [propeller.push.interpreter :as interpreter]
            [propeller.push.state :as state]
            [propeller.tools.math :as math]
            [propeller.gp :as gp]
            #?(:cljs [cljs.reader :refer [read-string]])))

(defn- target-function
  "Target function: f(x) = 10/(5 + SUM_i=1^5 (x_i - 3)^2)"
  [x1 x2 x3 x4 x5]
  (->> (list x1 x2 x3 x4 x5)
       (map #(* (- % 3) (- % 3)))
       (apply +)
       (+ 5)
       (/ 10)))

(target-function 3 7 3 3 3)

(defn map-vals-input
  "Returns all the input values of a map"
  [i]
  (vals (select-keys i [:input1 :input2 :input3 :input4 :input5])))


(defn get-random-input
  "returns a random input between two ranges"
  [a b]
  (->> (rand)
       (* (- b a))
       (+ a)
       (float)))

(def train-data
  (repeatedly 1024 (fn [] (repeatedly 5 #(get-random-input 0.05 6.05)))))

(def test-data
  (repeatedly 5000 (fn [] (repeatedly 5 #(get-random-input -0.25 6.35)))))

(def train-and-test-data
  (let [train-inputs train-data
        test-inputs test-data]
    {:train (map (fn [x] {:input1 (first x)
                          :input2 (nth x 1)
                          :input3 (nth x 2)
                          :input4 (nth x 3)
                          :input5 (nth x 4)
                          :output1 (apply target-function (map #(nth x %) (range 5)))}) train-inputs)
     :test (map (fn [x] {:input1 (first x)
                          :input2 (nth x 1)
                          :input3 (nth x 2)
                          :input4 (nth x 3)
                          :input5 (nth x 4)
                          :output1 (apply target-function (map #(nth x %) (range 5)))}) test-inputs)}))

(def instructions 
  (list :in1
        :in2
        :in3
        :in4
        :in5
        :float_add
        :float_subtract
        :float_mult
        :float_quot
        :float_eq
        :exec_dup
        :exec_if
        'close
        0.0
        1.0))

(def data (:train train-and-test-data))

(defn error-function
  "Finds the behaviors and errors of an individual. The error is the absolute
  deviation between the target output value and the program's selected behavior,
  or 1000000 if no behavior is produced. The behavior is here defined as the
  final top item on the FLOAT stack."
  ([argmap data individual]
   (let [program (genome/plushy->push (:plushy individual) argmap)
         inputs (map (fn [i] (map-vals-input i)) data)
         correct-outputs (map (fn [x] (:output1 x)) data)
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
                          :float))
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
                       :cljs (apply + errors))))))

(defn -main
  "Runs propel-gp, giving it a map of arguments."
  [& args]
  (gp/gp
    (merge
      {:instructions             instructions
       :error-function           error-function
       :training-data            (:train train-and-test-data)
       :testing-data             (:test train-and-test-data)
       :max-generations          300
       :population-size          1000
       :max-initial-plushy-size  100
       :step-limit               200
       :parent-selection         :lexicase
       :tournament-size          5
       :umad-rate                0.1
       :solution-error-threshold  0.1
       :variation                {:umad 1.0 :crossover 0.0}
       :elitism                  false}
      (apply hash-map (map #(if (string? %) (read-string %) %) args))))
  (#?(:clj shutdown-agents)))
