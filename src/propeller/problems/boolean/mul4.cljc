;; EXPERIMENTAL: Code in this file is still under development. It may be broken. Expect changes.

(ns propeller.problems.boolean.mul4
  (:require [propeller.genome :as genome]
            [propeller.push.interpreter :as interpreter]
            [propeller.push.state :as state]
            [propeller.gp :as gp]
            [propeller.push.instructions :refer [def-instruction
                                                 make-instruction]]
            #?(:cljs [cljs.reader :refer [read-string]])))

(defn target-function
  "Returns a vector of 8 bits (booleans) for the product of the numbers
   a and b, which should be provided as 4 booleans each."
  [a3 a2 a1 a0 b3 b2 b1 b0]
  (let [a (+ (if a3 8 0)
             (if a2 4 0)
             (if a1 2 0)
             (if a0 1 0))
        b (+ (if b3 8 0)
             (if b2 4 0)
             (if b1 2 0)
             (if b0 1 0))
        product (* a b)]
    (loop [bit-index 7
           product-bits {}
           remainder product]
      (if (< bit-index 0)
        product-bits
        (let [pow2 (bit-shift-left 1 bit-index)
              this-bit (>= remainder pow2)]
          (recur (dec bit-index)
                 (assoc product-bits (keyword (str "c" bit-index)) this-bit)
                 (- remainder (* (if this-bit  1 0) pow2))))))))

(def train-and-test-data
  (let [bools [false true]]
    {:train (for [a3 bools
                  a2 bools
                  a1 bools
                  a0 bools
                  b3 bools
                  b2 bools
                  b1 bools
                  b0 bools]
              {:inputs {:a3 a3 :a2 a2 :a1 a1 :a0 a0 :b3 b3 :b2 b2 :b1 b1 :b0 b0}
               :outputs (target-function a3 a2 a1 a0 b3 b2 b1 b0)})
     :test []}))

(def-instruction
  :set-c7
  ^{:stacks [:boolean :output]}
  (fn [state]
    (if (state/empty-stack? state :boolean)
      state
      (assoc-in (state/pop-stack state :boolean)
                [:output :c7]
                (state/peek-stack state :boolean)))))

(def-instruction
  :set-c6
  ^{:stacks [:boolean :output]}
  (fn [state]
    (if (state/empty-stack? state :boolean)
      state
      (assoc-in (state/pop-stack state :boolean)
                [:output :c6]
                (state/peek-stack state :boolean)))))

(def-instruction
  :set-c5
  ^{:stacks [:boolean :output]}
  (fn [state]
    (if (state/empty-stack? state :boolean)
      state
      (assoc-in (state/pop-stack state :boolean)
                [:output :c5]
                (state/peek-stack state :boolean)))))

(def-instruction
  :set-c4
  ^{:stacks [:boolean :output]}
  (fn [state]
    (if (state/empty-stack? state :boolean)
      state
      (assoc-in (state/pop-stack state :boolean)
                [:output :c4]
                (state/peek-stack state :boolean)))))

(def-instruction
  :set-c3
  ^{:stacks [:boolean :output]}
  (fn [state]
    (if (state/empty-stack? state :boolean)
      state
      (assoc-in (state/pop-stack state :boolean)
                [:output :c3]
                (state/peek-stack state :boolean)))))

(def-instruction
  :set-c2
  ^{:stacks [:boolean :output]}
  (fn [state]
    (if (state/empty-stack? state :boolean)
      state
      (assoc-in (state/pop-stack state :boolean)
                [:output :c2]
                (state/peek-stack state :boolean)))))

(def-instruction
  :set-c1
  ^{:stacks [:boolean :output]}
  (fn [state]
    (if (state/empty-stack? state :boolean)
      state
      (assoc-in (state/pop-stack state :boolean)
                [:output :c1]
                (state/peek-stack state :boolean)))))

(def-instruction
  :set-c0
  ^{:stacks [:boolean :output]}
  (fn [state]
    (if (state/empty-stack? state :boolean)
      state
      (assoc-in (state/pop-stack state :boolean)
                [:output :c0]
                (state/peek-stack state :boolean)))))

(def-instruction
  :boolean_bufa
  ^{:stacks #{:boolean}}
  (fn [state]
    (make-instruction state 
                      (fn [b1 b2] b1) 
                      [:boolean :boolean]
                      :boolean)))


(def-instruction
  :boolean_nota
  ^{:stacks #{:boolean}}
  (fn [state]
    (make-instruction state 
                      (fn [b1 b2] (not b1)) 
                      [:boolean :boolean] 
                      :boolean)))


(def-instruction
  :boolean_nand
  ^{:stacks #{:boolean}}
  (fn [state]
    (make-instruction state
                      (fn [b1 b2] (not (and b1 b2)))
                      [:boolean :boolean] 
                      :boolean)))

(def-instruction
  :boolean_nor
  ^{:stacks #{:boolean}}
  (fn [state]
    (make-instruction state
                      (fn [b1 b2] (not (or b1 b2)))
                      [:boolean :boolean]
                      :boolean)))

(def-instruction
  :boolean_xnor
  ^{:stacks #{:boolean}}
  (fn [state]
    (make-instruction state
                      (fn [b1 b2] (= b1 b2))
                      [:boolean :boolean]
                      :boolean)))

(def instructions
  (list :a3
        :a2
        :a1
        :a0
        :b3
        :b2
        :b1
        :b0
        :set-c7 ;; defined here
        :set-c6 ;; defined here
        :set-c5 ;; defined here
        :set-c4 ;; defined here
        :set-c3 ;; defined here
        :set-c2 ;; defined here
        :set-c1 ;; defined here
        :set-c0 ;; defined here
        ;; Recommended by Kalkreuth et al: BUFa, NOTa, AND, OR, XOR, NAND, NOR, XNOR
        :boolean_bufa ;; defined here
        :boolean_nota ;; defined here
        :boolean_and
        :boolean_or
        :boolean_xor
        :boolean_nand ;; defined here
        :boolean_nor ;; defined here
        :boolean_xnor ;; defined here
        :boolean_dup
        :boolean_swap
        :boolean_rot
        :boolean_pop
        :integer_dup ;; will be a noop
        :exec_pop
        true
        false))



(defn error-function
  [argmap data individual]
  (let [program (genome/plushy->push (:plushy individual) argmap)
        input-maps (mapv :inputs data)
        correct-output-maps (mapv :outputs data)
        output-maps (mapv (fn [input-map]
                            (:output
                             (interpreter/interpret-program
                              program
                              (assoc state/empty-state
                                     :input input-map
                                     :output {:c7 :unset
                                              :c6 :unset
                                              :c5 :unset
                                              :c4 :unset
                                              :c3 :unset
                                              :c2 :unset
                                              :c1 :unset
                                              :c0 :unset})
                              (:step-limit argmap))))
                          input-maps)
        errors (flatten (map (fn [correct-output-map output-map]
                               (mapv (fn [k]
                               ; no answer same as wrong answer
                                       (if (= (k correct-output-map)
                                              (k output-map))
                                         0
                                         1))
                                     [:c7 :c6 :c5 :c4 :c3 :c2 :c1 :c0]))
                             correct-output-maps
                             output-maps))]
    (assoc individual
           :behaviors output-maps
           :errors errors
           :total-error #?(:clj  (apply +' errors)
                           :cljs (apply + errors)))))

(defn -main
  "Runs the top-level genetic programming function, giving it a map of 
  arguments with defaults that can be overridden from the command line
  or through a passed map."
  [& args]
  (gp/gp
   (merge
    {:instructions             instructions
     :error-function           error-function
     :training-data            (:train train-and-test-data)
     :testing-data             (:test train-and-test-data)
     :max-generations          100
     :population-size          100
     :max-initial-plushy-size  10
     :step-limit               1000
     :parent-selection         :lexicase
     :tournament-size          5
     :umad-rate                0.01
     :variation                {:umad 1.0 :crossover 0.0}
     :elitism                  false}
    (apply hash-map (map #(if (string? %) (read-string %) %) args)))))
