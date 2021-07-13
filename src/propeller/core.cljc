(ns propeller.core
  #?(:clj (:gen-class))
  (:require [propeller.gp :as gp]
            [propeller.problems.simple-regression :as regression]
            [propeller.problems.string-classification :as string-classif]
            [clojure.string :as string]
            #?(:cljs [cljs.reader :refer [read-string]])))

(defn eval-problem-var
  [problem-name var-name]
  (eval (symbol (str "propeller.problems." problem-name "/" var-name))))

(defn -main
  "Runs propel-gp, giving it a map of arguments."
  [& args]
  ;; Exception for when no args were passed
  (when (empty? args)
    (println "You must specify a problem to run.")
    (println "Try, for example:")
    (println "   lein run software.smallest")
    (System/exit 1))

  ;; Creates problems
  (require (symbol (str "propeller.problems." (first args))))
  (gp/gp
    (update-in
      (merge
        {:instructions            (eval-problem-var (first args) "instructions")
         :error-function          (eval-problem-var (first args) "error-function")
         :max-generations         500
         :population-size         500
         :max-initial-plushy-size 100
         :step-limit              200
         :parent-selection        :lexicase
         :tournament-size         5
         :umad-rate               0.1
         :variation               {:umad 0.5 :crossover 0.5}
         :elitism                 false
         :PSB2-path               ""
         :PSB2-problem            (clojure.string/replace (first args) #"PSB2." "")}
        (apply hash-map
               (map #(if (and (string? %) (not (.contains % "/"))) (read-string %) %)
                    (rest args))))
      [:error-function]
      identity)))