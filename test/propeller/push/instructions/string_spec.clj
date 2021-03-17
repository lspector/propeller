(ns propeller.push.instructions.string-spec
  (:require
   [clojure.string :as string]
   [clojure.test.check.generators :as gen]
   [clojure.test.check.properties :as prop]
   [clojure.test.check.clojure-test :as ct :refer [defspec]]
   [propeller.push.state :as state]
   [propeller.push.core :as core]
   [propeller.push.instructions.string :as string-instructions]))


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
  (prop/for-all [str gen/string] 
                (check-butlast str)))


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
  (prop/for-all [str1 gen/string
                 str2 gen/string]
                (check-concat str1 str2)))


;; string/conj-char

(defn check-conj-char
  [value char]
  (let [start-state (-> state/empty-state
                        (state/push-to-stack :string value)
                        (state/push-to-stack :char char))
        end-state ((:string_conj-char @core/instruction-table) start-state)
        expected-result (str value char)]
    (= expected-result
       (state/peek-stack end-state :string))))

(defspec conj-char-spec 100
  (prop/for-all [str gen/string
                 char gen/char]
                (check-concat str char)))


;; string/contains

(defn check-contains
  [value1 value2]
  (let [start-state (-> state/empty-state
                        (state/push-to-stack :string value1)
                        (state/push-to-stack :string value2))
        end-state ((:string_contains @core/instruction-table) start-state)
        expected-result (string/includes? value2 value1)]
    (= expected-result
       (state/peek-stack end-state :boolean))))

(defspec contains-spec 100
  (prop/for-all [str1 gen/string
                 str2 gen/string]
                (check-contains str1 str2)))