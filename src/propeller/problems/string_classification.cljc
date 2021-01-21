(ns propeller.problems.string-classification
  (:require [propeller.genome :as genome]
            [propeller.push.interpreter :as interpreter]
            [propeller.push.state :as state]))

;; =============================================================================
;; String classification
;; =============================================================================

;; Set of original propel instructions
(def instructions
  (list :in1
        :integer_add
        :integer_subtract
        :integer_mult
        :integer_quot
        :integer_eq
        :exec_dup
        :exec_if
        :boolean_and
        :boolean_or
        :boolean_not
        :boolean_eq
        :string_eq
        :string_take
        :string_drop
        :string_reverse
        :string_concat
        :string_length
        :string_contains
        'close
        0
        1
        true
        false
        ""
        "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
        "A"
        "C"
        "G"
        "T"))

(defn train-and-test-data
  []
  (let [train-inputs ["GCG" "GACAG" "AGAAG" "CCCA" "GATTACA" "TAGG" "GACT"]
        test-inputs ["GCGT" "GACTTAG" "AGTAAG" "TCCTCA" "GAACA" "AGG" "GAC"]]
    {:train {:inputs  train-inputs
             :outputs [false false false false true true true]}
     :test  {:inputs  test-inputs
             :outputs [true true true true false false false]}}))

(defn error-function
  "Finds the behaviors and errors of an individual: Error is 0 if the value and
  the program's selected behavior match, or 1 if they differ, or 1000000 if no
  behavior is produced. The behavior is here defined as the final top item on
  the BOOLEAN stack."
  ([argmap individual]
   (error-function argmap individual :train))
  ([argmap individual subset]
   (let [program (genome/plushy->push (:plushy individual) argmap)
         data (get (train-and-test-data) subset)
         inputs (:inputs data)
         correct-outputs (:outputs data)
         outputs (map (fn [input]
                        (state/peek-stack
                          (interpreter/interpret-program
                            program
                            (assoc state/empty-state :input {:in1 input})
                            (:step-limit argmap))
                          :boolean))
                      inputs)
         errors (map (fn [correct-output output]
                       (if (= output :no-stack-item)
                         1000000
                         (if (= correct-output output)
                           0
                           1)))
                     correct-outputs
                     outputs)]
     (assoc individual
       :behaviors outputs
       :errors errors
       :total-error #?(:clj  (apply +' errors)
                       :cljs (apply + errors))))))
