# Adding Genetic Operators

In addition to the already-included genetic operators, you can add your own!

## Variation Genetic Operators

1. Go to `propeller.variation.cljc`
2. Define a genetic operator function
3. In `propeller.variation/new-individual`, add the new genetic operator in the `new-individual` function under the `case` call

``` clojure
(defn new-individual
  "Returns a new individual produced by selection and variation of
  individuals in the population."
  [pop argmap]
  ...
     (case op
       ...
       
       :new-genetic-operator
       (-> (:plushy (selection/select-parent pop argmap))
           (new-genetic-operator ))
       ...
       :else
       (throw #?(:clj  (Exception. (str "No match in new-individual for " op))
                 :cljs (js/Error
                         (str "No match in new-individual for " op))))))})
```

4. When running a problem, specify the genetic operator in `:variation`.
For example:
```
lein run -m propeller.problems.simple-regression :variation "{:new-genetic-operator 1.0}"

```

## Selection Genetic Operators

1. Go to `propeller.selection.cljc`
2. Define a genetic operator function
3. In `propeller.selection.cljc`, add the new genetic operator in the `select-parent` function under the `case` call.

```clojure
(defn select-parent
  "Selects a parent from the population using the specified method."
  [pop argmap]
  (case (:parent-selection argmap)
    ...
    :new-genetic-operator (:new-genetic-operator )
    ...
    ))
```

4. When running a problem, specify the selection method in `:parent-selection`

For example:

```
lein run -m propeller.problems.simple-regression :parent-selection :new-genetic-operator

```

