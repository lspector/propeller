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

(defn plushy->push
  "Returns the Push program expressed by the given plushy representation.

  The function takes in a plushy representation as input and converts it into a Push program by iteratively processing
  the plushy elements and adding instructions to the push program.
  It also handles the case where there are open instructions that need to be closed before the end of the program.
  "
  ([plushy] (plushy->push plushy {}))
  ([plushy argmap]
   (let [plushy (if (:diploid argmap) (map first (partition 2 plushy)) plushy)
         opener? #(and (vector? %) (= (first %) 'open))]    ;; [open <n>] marks opens
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
             (recur (concat push [i]) (rest plushy))))))))) ;; anything else
