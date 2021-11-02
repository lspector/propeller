(ns propeller.push.state
  (:require [propeller.push.limits :as l]
            #?(:cljs [goog.string :as gstring])))

;; Empty push state - all available stacks are empty
(defonce empty-state {:boolean        '()
                      :char           '()
                      :code           '()
                      :exec           '()
                      :float          '()
                      :input          {}
                      :output         {}
                      :integer        '()
                      :print          '("")
                      :string         '()
                      :vector_boolean '()
                      :vector_float   '()
                      :vector_integer '()
                      :vector_string  '()})

;; All stack types available in a Push state
(defonce stacks (set (keys empty-state)))

;; All vector stack types available in a Push state, with their corresponding
;; element types
(defonce vec-stacks {:vector_boolean :boolean
                     :vector_float   :float
                     :vector_integer :integer
                     :vector_string  :string})

(defonce stack-limiter {:exec           l/limit-code
                        :code           l/limit-code
                        :integer        #(long (l/limit-number %))
                        :float          l/limit-number
                        :string         l/limit-string
                        :vector_boolean l/limit-string
                        :vector_float   #(mapv l/limit-number (l/limit-vector %))
                        :vector_integer #(mapv (fn [i] (int (l/limit-number i))) (l/limit-vector %))
                        :vector_string  #(mapv (fn [s] (l/limit-string s)) (l/limit-vector %))})

(def example-state {:exec    '()
                    :integer '(1 2 3 4 5 6 7)
                    :string  '("abc")
                    :input   {:in1 4}})

;; Returns true if the stack is empty
(defn empty-stack?
  [state stack]
  (empty? (get state stack)))

;; Returns the stack size
(defn stack-size
  [state stack]
  (count (get state stack)))

;; Returns the top item on the stack
(defn peek-stack
  [state stack]
  (if (empty? (get state stack))
    :no-stack-item
    (first (get state stack))))

;; Returns the top n items on the stack, as a chunk. If there are less than n
;; items on the stack, returns the entire stack
(defn peek-stack-many
  [state stack n]
  (take n (get state stack)))

;; Removes the top item of stack
(defn pop-stack
  [state stack]
  (update state stack rest))

;; Pops the top n items of the stack. If there are less than n items on the
;; stack, pops the entire stack
(defn pop-stack-many
  [state stack n]
  (update state stack #(drop n %)))

;; Pushes an item onto the stack
(defn push-to-stack
  [state stack item]
  (if (or (nil? item)
          (>= (stack-size state stack) l/max-stack-items))
    state
    (let [limiter (get stack-limiter stack identity)]
      (update state stack conj (limiter item)))))

;; Pushes a collection of items onto the stack, as a chunk (i.e. leaving them in
;; the order they are in)
(defn push-to-stack-many
  [state stack items]
  (let [items (if (coll? items) items (list items))
        items-no-nil (filter #(not (nil? %)) items)
        items-to-push (take (- l/max-stack-items (stack-size state stack)) items-no-nil)
        limit (get stack-limiter stack identity)]
    (update state stack into (map limit (reverse items-to-push)))))

;; Takes a state and a collection of stacks to take args from. If there are
;; enough args on each of the desired stacks, returns a map with keys
;; {:state :args}, where :state is the new state and :args is a list of args
;; popped from the stacks. If there aren't enough args on the stacks, returns
;; :not-enough-args without popping anything
(defn get-args-from-stacks
  [state stacks]
  (loop [state state
         stacks (reverse stacks)
         args '()]
    (if (empty? stacks)
      {:state state :args args}
      (let [current-stack (first stacks)]
        (if (empty-stack? state current-stack)
          :not-enough-args
          (recur (pop-stack state current-stack)
                 (rest stacks)
                 (conj args (peek-stack state current-stack))))))))


;; Pretty-print a Push state, for logging or debugging purposes
(defn print-state
  [state]
  (doseq [stack (keys empty-state)]
    #?(:clj  (printf "%-15s = " stack)
       :cljs (print (gstring/format "%-15s = " stack)))
    (prn (if (get state stack) (get state stack) '()))
    (flush)))
