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


;; string/from-boolean

(defn check-from-boolean
  [value]
  (let [start-state (state/push-to-stack state/empty-state :boolean value)
        end-state ((:string_from_boolean @core/instruction-table) start-state)
        expected-result (str value)]
    (= expected-result
       (state/peek-stack end-state :string))))

(defspec from-boolean-spec 10
  (prop/for-all [bool gen/boolean]
                (check-from-boolean bool)))


;; string/from-char

(defn check-from-char
  [value]
  (let [start-state (state/push-to-stack state/empty-state :char value)
        end-state ((:string_from_char @core/instruction-table) start-state)
        expected-result (str value)]
    (= expected-result
       (state/peek-stack end-state :string))))

(defspec from-char-spec 100
  (prop/for-all [char gen/char]
                (check-from-char char)))


;; string/from-float

(defn check-from-float
  [value]
  (let [start-state (state/push-to-stack state/empty-state :float value)
        end-state ((:string_from_float @core/instruction-table) start-state)
        expected-result (str value)]
    (= expected-result
       (state/peek-stack end-state :string))))

(defspec from-float-spec 100
  (prop/for-all [float gen/double]
                (check-from-float float)))


;; string/from-integer

(defn check-from-integer
  [value]
  (let [start-state (state/push-to-stack state/empty-state :integer value)
        end-state ((:string_from_integer @core/instruction-table) start-state)
        expected-result (str value)]
    (= expected-result
       (state/peek-stack end-state :string))))

(defspec from-integer-spec 100
  (prop/for-all [int gen/small-integer]
                (check-from-integer int)))


;; string/indexof-char

(defn check-indexof-char
  [value char]
  (let [start-state (-> state/empty-state
                        (state/push-to-stack :string value)
                        (state/push-to-stack :char char))
        end-state ((:string_indexof_char @core/instruction-table) start-state)
        expected-result (string/index-of value char)]
    (or
     (and (not expected-result)
          (= (state/peek-stack end-state :string) value)
          (= (state/peek-stack end-state :char) char)
          (state/empty-stack? end-state :integer))
     (= expected-result
        (state/peek-stack end-state :integer)))))

(defspec indexof-char-spec 100
  (prop/for-all [str gen/string
                 char gen/char]
                (check-indexof-char str char)))


;; string/iterate


;; string/last

(defn check-last
  [value]
  (let [start-state (state/push-to-stack state/empty-state :string value)
        end-state ((:string_last @core/instruction-table) start-state)
        expected-result (last value)]
    (or
     (and (empty? value)
          (state/empty-stack? end-state :char)
          (= value (state/peek-stack end-state :string)))
     (and (state/empty-stack? end-state :string)
          (= expected-result
             (state/peek-stack end-state :char))))))

(defspec last-spec 100
  (prop/for-all [str gen/string]
                (check-last str)))


;; string/length

(defn check-length
  [value]
  (let [start-state (state/push-to-stack state/empty-state :string value)
        end-state ((:string_length @core/instruction-table) start-state)
        expected-result (count value)]
    (= expected-result
       (state/peek-stack end-state :integer))))

(defspec length-spec 100
  (prop/for-all [str gen/string]
                (check-length str)))


;; string/nth

(defn check-nth
  [value n]
  (let [start-state (-> state/empty-state
                        (state/push-to-stack :string value)
                        (state/push-to-stack :integer n))
        end-state ((:string_nth @core/instruction-table) start-state)]
    (or
     (and (empty? value)
          (state/empty-stack? end-state :char)
          (= value (state/peek-stack end-state :string))
          (= n (state/peek-stack end-state :integer)))
     (= (nth value (mod n (count value)))
        (state/peek-stack end-state :char)))))

(defspec nth-spec 100
  (prop/for-all [str gen/string
                 int gen/small-integer]
                (check-nth str int)))


;; string/occurencesof_char

(defn check-occurencesof-char
  [value char]
  (let [start-state (-> state/empty-state
                        (state/push-to-stack :string value)
                        (state/push-to-stack :char char))
        end-state ((:string_occurencesof_char @core/instruction-table) start-state)
        expected-result (count (filter #(= char %) value))]
    (= expected-result
       (state/peek-stack end-state :integer))))

(defspec occurencesof-char-spec 100
  (prop/for-all [str gen/string
                 char gen/char]
                (check-occurencesof-char str char)))


;; string/parse-to-chars

(defn check-parse-to-chars
  [value]
  (let [start-state (state/push-to-stack state/empty-state :string value)
        end-state ((:string_parse_to_chars @core/instruction-table) start-state)
        ;; Since split will return the empty string when given the empty string
        string-length (if (= 0 (count value)) 1 (count value))
        expected-result (string/split value #"")]
    (and 
     (= expected-result
       (state/peek-stack-many end-state :string string-length))
     (-> end-state
         (state/pop-stack-many :string string-length)
         (state/empty-stack? :string)))))

(defspec parse-to-chars-spec 100
  (prop/for-all [str gen/string]
                (check-parse-to-chars str)))


;; string/remove-char

(defn check-remove-char
  [value char]
  (let [start-state (-> state/empty-state 
                        (state/push-to-stack :string value)
                        (state/push-to-stack :char char))
        end-state ((:string_remove_char @core/instruction-table) start-state)
        expected-result (apply str (filter #(not= char %) value))]
    (= expected-result
       (state/peek-stack end-state :string))))

(defspec remove-char-spec 100
  (prop/for-all [str gen/string
                 char gen/char]
                (check-remove-char str char)))


;; string/replace

(defn check-replace
  [value1 value2 value3]
  (let [start-state (-> state/empty-state
                        (state/push-to-stack :string value1)
                        (state/push-to-stack :string value2)
                        (state/push-to-stack :string value3))
        end-state ((:string_replace @core/instruction-table) start-state)
        expected-result (string/replace value1 value2 value3)]
    (= expected-result
       (state/peek-stack end-state :string))))

(defspec replace-spec 100
  (prop/for-all [str1 gen/string
                 str2 gen/string
                 str3 gen/string]
                (check-replace str1 str2 str3)))


;; string/replace-char

(defn check-replace-char
  [value char1 char2]
  (let [start-state (-> state/empty-state
                        (state/push-to-stack :string value)
                        (state/push-to-stack :char char1)
                        (state/push-to-stack :char char2))
        end-state ((:string_replace_char @core/instruction-table) start-state)
        expected-result (string/replace value char1 char2)]
    (= expected-result
       (state/peek-stack end-state :string))))

(defspec replace-char-spec 100
  (prop/for-all [str gen/string
                 char1 gen/char
                 char2 gen/char]
                (check-replace str char1 char2)))


;; string/replace-first

(defn check-replace-first
  [value1 value2 value3]
  (let [start-state (-> state/empty-state
                        (state/push-to-stack :string value1)
                        (state/push-to-stack :string value2)
                        (state/push-to-stack :string value3))
        end-state ((:string_replace_first @core/instruction-table) start-state)
        expected-result (string/replace-first value1 value2 value3)]
    (= expected-result
       (state/peek-stack end-state :string))))

(defspec replace-first-spec 100
  (prop/for-all [str1 gen/string
                 str2 gen/string
                 str3 gen/string]
                (check-replace-first str1 str2 str3)))