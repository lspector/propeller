(ns propeller.push.interpreter
  (:require [propeller.push.core :as push]
            [propeller.push.state :as state]))

(defn interpret-one-step
  "Takes a Push state and executes the next instruction on the exec stack."
  [state]
  (let [popped-state (state/pop-stack state :exec)
        first-instruction-raw (first (:exec state))
        first-instruction (if (keyword? first-instruction-raw)
                            (first-instruction-raw @push/instruction-table)
                            first-instruction-raw)]
    (cond
      (fn? first-instruction)
      (first-instruction popped-state)
      ;
      (integer? first-instruction)
      (state/push-to-stack popped-state :integer first-instruction)
      ;
      (string? first-instruction)
      (state/push-to-stack popped-state :string first-instruction)
      ;
      (seq? first-instruction)
      (update popped-state :exec #(concat %2 %1) first-instruction)
      ;
      (or (= first-instruction true) (= first-instruction false))
      (state/push-to-stack popped-state :boolean first-instruction)
      ;
      :else
      (throw (Exception. (str "Unrecognized Push instruction in program: "
                              (name first-instruction-raw)))))))

(defn interpret-program
  "Runs the given problem starting with the stacks in start-state."
  [program start-state step-limit]
  (loop [state (assoc start-state :exec program :step 1)]
    (if (or (empty? (:exec state))
            (> (:step state) step-limit))
      state
      (recur (update (interpret-one-step state) :step inc)))))
