(ns propeller.problems.PSB1.scrabble-score
  (:require [psb2.core :as psb2]
            [propeller.genome :as genome]
            [propeller.push.interpreter :as interpreter]
            [clojure.string :as string]
            [propeller.tools.math :as math]
            [propeller.problems.data-creation :as dc]
            [propeller.utils :as utils]
            [propeller.push.instructions :refer [get-stack-instructions]]
            [propeller.push.state :as state]
            [propeller.tools.metrics :as metrics]
            [propeller.gp :as gp]
            #?(:cljs [cljs.reader :refer [read-string]])))

(def train-and-test-data (psb2/fetch-examples "data" "scrabble-score" 200 2000))
(def train-data (:train train-and-test-data))
(def test-data (:test train-and-test-data))

(def scrabble-letter-values
  (let [scrabble-map {\a 1
                      \b 3
                      \c 3
                      \d 2
                      \e 1
                      \f 4
                      \g 2
                      \h 4
                      \i 1
                      \j 8
                      \k 5
                      \l 1
                      \m 3
                      \n 1
                      \o 1
                      \p 3
                      \q 10
                      \r 1
                      \s 1
                      \t 1
                      \u 1
                      \v 4
                      \w 4
                      \x 8
                      \y 4
                      \z 10}
        visible-chars (map char (range 0 127))]
    (vec (for [c visible-chars]
           (get scrabble-map (first (string/lower-case c)) 0)))))

;; scrabble-letter-values

;; (def program '(0 :in1 :string_iterate (:integer_from_char [0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 1 3 3 2 1 4 2 4 1 8 5 1 3 1 1 3 10 1 1 1 1 4 4 8 4 10 0 0 0 0 0 0 1 3 3 2 1 4 2 4 1 8 5 1 3 1 1 3 10 1 1 1 1 4 4 8 4 10 0 0 0 0] :vector_integer_nth :integer_add)))

;; (def inputs (map (fn [i] (get i :input1)) test-data))
;; test-data
;; (def correct-outputs (map (fn [i] (get i :output1)) test-data))

;; correct-outputs
;; inputs

;; (def outputs (map (fn [input]
;;                     (state/peek-stack
;;                      (interpreter/interpret-program
;;                       program
;;                       (assoc state/empty-state :input {:in1 input})
;;                       200)
;;                      :integer))
;;                   inputs))

;; outputs
;; correct-outputs

;; (def errors (map (fn [correct-output output]
;;                    (if (= output :no-stack-item)
;;                      1000000
;;                      (math/abs (- correct-output output))))
;;                  correct-outputs
;;                  outputs))

;; (apply + errors)

;; (defn index-of [item coll]
;;   (count (take-while (partial not= item) coll)))

;; (index-of 1 errors)
;; (nth inputs 647)
;; (nth outputs 647)
;; (nth correct-outputs 647)

(def instructions
  (utils/not-lazy
   (concat
      ;;; stack-specific instructions
    (get-stack-instructions #{:exec :integer :boolean :char :vector_integer :string})
      ;;; input instructions
    (list :in1)
      ;;; close
    (list 'close)
      ;;; ERCs (constants)
    (list scrabble-letter-values))))

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
