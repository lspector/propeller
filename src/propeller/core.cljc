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
  ;; * default params changed to PSB2 defaults *
  (require (symbol (str "propeller.problems." (first args))))
  ;(when (string/includes? (first args) "PSB2")
  ;  (spit "PSB2_path.txt" (second args))
  ;  (println (str "Set path to PSB2 as " (second args))))
  (gp/gp
    (update-in
      (merge
        {:instructions            (eval-problem-var (first args) "instructions")
         :error-function          (eval-problem-var (first args) "error-function")
         :max-generations         300
         :population-size         1000
         :max-initial-plushy-size 250
         :step-limit              2000
         :parent-selection        :lexicase
         :tournament-size         5
         :umad-rate               0.1
         :variation               {:umad 1.0 :crossover 0.0}
         :elitism                 false
         :PSB2-path               "/Volumes/Samsung_T5/EvolutionaryComputing/PSB2/"}
        (apply hash-map
               (map #(if (string? %) (read-string %) %)
                    (rest args))))
      ;(apply hash-map
      ;       (if (string/includes? (first args) "PSB2")
      ;         ;; Removes PSB2 path as an arg so that it isn't included in parameter changes
      ;         (map #(if (string? %) (read-string %) %)
      ;              (rest (remove #(= % (second args)) args)))
      ;         ;; Regular parameter changes
      ;         (map #(if (string? %) (read-string %) %)
      ;              (rest args)))))
      [:error-function]
      identity)))

;;keyword path as a separate argument lol