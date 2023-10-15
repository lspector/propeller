# Adding a Problem

In general, a problem file has 3 components: `train-and-test-data`, `instructions`, `error-function`, and `-main`.

1. To add a new problem, you need training and test data. For Problem Synthesis Benchmark Problems (PSB2),
you can fetch datasets using `psb2.core/fetch-examples`.

```clojure
(defn fetch-examples
  "Fetches and returns training and test data from a PSB2 problem.
   Returns a map of the form {:train training-examples :test testing-examples}
   where training-examples and testing-examples are lists of training and test
   data. The elements of these lists are maps of the form:
   {:input1 first-input :input2 second-input ... :output1 first-output ...}
   The training examples will include all hard-coded edge cases included in the suite,
   along with enough random cases to include `n-train` cases.
   Note that this function loads large datasets and can be slow, 30-120 seconds.
   Parameters:
     `datasets-directory` - Location of the PSB2 datasets as downloaded from https://zenodo.org/record/4678739
     `problem-name` - Name of the PSB2 problem, lowercase and seperated by dashes.
         - Ex: indices-of-substring
     `n-train` - Number of training cases to return
     `n-test` - Number of test cases to return"
  [datasets-directory problem-name n-train n-test]

```
2. Define the possible Push instructions to be used to create plushys. It should be a non-lazy list of 
instructions from `push/instructions`, input instructions, close, and constants (including functions that produce constants).
3. Define an error function that will evaluate plushys and add `:behaviors parsed-outputs`, 
   `:errors`, and `:total-error` to the individual
4. Define the function `-main` with a map of default arguments.

## Example of a Problem

```clojure
(ns propeller.problems.PSB2.solve-boolean
  (:require [psb2.core :as psb2]
            [propeller.genome :as genome]
            [propeller.push.interpreter :as interpreter]
            [propeller.utils :as utils]
            [propeller.push.instructions :refer [get-stack-instructions]]
            [propeller.push.state :as state]
            [propeller.gp :as gp]
            #?(:cljs [cljs.reader :refer [read-string]])))

; ===========  PROBLEM DESCRIPTION  ================================
; SOLVE BOOLEAN from PSB2
; Given a string representing a Boolean
; expression consisting of T, F, |, and &, evaluate it and return
; the resulting Boolean.
;
; Source: https://arxiv.org/pdf/2106.06086.pdf
; ==================================================================

(def train-and-test-data (psb2/fetch-examples "data" "solve-boolean" 200 2000))

(def instructions
  (utils/not-lazy
    (concat
      ;;; stack-specific instructions
      (get-stack-instructions #{:exec :integer :boolean :char :string :print})
      ;;; input instructions
      (list :in1)
      ;;; close
      (list 'close)
      ;;; ERCs (constants)
      (list true false \t \f \& \|))))

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
                         :boolean))
                     inputs)
        parsed-outputs (map (fn [output]
                              (try (read-string output)
                                   #?(:clj  (catch Exception e 1000.0)
                                      :cljs (catch js/Error. e 1000.0))))
                            outputs)
        errors (map (fn [correct-output output]
                      (if (= output :no-stack-item)
                        10000
                        (if (= correct-output output)
                           0
                           1)))
                    correct-outputs
                    parsed-outputs)]
    (assoc individual
      :behaviors parsed-outputs
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
       :max-initial-plushy-size 250
       :step-limit              2000
       :parent-selection        :lexicase
       :tournament-size         5
       :umad-rate               0.1
       :variation               {:umad 1.0 :crossover 0.0}
       :elitism                 false}
      (apply hash-map (map #(if (string? %) (read-string %) %) args)))))

```