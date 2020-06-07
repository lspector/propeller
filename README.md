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

## Description

Propel is an implementation of the Push programming 
language and the PushGP genetic programming system in Clojure.

For more information on Push and PushGP see 
[http://pushlanguage.org](http://pushlanguage.org).

