(ns brokvolli.single-tests
  (:require
   [brokvolli.core :refer [*keydex*
                           kv-ize
                           tassoc
                           tconj]]
   [brokvolli.single :refer [transduce-kv]]
   [brokvolli.stateful-transducers-kv :refer :all]
   [brokvolli.transducers-kv :refer :all]
   [clojure.test :refer [are
                         deftest
                         is
                         run-test
                         run-tests
                         testing]]))


(deftest transduce-kv-tests
  (testing "Throwing when supplied with unsupported coll"
    (is (thrown? Exception (transduce-kv (comp (map inc)) conj (list 1 2 3)))))

  (testing "ignoring keydex"
    (are [coll xform-ized xform result] (= (transduce-kv (kv-ize xform-ized) conj coll)
                                           (transduce-kv xform tconj coll)
                                           result)
      [11 22 33]
      (map inc)
      (map-kv (fn [_ x] (inc x)))
      [12 23 34]

      [[11] [22] [33]]
      cat
      cat-kv
      [11 22 33]

      [[22 11] [44 33]]
      (mapcat reverse)
      (mapcat-kv (fn [_ x] (reverse x)))
      [11 22 33 44]

      [11 22 33 44 55]
      (filter odd?)
      (filter-kv (fn [_ x] (odd? x)))
      [11 33 55]

      [11 22 33 44 55]
      (remove even?)
      (remove-kv (fn [_ x] (even? x)))
      [11 33 55]

      [11 22 33 44 55]
      (take 3)
      (take-kv 3)
      [11 22 33]

      [11 22 33 44 55]
      (take-while #(<= % 33))
      (take-while-kv (fn [_ x] (<= x 33)))
      [11 22 33]

      [11 22 33 44 55]
      (take-nth 2)
      (take-nth-kv 2)
      [11 33 55]

      [11 22 33 44 55]
      (drop 3)
      (drop-kv 3)
      [44 55]

      [11 22 33 44 55]
      (drop-while #(<= % 33))
      (drop-while-kv (fn [_ x] (<= x 33)))
      [44 55]

      [11 22 33 44 55]
      (replace {11 :foo
                33 :bar
                55 :baz})
      (replace-kv {11 :foo
                   33 :bar
                   55 :baz})
      [:foo 22 :bar 44 :baz]

      [11 33 22 44 55 77 66 88]
      (partition-by even?)
      (partition-by-kv (fn [_ x] (even? x)))
      [[11 33]
       [22 44]
       [55 77]
       [66 88]]

      [11 22 33 44 55 66 77 88]
      (partition-all 3)
      (partition-all-kv 3)
      [[11 22 33]
       [44 55 66]
       [77 88]]

      [11 22 33 44 55]
      (keep even?)
      (keep-kv (fn [_ x] (even? x)))
      [false true false true false]

      [11 22 11 33 22 44 33 55 44]
      (distinct)
      (distinct-kv)
      [11 22 33 44 55]

      [11 22 33]
      (interpose :foo)
      (interpose-kv :foo)
      [11 :foo 22 :foo 33]

      [11 22 22 33 33 33]
      (dedupe)
      (dedupe-kv)
      [11 22 33]))


  (testing "only keydex"
    (are [coll xform-ized xform result] (= (transduce-kv (kv-ize xform-ized) conj coll)
                                           (transduce-kv xform tconj coll)
                                           result)
      [11 22 33]
      (map (fn [_] (inc *keydex*)))
      (map-kv (fn [keydex _] (inc keydex)))
      [1 2 3]

      [11 22 33 44 55]
      (filter (fn [_] (even? *keydex*)))
      (filter-kv (fn [keydex _] (even? keydex)))
      [11 33 55]

      [11 22 33 44 55]
      (remove (fn [_] (odd? *keydex*)))
      (remove-kv (fn [keydex _] (odd? keydex)))
      [11 33 55]

      [11 22 33 44 55]
      (take-while (fn [_] (<= *keydex* 2)))
      (take-while-kv (fn [keydex _] (<= keydex 2)))
      [11 22 33]

      [11 22 33 44 55]
      (drop-while (fn [_] (<= *keydex* 2)))
      (drop-while-kv (fn [keydex _] (<= keydex 2)))
      [44 55]

      [11 22 33]
      (partition-by (fn [_] (even? *keydex*)))
      (partition-by-kv (fn [keydex _] (even? keydex)))
      [[11] [22] [33]]

      [11 22 33 44 55]
      (keep (fn [_] (even? *keydex*)))
      (keep-kv (fn [keydex _] (even? keydex)))
      [true false true false true]))


  (testing "value and keydex"
    (are [coll xform-ized xform result] (= (transduce-kv (kv-ize xform-ized) conj coll)
                                           (transduce-kv xform tconj coll)
                                           result)
      [11 22 33]
      (map #(+ % (inc *keydex*)))
      (map-kv (fn [keydex x] (+ x (inc keydex))))
      [12 24 36]

      [11 33 55 77 99]
      (filter #(and (odd? %) (odd? *keydex*)))
      (filter-kv (fn [keydex x] (and (odd? x) (odd? keydex))))
      [33 77]

      [11 22 33 44 55]
      (take-while #(or (<= % 33) (<= *keydex* 1)))
      (take-while-kv (fn [keydex x] (or (<= x 33) (<= keydex 1))))
      [11 22 33]))


  (testing "composition"
    (are [coll xform-ized xform result] (= (transduce-kv (kv-ize xform-ized) conj coll)
                                           (transduce-kv xform tconj coll)
                                           result)
      [11 22 33 44 55 66 77 88 99]
      (comp (map inc)
            (filter even?)
            (take 3))
      (comp (map-kv (fn [_ x] (inc x)))
            (filter-kv (fn [_ x] (even? x)))
            (take-kv 3))
      [12 34 56]

      [11 22 33 44 55 66 77 88 99]
      (comp (map inc)
            (filter (fn [_] (even? *keydex*)))
            (take-while (fn [_] (<= *keydex* 5))))
      (comp (map-kv (fn [_ x] (inc x)))
            (filter-kv (fn [keydex _] (even? keydex)))
            (take-while-kv (fn [keydex _] (<= keydex 5))))
      [12 34 56]))

  (testing "transducing a hashmap, comparing `transduce` and `transduce-kv`"
    (are [x y z] (= x y z)
      (transduce (comp (map #(update % 1 inc))
                       (filter #(even? (second %))))
                 (completing
                    (fn
                      ([] {})
                      ([result [k v]] (assoc result k v))))
                 {:a 11 :b 22 :c 33 :d 44 :e 55})

      (transduce-kv (kv-ize
                     (comp (map inc)
                           (filter even?)))
                    tassoc
                    {:a 11 :b 22 :c 33 :d 44 :e 55})

      (transduce-kv (comp (map-kv (fn [_ x] (inc x)))
                          (filter-kv (fn [_ x] (even? x))))
                    tassoc
                    {:a 11 :b 22 :c 33 :d 44 :e 55})))

  (testing "nested composition"
    (are [x y] (= x y)
      (transduce-kv (kv-ize
                     (comp
                      (comp (map inc)
                            (filter even?))
                      (take 3)))
                    conj
                    [11 22 33 44 55 66 77 88 99])
      [12 34 56]

      (transduce-kv (comp
                     (comp (map-kv (fn [_ x] (inc x)))
                           (filter-kv (fn [_ x] (even? x))))
                     (take-kv 3))
                    tconj
                    [11 22 33 44 55 66 77 88 99])
      [12 34 56]

      (transduce-kv (kv-ize
                     (comp
                      (map inc)
                      (comp
                       (filter even?)
                       (comp
                        (take 3)))))
                    conj
                    [11 22 33 44 55 66 77 88 99])
      [12 34 56]

      (transduce-kv (comp
                     (map-kv (fn [_ x] (inc x)))
                     (comp
                      (filter-kv (fn [_ x] (even? x)))
                      (comp
                       (take-kv 3))))
                    tconj
                    [11 22 33 44 55 66 77 88 99])
      [12 34 56]))

  (testing "keydex properly propagated when expanding and contracting"
    (are [x y z] (= x y z)
      (transduce-kv (kv-ize
                     (comp (remove (fn [_] (= *keydex* 1)))
                           (map #(vector *keydex* %))))
                    conj
                    [11 22 33])
      (transduce-kv (comp (remove-kv (fn [keydex _] (= keydex 1)))
                          (map-kv (fn [keydex x] (vector keydex x))))
                    tconj
                    [11 22 33])
      [[0 11] [2 33]]

      (transduce-kv (kv-ize
                     (comp (mapcat #(repeat 3 %))
                           (map #(vector *keydex* %))))
                    conj
                    [11 22 33])
      (transduce-kv (comp (mapcat-kv (fn [_ x] (repeat 3 x)))
                          (map (fn [keydex x] (vector keydex x))))
                    tconj
                    [11 22 33])
      [[0 11] [0 11] [0 11]
       [1 22] [1 22] [1 22]
       [2 33] [2 33] [2 33]]))

  (testing "`coll` implementation requirements: `clojure.lang.IKVReduce`"
    (testing "sequential collections"
      (are [coll-type coll] (and
                             (instance? coll-type coll)
                             (instance? clojure.lang.IKVReduce coll)
                             (= [12 23 34]
                                (transduce-kv (kv-ize (map inc)) conj coll)
                                (transduce-kv (map-kv (fn [_ x] (inc x))) tconj coll)))
        clojure.lang.PersistentVector (vector 11 22 33)))
    (testing "associative collections"
      (are [coll-type coll] (and
                             (instance? coll-type coll)
                             (instance? clojure.lang.IKVReduce coll)
                             (= {:a 12, :b 23, :c 34}
                                (transduce-kv (kv-ize (map inc))
                                              tassoc
                                              coll)
                                (transduce-kv (map-kv (fn [_ x] (inc x)))
                                              tassoc
                                              coll)))
        clojure.lang.PersistentHashMap (hash-map :a 11 :b 22 :c 33)
        clojure.lang.PersistentArrayMap (array-map :a 11 :b 22 :c 33)
        clojure.lang.PersistentTreeMap (sorted-map :a 11 :b 22 :c 33)))))


#_(run-tests)

