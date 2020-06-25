(ns propeller.variation
  (:require [propeller.selection :as selection]))

(defn crossover
  "Crosses over two individuals using uniform crossover. Pads shorter one."
  [plushy-a plushy-b]
  (let [shorter (min-key count plushy-a plushy-b)
        longer (if (= shorter plushy-a)
                 plushy-b
                 plushy-a)
        length-diff (- (count longer) (count shorter))
        shorter-padded (concat shorter (repeat length-diff :crossover-padding))]
    (remove #(= % :crossover-padding)
            (map #(if (< (rand) 0.5) %1 %2)
                 shorter-padded
                 longer))))

(defn uniform-addition
  "Returns plushy with new instructions possibly added before or after each
  existing instruction."
  [plushy instructions umad-rate]
  (apply concat
         (map #(if (< (rand) umad-rate)
                 (shuffle [% (rand-nth instructions)])
                 [%])
              plushy)))

(defn uniform-deletion
  "Randomly deletes instructions from plushy at some rate."
  [plushy umad-rate]
  (remove (fn [_] (< (rand)
                     (/ 1 (+ 1 (/ 1 umad-rate)))))
          plushy))

(defn new-individual
  "Returns a new individual produced by selection and variation of
  individuals in the population."
  [pop argmap]
  {:plushy
   (let [prob (rand)]
     (cond
       (< prob (:crossover (:variation argmap)))
       (crossover (:plushy (selection/select-parent pop argmap))
                  (:plushy (selection/select-parent pop argmap)))
       (< prob (+ (:crossover (:variation argmap))
                  (:umad (:variation argmap)) 2))
       (uniform-deletion (uniform-addition (:plushy (selection/select-parent pop argmap))
                                           (:instructions argmap)
                                           (:umad-rate argmap))
                         (:umad-rate argmap))
       :else (:plushy (selection/select-parent pop argmap))))})
