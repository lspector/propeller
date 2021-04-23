# propeller

Yet another Push-based genetic programming system in Clojure.

## Usage

To run PushGP from a REPL, load propel.core into your REPL (i.e. `lein repl`), 
and run `-main` with arguments including, first, the problem name, for example:
`(-main 'simple-regression)` or `(-main 'simple-regression :population-size 100)`.

To run PushGP on the genetic programming problem p from the 
command line, execute `lein run p`. For example `lein run simple-regression`. Additional command-line arguments may 
be provided to override the default key/value pairs specified in `-main`, for 
example, `lein run simple-regression :population-size 100`. You can use something 
like `lein run simple-regression | tee outfile` to send output both to the terminal 
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

