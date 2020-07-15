# propeller

Yet another Push-based genetic programming system in Clojure.

## Usage

To run PushGP on the default genetic programming problem 
from a REPL, load propel.core into your REPL (i.e. `lein repl`), 
and run `(-main)`.

To run PushGP on the default genetic programming problem from 
command line, execute `lein run`. Command-line arguments may 
be provided to override the defaults specified in `-main`, for 
example, `lein run :population-size 100`. You can use something 
like `lein run | tee outfile` to send output both to the terminal 
and to `outfile`.

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

