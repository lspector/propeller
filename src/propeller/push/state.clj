(ns propeller.push.state)

;; Set of all stacks used by the Push interpreter
(defonce stacks #{:auxiliary
                  :boolean
                  :char
                  :code
                  :environment
                  :exec
                  :float
                  :genome
                  :gtm
                  :input
                  :integer
                  :output
                  :return
                  :string
                  :tag
                  :vector_boolean
                  :vector_float
                  :vector_integer
                  :vector_string
                  :zip})

;; Record-based states for performance
(defmacro define-push-state []
  `(defrecord ~'State [~@(map #(symbol (name %)) stacks)]))

(define-push-state)

;; Empty push state - each stack type is nil
(defonce empty-state (map->State {}))

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
  "Returns top item on a stack."
  [state stack]
  (let [working-stack (get state stack)]
    (if (empty? working-stack)
      :no-stack-item
      (first working-stack))))

(defn pop-stack
  "Removes top item of stack."
  [state stack]
  (update state stack rest))

(defn push-to-stack
  "Pushes item(s) onto stack."
  [state stack items]
  (let [items-list (if (coll? items) items (list items))
        items-list-no-nil (filter #(not (nil? %)) items-list)]
    (update state stack into items-list-no-nil)))

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
