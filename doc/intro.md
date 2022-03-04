# Introduction to Propeller

TODO: write [great documentation](http://jacobian.org/writing/what-to-write/)




# Simplification

To use Propeller's auto-simplification system, simply include the following four command line arguments when running a problem:

```clojure
:simplification? true
```
Toggle auto-simplification
```clojure
:simplification-k 4
``` 
This is the upper bound for elements deleted from the plushy every step. Every step, a number in $[1, k]$ of elements is deleted from the plushy representation of the solution.
```clojure
:simplification-steps 1000 
```
Number of simplification steps to perform
```clojure
:simplification-verbose? true 
```
whether or not to output simplification info into the output of the evolutionary run.
The output with verbose adds the following lines to the output:
```clojure
{:start-plushy-length 42, :k 4}
{:final-plushy-length 13, :final-plushy (:in1 :in1 :integer_quot :in1 :in1 :exec_dup :in1 :integer_mult close :exec_dup :integer_add 1 :integer_add)}
```