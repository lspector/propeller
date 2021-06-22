(ns propeller.core
  #?(:clj (:gen-class))
  (:require [propeller.gp :as gp]
            [propeller.problems.simple-regression :as regression]
            [propeller.problems.string-classification :as string-classif]
            #?(:cljs [cljs.reader :refer [read-string]])))

(defn eval-problem-var
  [problem-name var-name]
  (eval (symbol (str "propeller.problems." problem-name "/" var-name)))
  ;; Overload 2: PSB specific
  ;; Passes into eval
  [problem-name var-name PSB2-name]
  ((eval (symbol (str "propeller.problems." problem-name "/" var-name))) PSB2-name))

(defn -main
  "Runs propel-gp, giving it a map of arguments."
  [& args]
  ;; Exception for when no args were passed
  (when (empty? args)
    (println "You must specify a problem to run.")
    (println "Try, for example:")
    (println "   lein run software.smallest")
    (System/exit 1))

  ;; Setting the path for PSB2
  (when (= (first args) "PSB2-set-path")
    (spit "PSB2_path.txt" (second args))
    (println (str "Set path to PSB2 as " (second args)))
    (System/exit 1))

  ;; Creates PSB2 problems
  (when (= (first args) "PSB2")
    ;; For tomorrow, Figure out what this does?
    (require (symbol (str "propeller.problems.PSB2" (first args))))
    ;; For tomorrow, add the gp/gp function here
    ;; ---
    

  ;; Creates regular problems
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
         :elitism                 false}
        (apply hash-map
               (map #(if (string? %) (read-string %) %)
                    (rest args))))
      [:error-function]
      identity)))
