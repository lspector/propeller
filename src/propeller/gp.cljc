(ns propeller.gp
  (:require [clojure.string]
            [propeller.genome :as genome]
            [propeller.variation :as variation]
            [propeller.push.instructions.bool]
            [propeller.push.instructions.character]
            [propeller.push.instructions.code]
            [propeller.push.instructions.input-output]
            [propeller.push.instructions.numeric]
            [propeller.push.instructions.polymorphic]
            [propeller.push.instructions.string]
            [propeller.push.instructions.vector]))

(defn report
  "Reports information each generation."
  [pop generation argmap]
  (let [best (first pop)]
    (println "-------------------------------------------------------")
    (println "               Report for Generation" generation)
    (println "-------------------------------------------------------")
    (print "Best plushy: ") (prn (:plushy best))
    (print "Best program: ") (prn (genome/plushy->push (:plushy best) argmap))
    (println "Best total error:" (:total-error best))
    (println "Best errors:" (:errors best))
    (println "Best behaviors:" (:behaviors best))
    (println "Genotypic diversity:"
             (float (/ (count (distinct (map :plushy pop))) (count pop))))
    (println "Behavioral diversity:"
             (float (/ (count (distinct (map :behaviors pop))) (count pop))))
    (println "Average genome length:"
             (float (/ (reduce + (map count (map :plushy pop))) (count pop))))
    (println "Average total error:"
             (float (/ (reduce + (map :total-error pop)) (count pop))))
    (println)))

(defn gp
  "Main GP loop."
  [{:keys [population-size max-generations error-function instructions
           max-initial-plushy-size]
    :as   argmap}]
  ;;
  (println "Starting GP with args: " argmap)
  ;;
  (loop [generation 0
         population (repeatedly
                      population-size
                      #(hash-map :plushy (genome/make-random-plushy
                                           instructions
                                           max-initial-plushy-size)))]
    (let [evaluated-pop (sort-by :total-error
                                 (#?(:clj  pmap
                                     :cljs map)
                                   (partial error-function argmap) population))
          best-individual (first evaluated-pop)]
      (report evaluated-pop generation argmap)
      (cond
        ;; Success on training cases is verified on testing cases
        (zero? (:total-error best-individual))
        (do (println "SUCCESS at generation" generation)
            (print "Checking program on test cases... ")
            (if (zero? (:total-error (error-function argmap best-individual :test)))
              (println "Test cases passed.")
              (println "Test cases failed."))
            ;(#?(:clj shutdown-agents))
            )
        ;;
        (>= generation max-generations)
        nil
        ;;
        :else (recur (inc generation)
                     (if (:elitism argmap)
                       (conj (repeatedly (dec population-size)
                                         #(variation/new-individual evaluated-pop argmap))
                             (first evaluated-pop))
                       (repeatedly population-size
                                   #(variation/new-individual evaluated-pop argmap))))))))
