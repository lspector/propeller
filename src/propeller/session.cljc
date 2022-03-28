(ns propeller.session
  (:require [propeller.genome :as genome]
            [propeller.gp :as gp]
            [propeller.selection :as selection]
            [propeller.variation :as variation]
            [propeller.push.instructions :as instructions]
            [propeller.push.interpreter :as interpreter]
            [propeller.push.state :as state]))

#_(interpreter/interpret-program
    '(1 2 :integer_add) state/empty-state 1000)

#_(interpreter/interpret-program
    '(1 2 :integer_add) (assoc state/empty-state :keep-history true) 1000)

#_(interpreter/interpret-program
    '(3 3 :integer_eq :exec_if (1 "yes") (2 "no"))
    state/empty-state
    1000)

#_(interpreter/interpret-program
    '(:in1 :string_reverse 1 :string_take "?" :string_eq :exec_if
       (:in1 " I am asking." :string_concat)
       (:in1 " I am saying." :string_concat))
    (assoc state/empty-state :input {:in1 "Can you hear me?"})
    1000)

#_(interpreter/interpret-program
    '(:in1 :string_reverse 1 :string_take "?" :string_eq :exec_if
       (:in1 " I am asking." :string_concat)
       (:in1 " I am saying." :string_concat))
    (assoc state/empty-state :input {:in1 "I can hear you."})
    1000)

#_(genome/plushy->push
    (genome/make-random-plushy (instructions/get-stack-instructions #{:float :integer :exec :boolean}) 20))

#_(require '[propeller.problems.simple-regression :as regression])

#_(gp/gp {:instructions            regression/instructions
        :error-function          regression/error-function
        :training-data           (:train regression/train-and-test-data)
        :testing-data            (:test regression/train-and-test-data)
        :max-generations         500
        :population-size         500
        :max-initial-plushy-size 100
        :step-limit              200
        :parent-selection        :tournament
        :tournament-size         5
        :umad-rate               0.01
        :variation               {:umad      1.0
                                  :crossover 0.0}
        :elitism                 false})

#_(require '[propeller.problems.string-classification :as sc])

#_(gp/gp {:instructions            sc/instructions
          :error-function          sc/error-function
          :training-data           (:train sc/train-and-test-data)
          :testing-data            (:test sc/train-and-test-data)
          :max-generations         500
          :population-size         500
          :max-initial-plushy-size 100
          :step-limit              200
          :parent-selection        :lexicase
          :tournament-size         5
          :umad-rate               0.1
          :variation               {:umad 0.5 :crossover 0.5}
          :elitism                 false})

