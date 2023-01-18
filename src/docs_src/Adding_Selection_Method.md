# Adding a Selection Method

1. Define a selection method function in `propeller.selection` that selects an individual from the population
2. Add the selection method in `propeller.selection/select-parent` under the `case` call:

   ```clojure
    (defn select-parent
    "Selects a parent from the population using the specified method."
    [pop argmap]
    (case (:parent-selection argmap)
    :new-selection-method (new-selection-method )))
   ```

3. When runnning a problem, specify the selection method in `:parent-selection`. 
For example:
    ```
    lein run -m propeller.problems.simple-regression :parent-selection :new-selection-method"
    ```


