(ns propeller.utils-test
  (:require [clojure.test :as t]
            [propeller.utils :as u]))
(t/deftest first-non-nil-test
  (t/is (= 1 (u/first-non-nil '(1 2 3))))
  (t/is (= nil (u/first-non-nil [])))
  (t/is (= nil (u/first-non-nil []))))

(t/deftest indexof-test
  (t/is (= 2 (u/indexof :b '(:z :y :b :q :w))))
  (t/is (= 1 (u/indexof :b '(:a :b :c :b :a))))
  (t/is (= -1 (u/indexof :b '(:a :c :q :r)))))

(t/deftest not-lazy-test
  (t/is (= '(:a :b :c) (u/not-lazy [:a :b :c])))
  (t/is (= '(:a :b :c) (u/not-lazy '(:a :b :c)))))
(t/deftest ensure-list-test
  (t/is (= '(0 1 2 3 4) (u/ensure-list (range 5))))
  (t/is (= '([:a :b :c]) (u/ensure-list [:a :b :c]))))

(t/deftest random-instruction-test
  (t/is
    (letfn [(instruct [] 1)]
      (let [test (u/random-instruction [instruct 2])]
            (if (= 1 test)
              true
              (= 2 test))))))


(t/deftest count-points-test
  (t/is (= 6 (u/count-points '(:a :b (:c :d)))))
  (t/is (= 1 (u/count-points '())))
  (t/is (= 2 (u/count-points '(:a)))))

;(t/deftest seq-zip-test
;  (t/is ))

;(t/deftest depth-test
;  (t/is (= 3 (u/depth ()))))

