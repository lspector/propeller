(ns propeller.push.state)

;; Empty push state - all available stacks are empty
(defonce empty-state {:auxiliary      '()
                      :boolean        '()
                      :char           '()
                      :code           '()
                      :exec           '()
                      :float          '()
                      :input          {}
                      :integer        '()
                      :output         '()
                      :string         '()
                      :vector_boolean '()
                      :vector_float   '()
                      :vector_integer '()
                      :vector_string  '()})

(def example-push-state
  {:exec    '()
   :integer '(1 2 3 4 5 6 7)
   :string  '("abc")
   :input   {:in1 4}})

(defn empty-stack?
  "Returns true if the stack is empty."
  [state stack]
  (empty? (get state stack)))

(defn peek-stack
  "Returns the top item on a stack."
  [state stack]
  (let [working-stack (get state stack)]
    (if (empty? working-stack)
      :no-stack-item
      (first working-stack))))

(defn peek-stack-multiple
  "Returns the top n items on a stack. If there are less than n items on the
  stack, returns the entire stack."
  [state stack n]
  (take n (get state stack)))

(defn pop-stack
  "Removes the top item of stack."
  [state stack]
  (update state stack rest))

(defn pop-stack-multiple
  "Removes the top n items of a stack. If there are less than n items on the
  stack, pops the entire stack."
  [state stack n]
  (update state stack #(drop n %)))

(defn push-to-stack
  "Pushes an item onto a stack."
  [state stack item]
  (update state stack conj item))

(defn push-to-stack-multiple
  "Pushes a list of items onto a stack, leaving them in the order they are in."
  [state stack items]
  (let [items-list (if (coll? items) items (list items))
        items-list-no-nil (filter #(not (nil? %)) items-list)]
    (update state stack into (reverse items-list-no-nil))))

(defn get-args-from-stacks
  "Takes a state and a collection of stacks to take args from. If there are
  enough args on each of the desired stacks, returns a map with keys
  {:state :args}, where :state is the new state and :args is a list of args
  popped from the stacks. If there aren't enough args on the stacks, returns
  :not-enough-args without popping anything."
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
