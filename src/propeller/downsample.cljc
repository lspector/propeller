(ns propeller.downsample
  (:require [propeller.tools.math :as math]
            [propeller.tools.metrics :as metrics]
            [propeller.utils :as utils]))

(defn assign-indices-to-data
  "assigns an index to each training case in order to differentiate them when downsampling"
  [training-data]
  (map (fn [data-map index]
         (let [data-m (if (map? data-map) data-map (assoc {} :data data-map))] ;if data is not in a map, make it one
           (assoc data-m :index index)))
       training-data (range (count training-data))))

(defn initialize-case-distances
  [{:keys [training-data population-size]}]
  (map #(assoc % :distances (vec (repeat (count training-data) population-size))) training-data))

(defn select-downsample-random
  "Selects a downsample from the training cases and returns it"
  [training-data {:keys [downsample-rate]}]
  (take (int (* downsample-rate (count training-data))) (shuffle training-data)))

(defn select-downsample-maxmin
  "selects a downsample that has it's cases maximally far away by sequentially 
   adding cases to the downsample that have their closest case maximally far away"
  [training-data {:keys [downsample-rate]}]
  (let [shuffled-cases (shuffle training-data)
        goal-size (int (* downsample-rate (count training-data)))]
    (loop [new-downsample (conj [] (first shuffled-cases))
           cases-to-pick-from (rest shuffled-cases)]
      (if (>= (count new-downsample) goal-size)
        new-downsample
        (let [tournament cases-to-pick-from
              min-case-distances (metrics/min-of-colls
                              (map (fn [distance-list]
                                     (utils/filter-by-index distance-list (map #(:index %) tournament)))
                                   (map #(:distances %) new-downsample)))
              selected-case-index (metrics/argmax min-case-distances)]
          (recur (conj new-downsample (nth tournament selected-case-index))
                 (shuffle (utils/drop-nth selected-case-index tournament))))))))

(defn select-downsample-maxmin-adaptive
  "selects a downsample that has it's cases maximally far away by sequentially 
   adding cases to the downsample that have their closest case maximally far away
   automatically stops when the maximum minimum distance is below delta"
[training-data {:keys [case-delta]}]
(let [shuffled-cases (shuffle training-data)]
  (loop [new-downsample (conj [] (first shuffled-cases))
         cases-to-pick-from (rest shuffled-cases)] 
      (let [tournament cases-to-pick-from
            min-case-distances (metrics/min-of-colls
                                (map (fn [distance-list]
                                       (utils/filter-by-index distance-list (map #(:index %) tournament)))
                                     (map #(:distances %) new-downsample)))
            selected-case-index (metrics/argmax min-case-distances)]
        (if (or (= 0 (count tournament)) (<= (apply max min-case-distances) case-delta))
          new-downsample
          (recur (conj new-downsample (nth tournament selected-case-index))
                 (shuffle (utils/drop-nth selected-case-index tournament))))))))

(defn get-distance-between-cases
  "returns the distance between two cases given a list of individual error vectors, and the index these
   cases exist in the error vector. Only makes the distinction between zero and nonzero errors"
  [error-lists case-index-1 case-index-2]
  (if (or (< (count (first error-lists)) case-index-1)
          (< (count (first error-lists)) case-index-2)
          (neg? case-index-1) (neg? case-index-2))
    (count error-lists) ;return the max distance 
    (let [errors-1 (map #(nth % case-index-1) error-lists)
          errors-2 (map #(nth % case-index-2) error-lists)]
    ;compute distance between errors-1 and errors-2
      (reduce + (map (fn [e1 e2] (math/abs (- (math/step e1) (math/step e2)))) errors-1 errors-2)))))

(defn update-at-indices
  "merges two vectors at the indices provided by a third vector"
  [big-vec small-vec indices]
  (->> big-vec
       (map-indexed (fn [idx itm] (let [index (.indexOf indices idx)]
                                    (if (not= -1 index) (nth small-vec index) itm))))
       vec))

(defn merge-map-lists-at-index
  "merges two lists of maps, replacing the maps in the big 
   list with their corresponding (based on index) maps in the small list"
  [big-list small-list]
  (map
   #(let [corresponding-small (some (fn [c] (when (= (:index %) (:index c)) c)) small-list)]
      (if (nil? corresponding-small) % corresponding-small))
   big-list))

(defn replace-mins-with-zero
  "replaces the minimum value(s) in a list with zero"
  [coll]
  (if (empty? coll)
    '()
    (let [m (apply min coll)]
      (map #(if (= m %) 0 %) coll))))

(defn replace-close-zero-with-zero
  "replaces values within a delta of zero with zero, used for regression problems"
  [coll delta]
  (map #(if (>= delta %) 0 %) coll))

(defn convert-to-elite-error
  "converts a set of errors into a list where all the elite errors are replaced with 0s so that we can use
   it in the selection of down-samples with elite/not-elite selection"
  [errors]
  (map #(replace-mins-with-zero %) errors))

(defn convert-to-soft-error
  [errors delta]
  (map #(replace-close-zero-with-zero % delta) errors))

(defn update-case-distances
  "updates the case distance field of training-data list, should be called after evaluation of individuals
   evaluated-pop should be a list of individuals that all have the :errors field with a list of this 
   individuals performance on the each case in the training-data, in order. ids-type is :elite to use elite/not-elite, :soft to consider near solves, and :solved to use solve/not-solved"
  ([evaluated-pop ds-data training-data ids-type]
   (update-case-distances evaluated-pop ds-data training-data ids-type 0)) ; default solution threshold is 0, only used if ids-type is :soft
  ([evaluated-pop ds-data training-data ids-type solution-threshold]
  (let [ds-indices (map #(:index %) ds-data) 
        errors (map #(:errors %) evaluated-pop)
        corr-errors (case ids-type
                      :elite (convert-to-elite-error errors)
                      :soft (convert-to-soft-error errors solution-threshold)
                      errors)] ;errors, including elite/not-elite distinction
    (merge-map-lists-at-index 
     training-data (map-indexed
                    (fn [idx d-case] (update-in d-case
                             [:distances] #(update-at-indices
                                            % (map (fn [other] (get-distance-between-cases corr-errors idx other))
                                                  (range (count ds-indices))) ds-indices))) ds-data)))))