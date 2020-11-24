(ns propeller.push.utils.globals)

;; =============================================================================
;; Values used by the Push instructions to keep the stack sizes within
;; reasonable limits.
;; =============================================================================

;; Limits the number of items that can be duplicated onto a stack at once.
;; We might want to extend this to limit all the different that things may be
;; placed on a stack.
(def max-stack-items 100)