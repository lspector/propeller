(ns propeller.tools.character-test
  (:require [clojure.test :as t]
            [propeller.tools.character :as c]))
(t/deftest get-ascii-test
  (t/is (= 97 (c/get-ascii (first "abc")))))

(t/deftest is-letter-test
  (t/is  (c/is-letter (first "abc"))))

(t/deftest is-digit-test
  (t/is (c/is-digit (first "545")))
  (t/is (c/is-digit (char \5))))

(t/deftest is-whitespace-test
  (t/is (c/is-whitespace (char \tab)))
  (t/is (c/is-whitespace (first " hello")))
  (t/is (c/is-whitespace (char \newline)))
  )
