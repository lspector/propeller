(ns propeller.problems.valiant
  (:require [propeller.genome :as genome]
            [propeller.push.interpreter :as interpreter]
            [propeller.push.state :as state]))

(def num-vars 100) ;10) ;100)                                          ;1000)
(def num-inputs 50) ;5) ; 50)                                         ;500)
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
    {:train {:inputs  train-inputs
             :outputs (map even-parity? train-inputs)}
     :test  {:inputs  test-inputs
             :outputs (map even-parity? test-inputs)}}))

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
  ([argmap individual]
   (error-function argmap individual :train))
  ([argmap individual subset]
   (let [program (genome/plushy->push (:plushy individual) argmap)
         data (get train-and-test-data subset)
         inputs (:inputs data)
         correct-outputs (:outputs data)
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
                       :cljs (apply + errors))))))
