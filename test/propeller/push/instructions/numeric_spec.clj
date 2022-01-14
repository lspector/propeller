(ns propeller.push.instructions.numeric-spec
  (:require
   ; [clojure.numer :as string]
    [clojure.test.check.generators :as gen]
    [clojure.test.check.properties :as prop]
    [clojure.test.check.clojure-test :as ct :refer [defspec]]
    [propeller.push.state :as state]
    [propeller.push.instructions :as instructions]
    [propeller.push.instructions.bool :as boolean-instructions]
    [propeller.push.interpreter :as interpreter]))
