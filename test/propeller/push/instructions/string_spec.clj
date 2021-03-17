(ns propeller.push.instructions.string-spec
  (:require
   [clojure.test.check.generators :as gen]
   [clojure.test.check.properties :as prop]
   [clojure.test.check.clojure-test :as ct :refer [defspec]]
   [propeller.push.state :as state]
   [propeller.push.core :as core]
   [propeller.push.instructions.string :as string]))


;; string/butlast

(defn check-butlast
  [value]
  (let [start-state (state/push-to-stack state/empty-state
                                         :string
                                         value)
        end-state ((:string_butlast @core/instruction-table) start-state)
        expected-result (apply str (butlast value))]
    (= expected-result
       (state/peek-stack end-state :string))))

(defspec butlast-spec 100
  (prop/for-all [s gen/string] 
                (check-butlast s)))


;; string/concat

(defn check-concat
  [value1 value2]
  (let [start-state (-> state/empty-state
                        (state/push-to-stack :string value1)
                        (state/push-to-stack :string value2))
        end-state ((:string_concat @core/instruction-table) start-state)
        expected-result (str value1 value2)]
    (= expected-result
       (state/peek-stack end-state :string))))
  
(defspec concat-spec 100
  (prop/for-all [s1 gen/string
                 s2 gen/string]
                (check-concat s1 s2)))