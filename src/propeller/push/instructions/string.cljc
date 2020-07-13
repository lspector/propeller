(ns propeller.push.instructions.string
  (:require [propeller.push.utils.helpers :refer [make-instruction]]
            [propeller.push.utils.macros :refer [def-instruction]]))

;; =============================================================================
;; STRING Instructions
;; =============================================================================

(def-instruction
  :string_=
  ^{:stacks #{:boolean :string}}
  (fn [state]
    (make-instruction state = [:string :string] :boolean)))

(def-instruction
  :string_concat
  ^{:stacks #{:string}}
  (fn [state]
    (make-instruction state #(apply str (concat %1 %2)) [:string :string] :string)))

(def-instruction
  :string_drop
  ^{:stacks #{:integer :string}}
  (fn [state]
    (make-instruction state #(apply str (drop %1 %2)) [:integer :string] :string)))

(def-instruction
  :string_includes?
  ^{:stacks #{:boolean :string}}
  (fn [state]
    (make-instruction state clojure.string/includes? [:string :string] :boolean)))

(def-instruction
  :string_length
  ^{:stacks #{:integer :string}}
  (fn [state]
    (make-instruction state count [:string] :integer)))

(def-instruction
  :string_reverse
  ^{:stacks #{:string}}
  (fn [state]
    (make-instruction state #(apply str (reverse %)) [:string] :string)))

(def-instruction
  :string_take
  ^{:stacks #{:integer :string}}
  (fn [state]
    (make-instruction state #(apply str (take %1 %2)) [:integer :string] :string)))
