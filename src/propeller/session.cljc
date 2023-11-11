; The "session" namespace is for trying things out interactively.
; For example, you can use it to test a new Push instruction by running a program that uses it and seeing the result.
; You might just want to do this interactively in the REPL, but the session file makes it a little easier since it already
; requires most of the namespaces you'll want to refer to.
; The commented-out stuff is a reminder of how to do some basic things.


(ns ^:no-doc propeller.session
  "The \"session\" namespace is for trying things out interactively.
  For example, you can use it to test a new Push instruction by running a program that uses it and seeing the result.
  You might just want to do this interactively in the REPL, but the session file makes it a little easier since it already
  requires most of the namespaces you'll want to refer to."
  (:require [propeller.genome :as genome]
            [propeller.gp :as gp]
            [propeller.selection :as selection]
            [propeller.variation :as variation]
            [propeller.push.instructions :as instructions]
            [propeller.push.interpreter :as interpreter]
            [propeller.push.state :as state]))

;; Interpreting a simple Push program:

#_(interpreter/interpret-program
    '(1 2 :integer_add) state/empty-state 1000)

;; Retaining history:

#_(interpreter/interpret-program
    '(1 2 :integer_add) (assoc state/empty-state :keep-history true) 1000)

;; A program with a conditional:

#_(interpreter/interpret-program
    '(3 3 :integer_eq :exec_if (1 "yes") (2 "no"))
    state/empty-state
    1000)

;; A program using an input instruction:

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

;; Making a random genome (plushy) using instructions with specified types,
;; and returning the Push program expressed by the genome:

#_(genome/plushy->push
    (genome/make-random-plushy (instructions/get-stack-instructions #{:float :integer :exec :boolean}) 20))

;; One way of running a genetic programming problem defined in the project
;; is to require the problem's namespace and then call `gp/gp` using the
;; items defined for the problem. Depending on your IDE and setup, you may
;; also have to open the problem's file and evaluate its contents.

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

;; Another way to run a problem defined within the project is to require
;; the problem's namespace and then call its `-main`. This will use defaults 
;; defined in the problem file:

#_(require '[propeller.problems.simple-regression :as regression])
#_(regression/-main)

;; Default values can be used but also partially overridden

#_(require '[propeller.problems.simple-regression :as regression])
#_(regression/-main :population-size 100 :variation {:umad 1.0})

