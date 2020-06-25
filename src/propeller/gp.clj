(ns propeller.gp
  (:require [propeller.genome :as genome]
            [propeller.variation :as variation]
            [propeller.push.core :as push]
            [propeller.push.instructions.boolean]
            [propeller.push.instructions.char]
            [propeller.push.instructions.code]
            [propeller.push.instructions.input-output]
            [propeller.push.instructions.numeric]
            [propeller.push.instructions.polymorphic]
            [propeller.push.instructions.string]))

(defn report
  "Reports information each generation."
  [pop generation]
  (let [best (first pop)]
    (println "-------------------------------------------------------")
    (println "               Report for Generation" generation)
    (println "-------------------------------------------------------")
    (print "Best plushy: ") (prn (:plushy best))
    (print "Best program: ") (prn (genome/plushy->push (:plushy best)))
    (println "Best total error:" (:total-error best))
    (println "Best errors:" (:errors best))
    (println "Best behaviors:" (:behaviors best))
    (println "Genotypic diversity:"
             (float (/ (count (distinct (map :plushy pop))) (count pop))))
    (println "Average genome length:"
             (float (/ (reduce + (map count (map :plushy pop))) (count pop))))
    (println)))

(defn gp
  "Main GP loop."
  [{:keys [population-size max-generations error-function instructions
           max-initial-plushy-size]
    :as   argmap}]
  ;;
  (println "Starting GP with args: " argmap)
  ;;
  (do (println "Registered instructions:")
      (println (sort (keys @push/instruction-table))))
  ;;
  (loop [generation 0
         population (repeatedly
                      population-size
                      #(hash-map :plushy
                                 (genome/make-random-plushy instructions
                                                            max-initial-plushy-size)))]
    (let [evaluated-pop (sort-by :total-error
                                 (map (partial error-function argmap)
                                      population))]
      (report evaluated-pop generation)
      (cond
        (zero? (:total-error (first evaluated-pop))) (println "SUCCESS")
        (>= generation max-generations) nil
        :else (recur (inc generation)
                     (if (:elitism argmap)
                       (conj (repeatedly (dec population-size)
                                         #(variation/new-individual evaluated-pop argmap))
                             (first evaluated-pop))
                       (repeatedly population-size
                                   #(variation/new-individual evaluated-pop argmap))))))))
