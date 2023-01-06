(ns propeller.tools.math-test
  (:require [clojure.test :as t]
            [propeller.tools.math :as m]))
(t/deftest not-lazy-test
  (t/is (= 1 (m/abs -1)))
  (t/is (= 1 (m/abs 1)))
  (t/is (= 0 (m/abs 0))))

(t/deftest approx=-test
  (t/is (m/approx= 3 5 2))
  (t/is (not (m/approx= 5 3 1.5)))
  (t/is (m/approx= -1 -5 9)))

(t/deftest ceil-test
  (t/is (= 5.0 (m/ceil 4.9)))
  (t/is (= 5.0 (m/ceil 4.5)))
  (t/is (= -10.0 (m/ceil -10.5 ))))

(t/deftest cos-test
  (t/is (= 1.0 (m/cos 0)))
  (t/is (= -1.0 (m/cos (* 3 m/PI)))))

(t/deftest div-test
  (t/is (= 0.1 (m/div 1 10))))

(t/deftest exp-test
  (t/is (m/approx= (m/exp 2) 7.3890 0.001)))

(t/deftest floor-test
  (t/is (= 4.0 (m/floor 4.9)))
  (t/is (= 4.0 (m/floor 4.5)))
  (t/is (= -11.0 (m/floor -10.5))))

(t/deftest log-test
  (t/is (= (m/log (m/exp 1)) 1.0))
  (t/is (= (m/log 10 10) 1.0)))

(t/deftest pow-test
  (t/is (= (m/pow 5 2) 25.0)))

(t/deftest root
  (t/is (= (m/root 16 2) 4.0)))

(t/deftest sign-test
  (t/is (= -1 (m/sign -10)))
  (t/is (= 1 (m/sign 10)))
  (t/is (= 0 (m/sign 0))))

(t/deftest sin-test
   (t/is (= 1.0 (m/sin (/ m/PI 2))))
   (t/is (m/approx= 0 (m/sin  m/PI) 0.0001)))

(t/deftest sqrt-test
  (t/is (= (m/sqrt 4) 2.0)))

(t/deftest square-test
  (t/is (= (m/square 5) 25)))

(t/deftest tan-test
      (t/is (m/approx= (m/tan (/  m/PI 4)) 1.0 0.00001))
      (t/is (= (m/tan 0) 0.0)))

(t/deftest mean-test
  (t/is (= (m/mean []) 0.0))
  (t/is (= (m/mean [1 2 3 4 5]) 3.0))
  (t/is (= (m/mean '(6 7 8 9 10)) 8.0) 8.0))

(t/deftest median-test
  (t/is (= (m/median [1 2 3 4 5]) 3))
  (t/is (= (m/median '(1 2 3 4 5 6)) 3.5)))

(t/deftest median-absolute-deviation-test
  (t/is (= (m/median-absolute-deviation [1 2 3 4 5]) 1))
  (t/is (= (m/median-absolute-deviation '(1 2 3 4 5 6)) 1.5)))

