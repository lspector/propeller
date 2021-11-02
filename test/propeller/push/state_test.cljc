(ns propeller.push.state-test
  (:require [clojure.test :as t]
            [propeller.push.state :as state]
            [propeller.push.utils.limits :as l]))

(t/deftest push-to-stack-test
  (t/is (= (state/push-to-stack {:integer '()} :integer 1)
           {:integer '(1)}))
  (t/is (= (state/push-to-stack {:integer '()} :integer 1e100)
           {:integer (list (long l/max-number-magnitude))})))

(t/deftest push-to-stack-many-test
  (t/is (= (state/push-to-stack-many {:string '()} :string ["a" "b" "c"])
           {:string '("a" "b" "c")})))
