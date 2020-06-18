(ns propeller.variation
  (:use [propeller selection]))

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
  "Randomly adds new instructions before every instruction (and at the end of
  the plushy) with some probability."
  [plushy instructions UMADRate]
  (let [rand-code (repeatedly (inc (count plushy))
                              (fn []
                                (if (< (rand) UMADRate)
                                  (rand-nth instructions)
                                  :mutation-padding)))]
    (remove #(= % :mutation-padding)
            (interleave (conj plushy :mutation-padding)
                        rand-code))))

(defn uniform-deletion
  "Randomly deletes instructions from plushy at some rate."
  [plushy UMADRate]
  (remove (fn [x] (< (rand) (/ 1(+ 1 (/ 1 UMADRate)))))
          plushy))

(defn new-individual
  "Returns a new individual produced by selection and variation of
  individuals in the population."
  [pop argmap]
  {:plushy
   (let [prob (rand)]
     (cond
       (< prob (:crossover (:variation argmap))) (crossover (:plushy (select-parent pop argmap))
                                                            (:plushy (select-parent pop argmap)))
       (< prob (+ (:crossover (:variation argmap)) (/ (:UMAD (:variation argmap)) 2))) (uniform-addition (:plushy (select-parent pop argmap))
                                                                                                         (:instructions argmap)
                                                                                                         (:UMADRate argmap))
       :else (uniform-deletion (:plushy (select-parent pop argmap))
                               (:UMADRate argmap))))})