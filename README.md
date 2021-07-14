# propeller

Yet another Push-based genetic programming system in Clojure.

## Usage

If you have installed [leiningen](https://leiningen.org), which is a tool
for running Clojure programs, then you can run Propeller on a genetic
programming problem that is defined within this project from the command
line with the command `lein run -m <namespace>`, replacing `<namespace>` 
with the actual namespace that you will find at the top of the problem file. 

For example, you can run the simple-regression genetic programming problem with:

```
lein run -m propeller.problems.simple-regression
```

Additional command-line arguments may
be provided to override the default key/value pairs specified in the 
problem file, for example:


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
then you should call `-main` without arguments, as `(-main)`.


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

Propel is an implementation of the Push programming 
language and the PushGP genetic programming system in Clojure.

For more information on Push and PushGP see 
[http://pushlanguage.org](http://pushlanguage.org).

