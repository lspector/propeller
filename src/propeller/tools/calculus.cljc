(ns propeller.tools.calculus)

(defonce ^:const dx 0.0001)

(defn deriv
  "Returns the derivative of f evaluated at c. If called with only one argument,
  it returns the derivative function."
  ([f c]
   ((deriv f) c))
  ([f]
   (fn [x]
     (/ (- (f (+ x dx)) (f x)) dx))))

(defn integrate
  "Returns the definite integral of f over [a, b] using Simpson's method.
  If called with only one argument (the function), returns the indefinite
  integral, which takes as input a value x and (optionally) a constant c."
  ([f]
   (fn this
     ([x] (this x 0))
     ([x c] (+ (integrate f 0 x) c))))
  ([f a b]
   (let [n (/ 1 dx)
         h (/ (- b a) n)]
     (loop [i 1
            sum1 (f (+ a (/ h 2)))
            sum2 0]
       (if (< i n)
         (recur (inc i)
                (+ sum1 (f (+ a (* h i) (/ h 2))))
                (+ sum2 (f (+ a (* h i)))))
         (* (/ h 6) (+ (f a) (f b) (* 4 sum1) (* 2 sum2))))))))

