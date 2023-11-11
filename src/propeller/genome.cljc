(ns propeller.genome
  "The genetic material in Propeller. A `plushy` is a list of Push instructions that represent a Push program.
They hold the genetic material for an `individual`. In the initial population, we create random plushys."
  {:doc/format :markdown}
  (:require [propeller.push.instructions :as instructions]
            [propeller.utils :as utils]))

(defn make-random-plushy
  "Creates and returns a new plushy made of random instructions and of a maximum size of max-initial-plushy-size."
  [instructions max-initial-plushy-size]
  (repeatedly
   (rand-int max-initial-plushy-size)
   #(utils/random-instruction instructions)))

(defn plushy->push-internal
  [plushy argmap]
  (let [opener? #(and (vector? %) (= (first %) 'open))]    ;; [open <n>] marks opens
    (loop [push ()                                         ;; iteratively build the Push program from the plushy
           plushy (mapcat #(let [n (get instructions/opens %)]
                             (if (and n
                                      (> n 0))
                               [% ['open n]]
                               [%]))
                          plushy)]
      (if (empty? plushy)                                  ;; maybe we're done?
        (if (some opener? push)                            ;; done with plushy, but unclosed open
          (recur push '(close))                            ;; recur with one more close
          push)                                            ;; otherwise, really done, return push
        (let [i (first plushy)]
          (if (= i 'close)
            (if (some opener? push)                        ;; process a close when there's an open
              (recur (let [post-open (reverse (take-while (comp not opener?)
                                                          (reverse push)))
                           open-index (- (count push) (count post-open) 1)
                           num-open (second (nth push open-index))
                           pre-open (take open-index push)]
                       (if (= 1 num-open)
                         (concat pre-open [post-open])
                         (concat pre-open [post-open ['open (dec num-open)]])))
                     (rest plushy))
              (recur push (rest plushy)))                  ;; unmatched close, ignore
            (recur (concat push [i]) (rest plushy))))))))  ;; anything else

(defn plushy->push
  "Returns the Push program expressed by the given plushy representation."
  ;; use an empty argmap if none provided
  ([plushy]
   (plushy->push plushy {}))
  ;; call plushy->push-internal with possibly-preprocessed plushy
  ([plushy argmap]
   (plushy->push-internal (if (or (> (or (:ah-umad (:variation argmap)) 0) 0) ;; must strip :vary and :protect
                                  (> (or (:autoconstructive-crossover (:variation argmap)) 0) 0)) ;; must strip :gene
                            (filter (complement #{:vary :protect :gene}) plushy)
                            plushy)
                          argmap)))