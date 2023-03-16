# propeller

Yet another Push-based genetic programming system in Clojure.

Full documentation is on the GitHub pages link.

## Usage

If you are working in a Clojure IDE with an integrated REPL, the first
thing you may want to do is to open `src/propeller/session.cljc` and 
evaluate the namespace declaration and the commented-out expressions 
therein. These demonstrate core components of Propeller including
complete genetic programming runs.

To run Propeller on a genetic programming that is defined within this
project from the command line, you will probably want to use either
the Clojure [CLI tools](https://clojure.org/guides/deps_and_cli) or 
[leiningen](https://leiningen.org).

The instructions below are written for leiningen. If you are using
the CLI tools instead, then replace `lein run -m` in each command
with `clj -M -m`.

If you are using leiningen, then you can start a ruh with the command 
`lein run -m <namespace>`, replacing `<namespace>` 
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

