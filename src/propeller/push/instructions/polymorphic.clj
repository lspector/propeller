(ns propeller.push.instructions.polymorphic
  (:require [propeller.push.core :refer [def-instruction]]
            [propeller.push.state :refer [empty-stack?
                                          peek-stack
                                          pop-stack
                                          push-to-stack]]
            [propeller.push.utils :refer [generate-functions
                                          make-instruction]]))

;; =============================================================================
;; Polymorphic Instructions
;;
;; (for all types, with the exception of non-data  stacks like auxiliary, tag,
;; input, and output)
;; =============================================================================

;; Pushes TRUE onto the BOOLEAN stack if the top two items are equal.
;; Otherwise FALSE
(defn- _=
  [stack state]
  (make-instruction state = [stack stack] :boolean))

;; Duplicates the top item of the stack. Does not pop its argument (since that
;; would negate the effect of the duplication)
(defn- _dup
  [stack state]
  (let [top-item (peek-stack state stack)]
    (if (empty-stack? state stack)
      state
      (push-to-stack state stack top-item))))

;; Duplicates n copies of the top item (i.e leaves n copies there). Does not pop
;; its argument (since that would negate the effect of the duplication). The
;; number n is determined by the top INTEGER. For n = 0, equivalent to POP.
;; For n = 1, equivalent to NOOP. For n = 2, equivalent to DUP. Negative values
;; of n are treated as 0.
(defn- _duptimes
  [stack state]
  (if (or (and (= stack :integer)
               (>= (count (:integer state)) 2))
          (and (not= stack :integer)
               (not (empty-stack? state :integer))
               (not (empty-stack? state stack))))
    (let [n (peek-stack state :integer)
          item-to-duplicate (peek-stack state stack)]
      nil)
    state))


(defn- _dupitems
  [stack state]
  ())

;; Pushes TRUE onto the BOOLEAN stack if the stack is empty. Otherwise FALSE
(defn- _empty
  [stack state]
  (push-to-stack state :boolean (empty-stack? state stack)))

;; Empties the given stack
(defn- _flush
  [stack state]
  ())

;; Pops the given stack
(defn- _pop
  [stack state]
  (pop-stack state stack))

;; Rotates the top three items on the stack (i.e. pulls the third item out and
;; pushes it on top). Equivalent to (yank state stack-type 2)
(defn- _rot
  [stack state]
  ())

;; Inserts the top item deeper into the stack, using the top INTEGER to
;; determine how deep
(defn- _shove
  [stack state]
  ())

;; Pushes the given stack's depth onto the INTEGER stack
(defn- _stackdepth
  [stack state]
  ())

;; Swaps the top two items on the stack
(defn- _swap
  [stack state]
  ())

;; Removes an indexed item from deep in the stack. The top INTEGER is used to
;; determine how deep to yank from
(defn- _yank
  [stack state]
  ())

;; Pushes a copy of an indexed item deep in the stack, without removing it.
;; The top INTEGER is used to determine how deep to yankdup from
(defn- _yankdup
  [stack state]
  ())

;; 5 types x 1 function = 5 instructions
(generate-functions [:boolean :char :float :integer :string] [_=])

;; 8 types x 12 function = 96 instructions
(generate-functions
  [:boolean :char :code :exec :float :integer :string :zip]
  [_dup _duptimes _dupitems _empty _flush _pop _rot _shove _stackdepth
   _swap _yank _yankdup])
