(ns propeller.main
  (:require [propeller.core :as propeller]))

(defn main! []
  (println "Loading main..."))

(defn ^:dev/after-load reload! []
  (propeller/-main))
