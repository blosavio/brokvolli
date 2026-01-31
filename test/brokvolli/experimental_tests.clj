(ns brokvolli.experimental-tests
  (:require
   [brokvolli.core :refer [concatv]]
   [brokvolli.experimental :refer :all]
   [clojure.test :refer [are
                         deftest
                         is
                         run-test
                         run-tests
                         testing]]))


(deftest reduce-offset-index-tests
  (testing "empty `coll`"
    (= (+) (reduce-offset-index + [])))

  (testing "`nil` as collection"
    (= (+) (reduce-offset-index + nil)))

  (testing "zero offset"
    (are [x y] (= x y)
      69 (reduce-offset-index + [11 22 33])

      (reduce-offset-index (fn
                             ([] [])
                             ([acc idx x] (conj acc (vector idx x))))
                           [11 22 33])
      [[0 11]
       [1 22]
       [2 33]]))

  (testing "non-zero offsets"
    (are [x y] (= x y)
      (reduce-offset-index 100
                           (fn
                             ([] [])
                             ([acc idx x] (conj acc (vector (inc idx) x))))
                           [11 22 33])
      [[101 11]
       [102 22]
       [103 33]]

      (reduce-offset-index 100
                           (fn
                             ([] {})
                             ([acc x y] (assoc acc x y)))
                           [11 22 33])
      {100 11,
       101 22,
       102 33})))


;; not intended to be comprehensive, merely demos

(deftest transduce-kv-tests
  (testing "individual transducer-kv"
    (are [x y] (= x y)
      (transduce-kv 100
                    (map-kv #(vector %1 %2))
                    (fn
                      ([] [])
                      ([x] x)
                      ([x y] (conj x y))
                      ([x _ z] (conj x z)))
                    [11 22 33 44 55])
      [[100 11] [101 22] [102 33] [103 44] [104 55]]

      (transduce-kv 100
                    (filter-kv (fn [keydex _] (even? keydex)))
                    (fn
                      ([] [])
                      ([x] x)
                      ([x y] (conj x y))
                      ([x _ z] (conj x z)))
                    [11 22 33 44 55])
      [11 33 55]

      (transduce-kv 100
                    (replace-kv {11 :foo!
                                 33 :bar!
                                 55 :baz!})
                    (fn
                      ([] [])
                      ([x] x)
                      ([x y] (conj x y))
                      ([x _ z] (conj x z)))
                    [11 22 33 44 55])
      [:foo! 22 :bar! 44 :baz!]))

  (testing "composed transducer-kv stacks"
    (are [x y] (= x y)
      (transduce-kv 100
                    (comp (filter-kv (fn [keydex _] (even? keydex)))
                          (map-kv (fn [keydex x] (vector keydex (* 2 x)))))
                    (fn
                      ([] [])
                      ([x] x)
                      ([x y] (conj x y))
                      ([x _ z] (conj x z)))
                    [11 22 33 44 55 66 77 88 99])
      [[100 22] [102 66] [104 110] [106 154] [108 198]]

      (transduce-kv 100
                    (comp (filter-kv (fn [keydex _] (even? keydex)))
                          (map-kv (fn [keydex x] (vector keydex (* 2 x))))
                          (replace-kv {[102 66] :foo!
                                       [106 154] :bar!}))
                    (fn
                      ([] [])
                      ([x] x)
                      ([x y] (conj x y))
                      ([x _ z] (conj x z)))
                    [11 22 33 44 55 66 77 88 99])
      [[100 22] :foo! [104 110] :bar! [108 198]])))


(deftest multi-threaded-transduce-kv-tests
  (testing "basic results"
    (are [x y result] (= x y result)
      (multi-threaded-transduce-kv
       (comp (map-kv (fn [keydex value] [keydex (inc value)]))
             (filter-kv (fn foobar [keydex _] (even? keydex)))
             (replace-kv {[4 56] :foo!}))
       (fn
         ([] [])
         ([x] x)
         ([x y] (conj x y))
         ([x _ z] (conj x z)))
       concatv
       [11 22 33 44 55 66 77 88 99])


      (multi-threaded-transduce-kv
       3
       (comp (map-kv (fn [keydex value] [keydex (inc value)]))
             (filter-kv (fn foobar [keydex _] (even? keydex)))
             (replace-kv {[4 56] :foo!}))
       (fn
         ([] [])
         ([x] x)
         ([x y] (conj x y))
         ([x _ z] (conj x z)))
       (fn
         ([] [])
         ([x] x)
         ([x y] (conj x y))
         ([x _ z] (conj x z)))
       concatv
       [11 22 33 44 55 66 77 88 99])

      [[0 12] [2 34] :foo! [6 78] [8 100]])))


(defn tconj
  {:UUIDv4 #uuid "8ea9cc9f-cc4b-4563-8474-adbc34f4ea52"}
  ([] [])
  ([x] (unreduced x))
  ([x y] (conj x y))
  ([x _ z] (conj x z)))


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
    (transduce-kv (take-while-kv (fn [_ x] (odd? x))) tconj [11 22 33 44 55]) [11 33 55]
    (transduce-kv (take-while-kv (fn [idx _] (even? idx))) tconj [11 22 33 44 55]) [11 33 55]
    (transduce-kv (take-nth-kv 2) tconj [11 22 33 44 55 66]) [11 33 55]
    (transduce-kv (drop-kv 3) tconj [11 22 33 44 55]) [44 55]
    (transduce-kv (drop-while-kv (fn [_ x] (even? x))) tconj [22 44 66 11 22 33 44]) [11 22 33 44]
    (transduce-kv (drop-while-kv (fn [keydex _] (<= keydex 2))) tconj [22 44 66 11 22 33 44]) [11 22 33 44]
    (transduce-kv (remove-kv (fn [_ x] (even? x))) tconj [11 22 33 44 55]) [11 33 55]
    (transduce-kv (remove-kv (fn [keydex _] (even? keydex))) tconj [11 22 33 44 55]) [22 44]
    (transduce-kv (keep-kv (fn [_ x] (even? x))) tconj [11 22 33 44 55]) [false true false true false]
    (transduce-kv (keep-kv (fn [keydex _] (even? keydex))) tconj [11 22 33 44 55]) [true false true false true]
    (transduce-kv (distinct-kv) tconj [11 22 22 33 33 33 44 44 44 44]) [11 22 33 44]
    (transduce-kv (interpose-kv :foo) tconj [11 22 33 44 55]) [11 :foo 22 :foo 33 :foo 44 :foo 55]
    (transduce-kv (dedupe-kv) tconj [11 22 22 33 33 33 44 44 44 44]) [11 22 33 44]))


#_(run-tests)

