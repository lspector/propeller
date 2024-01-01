(ns propeller.genome
  "The genetic material in Propeller. A `plushy` is a list of Push instructions that represent a Push program.
They hold the genetic material for an `individual`. In the initial population, we create random plushys."
  {:doc/format :markdown}
  (:require [propeller.push.instructions.parentheses :as parentheses]
            [propeller.utils :as utils]))

(defn make-random-plushy
  "Creates and returns a new plushy made of random instructions."
  [{:keys [instructions max-initial-plushy-size bmx? bmx-gene-length-limit]
    :as argmap}]
  (let [plushy (repeatedly (rand-int max-initial-plushy-size)
                           #(utils/random-instruction instructions argmap))]
    (if bmx?
      (-> plushy
          (utils/remove-empty-genes)
          (utils/enforce-gene-length-limit bmx-gene-length-limit))
      plushy)))

(defn plushy->push-internal
  [plushy argmap]
  (let [opener? #(and (vector? %) (= (first %) 'open))]    ;; [open <n>] marks opens
    (loop [push ()                                         ;; iteratively build the Push program from the plushy
           plushy (mapcat #(let [n (get parentheses/opens %)]
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
   (plushy->push-internal (if (:bmx? argmap)
                            (filter (complement #{:gap}) plushy)
                            plushy)
                          argmap)))