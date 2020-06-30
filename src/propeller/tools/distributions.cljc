(ns propeller.tools.distributions
  (:require [propeller.tools.calculus :as calculus]
            [propeller.tools.math :as math]))

;; =============================================================================
;; NORMAL
;; =============================================================================

(defn- box-muller
  "Given two uniformly distributed random variables (from 0 to 1), returns a
  Standard Normal variable computed using the Box-Muller Transform."
  [u1 u2]
  (* (math/sqrt (* -2 (math/log u1)))
     (math/cos (* 2 math/PI u2))))

(defn- normal-pdf
  "Given a mean and standard deviation, returns the corresponding Normal
  Probability Distribution Function."
  [mu sigma]
  (fn [x]
    (* (/ 1 (* sigma (math/sqrt (* 2 math/PI))))
       (math/exp (- (/ (math/pow (/ (- x mu) sigma) 2) 2))))))

(defn rand-norm
  "Generates n Normally-distributed random variables with given mean and
  standard deviation. If no parameters are provided, defaults to a
  single random observation from a Standard Normal distribution.
  Accepts an argument map with optional keys :n, :mu, and :sigma."
  [{:keys [n mu sigma]
    :or   {n 1, mu 0, sigma 1}}]
  (repeatedly n #(box-muller (rand) (rand))))

(defn pdf-norm
  "Returns the value of the Normal Probability Distribution Function at a
  particular value x. If no distributional parameters are provided, defaults to
  the Standard Normal PDF.
  Accepts an argument map with keys :x, and optionally :mu and :sigma."
  [{:keys [x mu sigma]
    :or   {mu 0, sigma 1}}]
  ((normal-pdf mu sigma) x))

(defn cdf-norm
  "Parameters: {:keys [x mu sigma]}
  Returns the value of the Normal Cumulative Distribution Function at a
  particular value x. If no distributional parameters are provided, defaults to
  the Standard Normal CDF.
  Accepts an argument map with keys :x, and optionally :mu and :sigma."
  [{:keys [x mu sigma]
    :or   {mu 0, sigma 1}}]
  (calculus/integrate (normal-pdf mu sigma) (- mu (* 6 sigma)) x))

(defn quant-norm
  "For a given probability p, returns the corresponding value of the quantile
  function (i.e. the inverse Cumulative Distribution Function). If no
  distributional parameters are provided, defaults to Standard Normal quantiles.
  Accepts an argument map with keys :p, and optionally :mu and :sigma."
  [{:keys [p mu sigma]
    :or   {mu 0, sigma 1}}]
  ())                                                       ; unfinished...
