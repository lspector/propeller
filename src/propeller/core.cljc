(ns propeller.core
  #?(:clj (:gen-class))
  (:require [propeller.gp :as gp]
            [propeller.problems.simple-regression :as regression]
            [propeller.problems.string-classification :as string-classif]
            [propeller.problems.software.number-io :as number-io]
            [propeller.problems.software.smallest :as smallest]
            #?(:cljs [cljs.reader :refer [read-string]])))

(defn -main
  "Runs propel-gp, giving it a map of arguments."
  [& args]
  (gp/gp
    (update-in
      (merge
        {:instructions            smallest/instructions
         :error-function          smallest/error-function
         :max-generations         500
         :population-size         500
         :max-initial-plushy-size 100
         :step-limit              200
         :parent-selection        :lexicase
         :tournament-size         5
         :umad-rate               0.1
         :variation               {:umad 0.5 :crossover 0.5}
         :elitism                 false}
        (apply hash-map
               (map read-string args)))
      [:error-function]
      identity)))
