(ns propeller.push.core)

;; PushGP instructions are represented as keywords, and stored in an atom. They
;; can be either constant literals or functions that take and return a Push state
(def instruction-table (atom (hash-map)))

;; Number of blocks opened by instructions (default = 0)
(def opens {:exec_dup 1
            :exec_if  2})
