(ns propeller.tools.character)

(defn is-letter
  "Returns true if the given character is a letter, A-Z or a-z."
  [c]
  (<= (int \A) (int c) (int \z)))


(defn is-digit
  "Returns true if the given character is a digit, 0-9."
  [c]
  (<= (int \0) (int c) (int \9)))


(defn is-whitespace
  "Returns true if the given character is whitespace (newline, space, tab)."
  [c]
  (contains? #{(int \newline) (int \tab) (int \space)} (int c)))
