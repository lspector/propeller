(ns propeller.core
  (:gen-class)
  (:use [propeller instructions gp problems]))

(defn -main
  "Runs propel-gp, giving it a map of arguments."
  [& args]
  (binding [*ns* (the-ns 'propeller.core)]
    (gp (update-in (merge {:instructions            default-instructions
                           :error-function          regression-error-function
                           :max-generations         500
                           :population-size         200
                           :max-initial-plushy-size 50
                           :step-limit              100
                           :parent-selection        :tournament
                           :tournament-size         5
                           :UMADRate 0.2
                           :variation {:UMAD 0.5 :crossover 0.5}}
                          (apply hash-map
                                 (map read-string args)))
                   [:error-function]
                   #(if (fn? %) % (eval %))))))