(ns propeller.downsample)

(defn assign-indices-to-data
  "assigns an index to each training case in order to differentiate them when downsampling"
  [{:keys [training-data]}]
  (map (fn [data-map index]
         (let [data-m (if (map? data-map) data-map (assoc {} :data data-map))] ;if data is not in a map, make it one
           (assoc data-m :index index)))
       training-data (range (count training-data))))

(defn select-downsample-random
  "Selects a downsample from the training cases and returns it"
  [training-data {:keys [downsample-rate]}]
  (take (int (* downsample-rate (count training-data))) (shuffle training-data)))

(defn update-case-data
  "updates the case metadata field of argmap, should be called after evaluation of individuals"
  [argmap])