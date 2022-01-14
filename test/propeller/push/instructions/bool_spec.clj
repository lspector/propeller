(ns propeller.push.instructions.bool-spec
  (:require
    ;[clojure.boolean :as bool]
    [clojure.test.check.generators :as gen]
    [clojure.test.check.properties :as prop]
    [clojure.test.check.clojure-test :as ct :refer [defspec]]
    [propeller.push.state :as state]
    [propeller.push.instructions :as instructions]
    [propeller.push.instructions.string :as string-instructions]
    [propeller.push.interpreter :as interpreter]))

;;boolean/and
(defn check-and
  [value1 value2]
  (let [start-state (-> state/empty-state
                        (state/push-to-stack :boolean value1)
                        (state/push-to-stack :boolean value2))
        end-state ((:boolean_and @instructions/instruction-table) start-state)
        expected-result (and value1 value2)]
    (= expected-result
       (state/peek-stack end-state :boolean))))

;; boolean/or
(defn check-or
  [value1 value2]
  (let [start-state (-> state/empty-state
                        (state/push-to-stack :boolean value1)
                        (state/push-to-stack :boolean value2))
        end-state ((:boolean_or @instructions/instruction-table) start-state)
        expected-result (or value1 value2)]
    (= expected-result
       (state/peek-stack end-state :boolean))))