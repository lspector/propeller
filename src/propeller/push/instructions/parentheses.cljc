(ns propeller.push.instructions.parentheses)

;; Number of blocks opened by instructions (default = 0)
(def opens
  "Number of blocks opened by instructions. The default is 0."
  {:exec_dup 1
   :exec_dup_times 1
   :exec_dup_items 0 ; explicitly set to 0 to make it clear that this is intended
   :exec_eq 0 ; explicitly set to 0 to make it clear that this is intended
   :exec_pop 1
   :exec_rot 3
   :exec_shove 1
   :exec_swap 2
   :exec_yank 0 ; explicitly set to 0 to make it clear that this is intended
   :exec_yank_dup 0 ; explicitly set to 0 to make it clear that this is intended
   :exec_deep_dup 0 ; explicitly set to 0 to make it clear that this is intended
   :exec_print 1
   :exec_if  2
   :exec_when 1
   :exec_while 1
   :exec_do_while 1
   :exec_do_range 1
   :exec_do_count 1
   :exec_do_times 1
   :exec_k 2
   :exec_s 3
   :exec_y 1
   :string_iterate 1
   :vector_boolean_iterate 1
   :vector_string_iterate 1
   :vector_integer_iterate 1
   :vector_float_iterate 1})
