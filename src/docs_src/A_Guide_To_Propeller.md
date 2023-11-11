# A Guide to Propeller

**Propeller**  is an implementation of the Push programming language and the PushGP genetic programming system in Clojure.

For more information on Push and PushGP see http://pushlanguage.org.

## Overview

**Propeller** is a Push-based genetic programming system in Clojure.

<!-- TOC -->
* [A Guide to Propeller](#a-guide-to-propeller)
  * [Overview](#overview)
    * [What can you do with Propeller?](#what-can-you-do-with-propeller)
  * [Installation](#installation)
  * [How do I run Propeller on a problem?](#how-do-i-run-propeller-on-a-problem)
    * [An Example](#an-example)
    * [Can you use a REPL?](#can-you-use-a-repl)
  * [Tutorials](#tutorials)
  * [Contributing](#contributing)
  * [License](#license)
  * [Citation](#citation)
  * [Contact](#contact)
<!-- TOC -->

### What can you do with Propeller?

You can evolve a Push program to solve a problem. 
You can also use the Push interpreter to evaluate Push programs in other projects, 
for example in agent-based evolutionary simulations in which 
agents are controlled by evolving Push programs.

## Installation

If you have installed [leiningen](https://leiningen.org), which is a tool
for running Clojure programs, then you can run Propeller on a genetic
programming problem that is defined within this project from the command
line with the command `lein run -m <namespace>`, replacing `<namespace>`
with the actual namespace that you will find at the top of the problem file.

If you have installed [Clojure](https://clojure.org/guides/install_clojure#java), you can run Propeller on a genetic programming 
problem with the command `clj -M -m <namespace>`, replacing `<namespace>` with 
the actual namespace that you will find at the top of the problem file. 
The examples below use leiningen, but you can replace `lein run -m` with `clj -M -m` to run the same problem.

A specific example is provided later below.

## How do I run Propeller on a problem?

To run Propeller on a problem, you want to call the `-main` function in the problem file using leiningen. 
The `-main` function will create a map of arguments from the input and run the main genetic programming loop.

Below is the general format to run a problem through the command-line:

```
lein run -m [namespace of the problem file you want to test]
```

Additional command-line arguments may
be provided to override the default key/value pairs specified in the
problem file,

```
lein run -m [namespace of the problem file you want to test] [key and value] [key and value]...
```

The possible keys come from the table below:

| Key                        | Description                                                                                                                                                                                                                                                                 |
|----------------------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `:instructions`            | List of possible Push instructions used to create a plushy                                                                                                                                                                                                                  |
| `:error-function`          | The error function used to evaluate individuals, specified in the given problem's namespace                                                                                                                                                                                 |
| `:training-data`           | Map of inputs and desired outputs used to evaluate individuals of the form: {:input1 first-input :input2 second-input ... :output1 first-output ...}                                                                                                                        |
| `:testing-data`            | Map of inputs and desired outputs not in the training-data to test generalizability of a program that fits the `training-data`. The map is of the form: {:input1 first-input :input2 second-input ... :output1 first-output ...}                                            |
| `:max-generations`         | Maximum number of generations                                                                                                                                                                                                                                               |
| `:population-size`         | Size of population in a generation                                                                                                                                                                                                                                          |
| `:max-initial-plushy-size` | Maximum number of Push instructions in the initial plushy                                                                                                                                                                                                                   |
| `:step-limit`              | The maximum number of steps that a Push program will be executed by `interpret-program`                                                                                                                                                                                     |
| `:parent-selection`        | Function from `propeller.selection` that determines method of parent selection method. Propeller includes `:tournament-selection`, `:lexicase-selection`, and `:epsilon-lexicase-selection`.                                                                                |
| `:tournament-size`         | If using a tournament selection method, the number of individuals in each tournaments used to determine parents                                                                                                                                                             |
| `:umad-rate`               | Rate (decimal between 0 and 1) of uniform mutation by addition and deletion (UMAD) genetic operator                                                                                                                                                                         |
| `:variation`               | Map with genetic operators as keys and probabilities as values. For example, {:umad 0.3 :crossover 0.7}. This would mean that when the system needs to generate a child, it will use UMAD 30% of the time and crossover 70% of the time. The probabilities should sum to 1. |
| `:elitism`                 | When true, will cause the individual with the lowest error in the population to survive, without variation, into the next generation.                                                                                                                                       |

When you run a problem, you will get a report each generation with the following information:

```
 :generation            
 :best-plushy 
 :best-program          
 :best-total-error      
 :best-errors        
 :best-behaviors        
 :genotypic-diversity   
 :behavioral-diversity  
 :average-genome-length 
 :average-total-error 

```

### An Example

For example, you can run the simple-regression genetic programming problem with:

```
lein run -m propeller.problems.simple-regression
```

This will run simple-regression with the default set of arguments in the `simple-regression` problem file.

```
{:instructions             instructions
:error-function           error-function
:training-data            (:train train-and-test-data)
:testing-data             (:test train-and-test-data)
:max-generations          500
:population-size          500
:max-initial-plushy-size  100
:step-limit               200
:parent-selection         :lexicase
:tournament-size          5
:umad-rate                0.1
:variation                {:umad 0.5 :crossover 0.5}
:elitism                  false}
```

You can override the default key/value pairs with additional arguments. For example:

```
lein run -m propeller.problems.simple-regression :population-size 100
```

On Unix operating systems, including MacOS, you can use something
like the following to send output both to the terminal
and to a text file (called `outfile` in this example):

```
lein run -m propeller.problems.simple-regression | tee outfile
```

If you want to provide command line arguments that include
characters that may be interpreted by your command line shell
before they get to Clojure, then enclose those in double
quotes, like in this example that provides a non-default
value for the `:variation` argument, which is a clojure map
containing curly brackets that may confuse your shell:

```
lein run -m propeller.problems.simple-regression :variation "{:umad 1.0}"
```

### Can you use a REPL?

Yes!

To run a genetic programming problem from a REPL, start
your REPL for the project (e.g. with `lein repl` at the
command line when in the project directory, or through your
IDE) and then do something like the following (which in
this case runs the simple-regression problem with
`:population-size` 100):

```
(require 'propeller.problems.simple-regression)
(in-ns 'propeller.problems.simple-regression)
(-main :population-size 100 :variation {:umad 1.0})
```

If you want to run the problem with the default parameters,
then you should call `-main` without arguments, as `(-main).

## Tutorials

- [Adding genetic operators](Adding_Genetic_Operators.md)
- [Adding selection methods](Adding_Selection_Method.md)
- [Adding a new problem](Adding_Problem.md)
- [Generating Documentation](Generating_Documentation.md)

## Contributing

You can report a bug on the [GitHub issues page](https://github.com/lspector/propeller/issues).

The best way to contribute to Propeller is to fork the [main GitHub repository](https://github.com/lspector/propeller) and submit a pull request.

Propeller provides a way to automatically [generate documentation](Generating_Documentation.md) for any contributions
you might make.

## License

**Eclipse Public License 2.0**

This commercially-friendly copyleft license provides the ability to commercially license binaries; 
a modern royalty-free patent license grant; and the ability for linked works to use other licenses, including commercial ones.

## Citation

We are in the process of creating a DOI, but in the meantime, 
we ask that you cite the [link to the repository](https://github.com/lspector/propeller) if you use Propeller.

## Contact

To discuss Propeller, Push, and PushGP, you can join the [Push-Language Discourse](https://discourse.pushlanguage.org/).