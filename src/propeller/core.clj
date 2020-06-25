(ns propeller.core
  (:gen-class)
  (:require [propeller.gp :as gp]
            [propeller.problems.simple-regression :as regression]
            [propeller.problems.string-classification :as string-classif]
            [propeller.push.core :as push]))

(defn -main
  "Runs propel-gp, giving it a map of arguments."
  [& args]
  (gp/gp
    (update-in
      (merge
        {:instructions            push/default-instructions
         :error-function          regression/error-function
         :max-generations         500
         :population-size         500
         :max-initial-plushy-size 50
         :step-limit              100
         :parent-selection        :lexicase
         :tournament-size         5
         :umad-rate               0.1
         :variation               {:umad 0.5 :crossover 0.5}
         :elitism                 false}
        (apply hash-map
               (map read-string args)))
      [:error-function]
      #(if (fn? %) % (eval %)))))
