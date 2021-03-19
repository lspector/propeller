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
        end-state ((:string_conj_char @core/instruction-table) start-state)
        expected-result (str value char)]
    (= expected-result
       (state/peek-stack end-state :string))))

(defspec conj-char-spec 100
  (prop/for-all [str gen/string
                 char gen/char]
                (check-conj-char str char)))


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


;; string/contains-char

(defn check-contains-char
  [value char]
  (let [start-state (-> state/empty-state
                        (state/push-to-stack :string value)
                        (state/push-to-stack :char char))
        end-state ((:string_contains_char @core/instruction-table) start-state)
        expected-result (string/includes? value (str char))]
    (= expected-result
       (state/peek-stack end-state :boolean))))

(defspec contains-char-spec 100
  (prop/for-all [str gen/string
                 char gen/char]
                (check-contains-char str char)))


;; string/drop

(defn check-drop
  [value n]
  (let [start-state (-> state/empty-state
                        (state/push-to-stack :string value)
                        (state/push-to-stack :integer n))
        end-state ((:string_drop @core/instruction-table) start-state)
        expected-result (apply str (drop n value))]
    (= expected-result
       (state/peek-stack end-state :string))))

(defspec drop-spec 100
  (prop/for-all [str gen/string
                 int gen/small-integer]
                (check-drop str int)))


;; string/empty-string

(defn check-empty-string
  [value]
  (let [start-state (state/push-to-stack state/empty-state :string value)
        end-state ((:string_empty_string @core/instruction-table) start-state)
        expected-result (empty? value)]
    (= expected-result
       (state/peek-stack end-state :boolean))))

(defspec empty-string-spec 100
  (prop/for-all [str gen/string]
                (check-empty-string str)))


;; string/first

(defn check-first
  [value]
  (let [start-state (state/push-to-stack state/empty-state :string value)
        end-state ((:string_first @core/instruction-table) start-state)
        expected-result (first value)]
    (or
     (and (empty? value)
          (= (state/peek-stack end-state :string) value)
          (state/empty-stack? end-state :char))
     (and (= expected-result
             (state/peek-stack end-state :char))
          (state/empty-stack? end-state :string)))))

(defspec first-spec 100
  (prop/for-all [str gen/string]
                (check-first str)))