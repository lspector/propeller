Downsampling the Training Data
=

Downsampling is a very simple way to improve the efficiency of your evolutionary runs. It might allow for deeper evolutionary searches and a greater success rate.

Using Downsampled selection with propeller is easy:

Set the :parent-selection argument to whichever selection strategy you would like, and set the :downsample? argument to true as follows:

```clojure
lein run -m propeller.problems.simple-regression :parent-selection :lexicase :downsample? true <required downsampling args here>
```

The number of evaluations is held constant when comparing to a full training set run, so set the :max-generations to a number of generations that you would have gone to using a **full** sample.

## Downsample Functions

In this repository, you have access to 3 different downsampling functions. These are the methods used to take a down-sample from the entire training set.

To use them, add the argument ```:ds-function``` followed by which function you would like to us

The list is
- ```:case-maxmin``` - This is the method used for informed down-sampled lexicase selection
- ```:case-maxmin-auto``` - This method automatically determines the downsample size
- ```:case-rand```- Random Sampling

### Using ```:case-maxmin```:

In order to use regular informed down-sampled selection, you must specify a few things:
- ```:downsample-rate```- This is the $r$ parameter: what proportion of the full sample should be in the down-sample $\in [0,1]$
- ```:ds-parent-rate``` - This is the $\rho$ parameter: what proportion of parents are used to evaluate case distances $\in [0,1]$
- ```:ds-parent-gens``` - This is the $k$ parameter: How many generations in between parent evaluations for distances $\in \{1,2,3, \dots\}$

### Using ```:case-maxmin-auto```:

In order to use automatic informed down-sampled selection, you must specify a few things:
- ```:case-delta ```- This is the $\Delta$ parameter: How close can the farthest case be from its closest case before we stop adding to the down-sample
- ```:ids-type``` - Either ```:elite``` or ```:solved ``` - Specifies whether we are using elite/not-elite or solved/not-solved as our binary-fication of case solve vectors.
- ```:ds-parent-rate``` - This is the $\rho$ parameter: what proportion of parents are used to evaluate case distances $\in [0,1]$
- ```:ds-parent-gens``` - This is the $k$ parameter: How many generations in between parent evaluations for distances $\in \{1,2,3, \dots\}$

### Using ```:case-rand```:

In order to use regular randomly down-sampled selection, you must specify a few things:
- ```:downsample-rate```- This is the $r$ parameter: what proportion of the full sample should be in the down-sample $\in [0,1]$





Here's an example of running informed downsampled lexicase selection with $r=0.1$, $\rho=0.01$ and $k=100$ on the simple classification problem:

```clojure
lein run -m propeller.problems.simple-classification :parent-selection :lexicase :downsample? true :ds-function :case-maxmin :downsample-rate 0.1 :max-generations 300 :ds-parent-rate 0.01 :ds-parent-gens 100
```

