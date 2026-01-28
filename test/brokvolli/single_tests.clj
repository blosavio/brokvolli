(ns brokvolli.single-tests
  (:require
   [brokvolli.core :as core]
   [brokvolli.single :refer [transduce-kv]]
   [clojure.test :refer [are
                         deftest
                         is
                         run-test
                         run-tests
                         testing]]))


(deftest transduce-kv-tests
  (testing "Throwing when supplied with unsupported coll"
    (is (thrown? Exception (transduce-kv (comp (map inc)) conj (list 1 2 3)))))

  (testing "off-the-shelf core transducers, ignoring *keydex*"
    (are [coll xform result] (= (transduce-kv (comp xform) conj coll)
                                result)
      [11 22 33] (map inc) [12 23 34]
      [[11] [22] [33]] cat [11 22 33]
      [[22 11] [44 33]] (mapcat reverse) [11 22 33 44]
      [11 22 33 44 55] (filter odd?) [11 33 55]
      [11 22 33 44 55] (remove even?) [11 33 55]
      [11 22 33 44 55] (take 3) [11 22 33]
      [11 22 33 44 55] (take-while #(<= % 33)) [11 22 33]
      [11 22 33 44 55] (take-nth 2) [11 33 55]
      [11 22 33 44 55] (drop 3) [44 55]
      [11 22 33 44 55] (drop-while #(<= % 33)) [44 55]
      [11 22 33 44 55] (replace {11 :foo
                                 33 :bar
                                 55 :baz}) [:foo 22 :bar 44 :baz]
      [11 33 22 44 55 77 66 88] (partition-by even?) [[11 33]
                                                      [22 44]
                                                      [55 77]
                                                      [66 88]]
      [11 22 33 44 55 66 77 88] (partition-all 3) [[11 22 33]
                                                   [44 55 66]
                                                   [77 88]]
      [11 22 33 44 55] (keep even?) [false true false true false]
      [11 22 11 33 22 44 33 55 44] (distinct) [11 22 33 44 55]
      [11 22 33] (interpose :foo) [11 :foo 22 :foo 33]
      [11 22 22 33 33 33] (dedupe) [11 22 33]))

  (testing "off-the-shelf core transducers, only *keydex*"
    (are [coll xform result] (= (transduce-kv (comp xform) conj coll)
                                result)
      [11 22 33] (map (fn [_ ] (inc core/*keydex*))) [1 2 3]
      [11 22 33 44 55] (filter (fn [_] (even? core/*keydex*))) [11 33 55]
      [11 22 33 44 55] (remove (fn [_] (odd? core/*keydex*))) [11 33 55]
      [11 22 33 44 55] (take-while (fn [_] (<= core/*keydex* 2))) [11 22 33]
      [11 22 33 44 55] (drop-while (fn [_] (<= core/*keydex* 2))) [44 55]
      [11 22 33] (partition-by (fn [_] (even? core/*keydex*))) [[11] [22] [33]]
      [11 22 33 44 55] (keep (fn [_] (even? core/*keydex*))) [true false true false true]))

  (testing "off-the-shelf core transducers, value and *keydex*"
    (are [coll xform result] (= (transduce-kv (comp xform) conj coll)
                                result)
      [11 22 33] (map #(+ % (inc core/*keydex*))) [12 24 36]
      [11 33 55 77 99] (filter #(and (odd? %) (odd? core/*keydex*))) [33 77]
      [11 22 33 44 55] (take-while #(or (<= % 33) (<= core/*keydex* 1))) [11 22 33]))


  (testing "off-the-shelf core transducers, composition"
    (are [coll xform result] (= (transduce-kv xform conj coll)
                                result)
      [11 22 33 44 55 66 77 88 99]
      (comp (map inc)
            (filter even?)
            (take 3))
      [12 34 56]

      [11 22 33 44 55 66 77 88 99]
      (comp (map inc)
            (filter (fn [_] (even? core/*keydex*)))
            (take-while (fn [_] (<= core/*keydex* 5))))
      [12 34 56]))

  (testing "transducing a hashmap, comparing `transduce` and `transduce-kv`"
    (are [x y] (= x y)
      (transduce (comp (map #(update % 1 inc))
                       (filter #(even? (second %))))
                 (completing
                  (fn
                    ([] {})
                    ([result [k v]] (assoc result k v))))
                 {:a 11 :b 22 :c 33 :d 44 :e 55})

      (transduce-kv (comp (map inc)
                          (filter even?))
                    (completing
                     (fn
                       ([] {})
                       ([result value] (assoc result core/*keydex* value))))
                    {:a 11 :b 22 :c 33 :d 44 :e 55})))

  (testing "nested `comp-kv`"
    (are [x y] (= x y)
      (transduce-kv (comp
                     (comp (map inc)
                           (filter even?))
                     (take 3))
                    conj
                    [11 22 33 44 55 66 77 88 99])
      [12 34 56]

      (transduce-kv (comp
                     (map inc)
                     (comp
                      (filter even?)
                      (comp
                       (take 3))))
                    conj
                    [11 22 33 44 55 66 77 88 99])
      [12 34 56]))

  (testing "`*keydex*` properly propagated when expanding and contracting"
    (are [x y] (= x y)
      (transduce-kv (comp (remove (fn [_] (= core/*keydex* 1)))
                          (map #(vector core/*keydex* %)))
                    conj
                    [11 22 33])
      [[0 11] [2 33]]

      (transduce-kv (comp (mapcat #(repeat 3 %))
                          (map #(vector core/*keydex* %)))
                    conj
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
                                (transduce-kv (map inc) conj coll)))
        clojure.lang.PersistentVector (vector 11 22 33)))
    (testing "associative collections"
      (are [coll-type coll] (and
                             (instance? coll-type coll)
                             (instance? clojure.lang.IKVReduce coll)
                             (= {:a 12, :b 23, :c 34}
                                (transduce-kv (map #(inc %))
                                              (completing
                                               (fn
                                                 ([] {})
                                                 ([result value]
                                                  (assoc result core/*keydex* value))))
                                              coll)))
        clojure.lang.PersistentHashMap (hash-map :a 11 :b 22 :c 33)
        clojure.lang.PersistentArrayMap (array-map :a 11 :b 22 :c 33)
        clojure.lang.PersistentTreeMap (sorted-map :a 11 :b 22 :c 33)))))


#_(run-tests)

