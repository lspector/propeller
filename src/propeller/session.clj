(ns propeller.session
  (:require [propeller.genome :as genome]
            [propeller.gp :as gp]
            [propeller.selection :as selection]
            [propeller.variation :as variation]
            [propeller.problems.simple-regression :as regression]
            [propeller.problems.string-classification :as string-classif]
            [propeller.push.core :as push]
            [propeller.push.interpreter :as interpreter]
            [propeller.push.state :as state]))

#_(interpreter/interpret-program
    '(1 2 integer_add) state/empty-state 1000)

#_(interpreter/interpret-program
    '(3 5 :integer_eq :exec_if (1 "yes") (2 "no"))
    state/empty-state
    1000)

#_(interpreter/interpret-program
    '(in1 :string_reverse 1 :string_take "?" :string_eq :exec_if
          (in1 " I am asking." :string_concat)
          (in1 " I am saying." :string_concat))
    (assoc state/empty-state :input {:in1 "Can you hear me?"})
    1000)

#_(interpreter/interpret-program
    '(in1 :string_reverse 1 :string_take "?" :string_eq :exec_if
          (in1 " I am asking." :string_concat)
          (in1 " I am saying." :string_concat))
    (assoc state/empty-state :input {:in1 "I can hear you."})
    1000)

#_(genome/plushy->push
    (genome/make-random-plushy push/default-instructions 20))

#_(interpreter/interpret-program
    (genome/plushy->push
      (genome/make-random-plushy push/default-instructions 20))
    (assoc state/empty-state :input {:in1 "I can hear you."})
    1000)

;; =============================================================================
;; Target function: f(x) = x^3 + x + 3
;; =============================================================================

#_(gp/gp {:instructions            push/default-instructions
          :error-function          regression/error-function
          :max-generations         50
          :population-size         200
          :max-initial-plushy-size 50
          :step-limit              100
          :parent-selection        :tournament
          :tournament-size         5})

#_(gp/gp {:instructions            push/default-instructions
          :error-function          string-classif/error-function
          :max-generations         50
          :population-size         200
          :max-initial-plushy-size 50
          :step-limit              100
          :parent-selection        :lexicase})
