(ns propeller.core
  #?(:clj (:gen-class)))

(defn -main
  "Not intended to be run; just print a message."
  [& _]
  ;; Exception for when no args were passed
  (println "To run a genetic programming problem, provide a the problem's")
  (println "namespace as specified in the Propeller README file at")
  (println "https://github.com/lspector/propeller/blob/master/README.md"))