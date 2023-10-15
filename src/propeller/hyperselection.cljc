(ns propeller.hyperselection)

(defn sum-list-map-indices
  "sums a list of maps that have the :index property's index multiplicity"
  [list-of-maps]
  (->> list-of-maps
       (map #(:index %))
       frequencies))

(defn ordered-freqs 
  "takes a map from indices to frequencies, and returns a sorted list of the frequences is descencing order"
  [freqs]
  (->> freqs
       vals
       (sort >)))

(defn normalize-list-by-popsize [popsize lst]
  (map #(double (/ % popsize)) lst))

(defn hyperselection-track 
  "outputs a normalized list of the hyperselection proportion for each parent"
  [new-pop]
  (->> new-pop
       sum-list-map-indices
       ordered-freqs
       (normalize-list-by-popsize (count new-pop))))

(defn log-hyperselection-and-ret [new-pop]
  (prn {:hyperselection (hyperselection-track new-pop)})
  new-pop)

(defn reindex-pop 
  "assigns each member of the population a unique index before selection to track hyperselection"
  [pop]
  (map (fn [indiv index] (assoc indiv :index index)) pop (range (count pop))))