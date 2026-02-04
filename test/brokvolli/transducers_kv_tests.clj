(ns brokvolli.transducers-kv-tests
  (:require
   [brokvolli.core :refer [concatv tconj]]
   [brokvolli.single :refer [transduce-kv]]
   [brokvolli.transducers-kv :refer :all]
   [brokvolli.stateful-transducers-kv :refer :all]
   [clojure.test :refer [are
                         deftest
                         is
                         run-test
                         run-tests
                         testing]]))


(deftest kv-transducer-tests
  (are [x y] (= x y)
    (transduce-kv (map-kv (fn [idx _] (inc idx))) tconj [11 22 33]) [1 2 3]
    (transduce-kv (map-kv (fn [_ x] (inc x))) tconj [11 22 33]) [12 23 34]
    (transduce-kv (filter-kv (fn [idx _] (even? idx))) tconj [11 22 33]) [11 33]
    (transduce-kv (filter-kv (fn [_ x] (odd? x))) tconj [11 22 33]) [11 33]
    (transduce-kv (replace-kv {11 :foo 33 :bar}) tconj [11 22 33]) [:foo 22 :bar]
    (transduce-kv (take-kv 3) tconj [11 22 33 44 55]) [11 22 33]
    (transduce-kv cat-kv tconj [[11] [22] [33]]) [11 22 33]
    (transduce-kv (mapcat-kv (fn [_ x] (reverse x))) tconj [[22 11] [44 33] [66 55]]) [11 22 33 44 55 66]
    (transduce-kv (take-while-kv (fn [_ x] (odd? x))) tconj [11 33 55 22 33 44]) [11 33 55]
    (transduce-kv (take-while-kv (fn [idx _] (<= idx 2))) tconj [11 22 33 44 55]) [11 22 33]
    (transduce-kv (take-nth-kv 2) tconj [11 22 33 44 55 66]) [11 33 55]
    (transduce-kv (drop-kv 3) tconj [11 22 33 44 55]) [44 55]
    (transduce-kv (drop-while-kv (fn [_ x] (even? x))) tconj [22 44 66 11 22 33 44]) [11 22 33 44]
    (transduce-kv (drop-while-kv (fn [keydex _] (<= keydex 2))) tconj [22 44 66 11 22 33 44]) [11 22 33 44]
    (transduce-kv (remove-kv (fn [_ x] (even? x))) tconj [11 22 33 44 55]) [11 33 55]
    (transduce-kv (remove-kv (fn [keydex _] (even? keydex))) tconj [11 22 33 44 55]) [22 44]
    (transduce-kv (partition-by-kv (fn [keydex _] (= 0 (rem keydex 3)))) tconj [11 22 33 44 55 66 77 88 99 111]) [[11] [22 33] [44] [55 66] [77] [88 99] [111]]
    (transduce-kv (partition-all-kv 3) tconj [11 22 33 44 55 66 77 88 99 111]) [[11 22 33] [44 55 66] [77 88 99] [111]]
    (transduce-kv (keep-kv (fn [_ x] (even? x))) tconj [11 22 33 44 55]) [false true false true false]
    (transduce-kv (keep-kv (fn [keydex _] (even? keydex))) tconj [11 22 33 44 55]) [true false true false true]
    (transduce-kv (distinct-kv) tconj [11 22 22 33 33 33 44 44 44 44]) [11 22 33 44]
    (transduce-kv (interpose-kv :foo) tconj [11 22 33 44 55]) [11 :foo 22 :foo 33 :foo 44 :foo 55]
    (transduce-kv (dedupe-kv) tconj [11 22 22 33 33 33 44 44 44 44]) [11 22 33 44]))


(deftest composed-transducer-kv-tests
  (are [x y] (= x y)
    (transduce-kv (comp (map-kv (fn [_ x] (inc x)))
                        (filter-kv (fn [idx _] (even? idx)))
                        (remove-kv (fn [_ x] (= 100 x)))
                        (replace-kv {34 :foo})
                        (take-kv 3)
                        (map-kv (fn [idx x] (vector idx x)))
                        cat-kv
                        (mapcat-kv (fn [_ x] (repeat 3 x))) ;; note: keydexes refer to their 'position' in the original collection
                        (take-while-kv (fn [idx _] (<= idx 3))))
                  tconj
                  [11 22 33 44 55 66 77 88 99])
    [0 0 0 12 12 12 2 2 2 :foo :foo :foo]))


#_(run-tests)

