# Downsampling the Training Data

Downsampling is a very simple way to improve the efficiency of your evolutionary runs. It might allow for deeper evolutionary searches and a greater success rate.

Using Downsampled-Lexicase selection with propeller is easy:

Set the :parent-selection argument to :ds-lexicase as follows
```clojure
lein run -m propeller.problems.simple-regression :parent-selection :ds-lexicase <ARGS>
```

Arguments:


- Case Downsampling function:
    - Random sampling (default)
    - Case tournament selection
         ```clojure 
        :ds-function :case-tournament 
        ```
    - Case Lexicase Selection
        WIP
- Downsample Rate:
    ```clojure 
        :downsample-rate 0.1
    ```    

