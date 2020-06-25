(ns propeller.session
  (:require (propeller [gp :as gp]
                       [variation :as variation]
                       [selection :as selection]
                       [genome :as genome])
            (propeller.push [interpreter :as interpreter]
                            [core :as push]
                            [state :as state])
            (propeller.problems [simple-regression :refer [regression-error-function]]
                                [string-classification :refer [string-classification-error-function]])))

#_(interpreter/interpret-program '(1 2 integer_+) empty-push-state 1000)

#_(interpreter/interpret-program '(3 5 integer_= exec_if (1 "yes") (2 "no"))
                     empty-push-state
                     1000)

#_(interpreter/interpret-program '(in1 string_reverse 1 string_take "?" string_= exec_if
                           (in1 " I am asking." string_concat)
                           (in1 " I am saying." string_concat))
                     (assoc empty-push-state :input {:in1 "Can you hear me?"})
                     1000)

#_(interpreter/interpret-program '(in1 string_reverse 1 string_take "?" string_= exec_if
                           (in1 " I am asking." string_concat)
                           (in1 " I am saying." string_concat))
                     (assoc empty-push-state :input {:in1 "I can hear you."})
                     1000)

#_(genome/plushy->push (genome/make-random-plushy push/default-instructions 20))

#_(interpreter/interpret-program (genome/plushy->push (genome/make-random-plushy push/default-instructions 20))
                     (assoc empty-push-state :input {:in1 "I can hear you."})
                     1000)

;; Target function: f(x) = x^3 + x + 3

#_(gp/gp {:instructions            push/default-instructions
       :error-function          regression-error-function
       :max-generations         50
       :population-size         200
       :max-initial-plushy-size 50
       :step-limit              100
       :parent-selection        :tournament
       :tournament-size         5})

#_(gp/gp {:instructions            push/default-instructions
       :error-function          string-classification-error-function
       :max-generations         50
       :population-size         200
       :max-initial-plushy-size 50
       :step-limit              100
       :parent-selection        :lexicase})
