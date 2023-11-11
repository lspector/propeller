# propeller

Yet another Push-based genetic programming system in Clojure.

Full documentation is at [https://lspector.github.io/propeller/](https://lspector.github.io/propeller/).

## Usage

If you are working in a Clojure IDE with an integrated REPL, the first
thing you may want to do is to open `src/propeller/session.cljc` and 
evaluate the namespace declaration and the commented-out expressions 
therein. These demonstrate core components of Propeller including
complete genetic programming runs. When conducting complete genetic
programming runs this way (using `gp/gp`), depending on your IDE you 
may need to explicitly open and load the problem file before evaluating 
the calls to `require` and `gp/gp`.

To run Propeller from the command line, on a genetic programming problem 
that is defined within this project, you will probably want to use either
the Clojure [CLI tools](https://clojure.org/guides/deps_and_cli) or 
[leiningen](https://leiningen.org). In the examples below, the leiningen
and CLI commands are identical except that the former begin with
`lein run -m`, while the latter begin with `clj -M -m`.

To start a run use `clj -M -m <namespace>` or 
`lein run -m <namespace>`, replacing `<namespace>` 
with the actual namespace that you will find at the top of the problem file. 

For example, you can run the simple-regression genetic programming problem with:

```
clj -M -m propeller.problems.simple-regression
```
or 

```
lein run -m propeller.problems.simple-regression
```

Additional command-line arguments may
be provided to override the default key/value pairs specified in the 
problem file, for example:

```
clj -M -m propeller.problems.simple-regression :population-size 100
```

or

```
lein run -m propeller.problems.simple-regression :population-size 100
```

On Unix operating systems, including MacOS, you can use something
like the following to send output both to the terminal
and to a text file (called `outfile` in this example):


```
clj -M -m propeller.problems.simple-regression | tee outfile
```

or

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
clj -M -m propeller.problems.simple-regression :variation "{:umad 1.0}"
```

or

```
lein run -m propeller.problems.simple-regression :variation "{:umad 1.0}"
```

For many genetic operator hyperparameters, collections may be provided in place of single values. When this is done, a random element of the collection will be chosen (with each being equally likely) each time the operator is used. When specied at the command line, these collections will also have to be quoted, for example with `:umad-rate "[0.01 0.05 0.1]"` to mean that UMAD rates of 0.01, 0.05, and 0.1 can be used.

By default, Propeller will conduct many processes concurrently on multiple 
cores using threads. If you  want to disable this behavior (for example, during 
debugging) then provide the argument `:single-thread-mode` with the value `true`.
Threads are not available in Javascript, so no processes are run concurrnetly
when Propeller is run in Clojurescript.


## CLJS Usage

### Development

Run in development:

```bash
yarn
(mkdir -p target && cp assets/index.html target/)
yarn shadow-cljs watch app
```

`shadow-cljs` will be installed in `node_modules/` when you run `yarn`.

`:dev-http` specifies that `target/` will be served at http://localhost:8080 .

### REPL

After page is loaded, you may also start a REPL connected to browser with:

```bash
yarn shadow-cljs cljs-repl app
```

Once the REPL is loaded, load the core namespace with:

```
(ns propeller.core)
```
Calling `(-main)` will run the default genetic programming problem.

## Description

Propeller is an implementation of the Push programming 
language and the PushGP genetic programming system in Clojure, based
on Tom Helmuth's little PushGP implementation [propel](https://github.com/thelmuth/propel).

For more information on Push and PushGP see 
[http://pushlanguage.org](http://pushlanguage.org).

