(ns brokvolli.multi-tests
  (:refer-clojure :exclude [transduce])
  (:require
   [brokvolli.core :refer [*keydex*]]
   [brokvolli.multi :refer :all]
   [clojure.test :refer [are
                         deftest
                         is
                         run-test
                         run-tests
                         testing]]))


(deftest concatv-tests
  (are [x y] (= x y)
    (concatv) []
    (concatv [] []) []
    (concatv [11 22 33]) [11 22 33]
    (concatv [11 22 33] []) [11 22 33]
    (concatv [] [11 22 33]) [11 22 33]
    (concatv [11 22 33] [44 55 66]) [11 22 33 44 55 66]
    (type (concatv [11 22 33] [44 55 66])) clojure.lang.PersistentVector))


(deftest split-vector-tests
  (are [x y] (= x y)
    (split-vector []) [[] [] 0]
    (split-vector [11]) [[] [11] 0]
    (split-vector [11 22]) [[11] [22] 1]
    (split-vector [11 22 33]) [[11] [22 33] 1]
    (split-vector [11 22 33 44]) [[11 22] [33 44] 2]
    (split-vector [11 22 33 44 55]) [[11 22] [33 44 55] 2]))


(deftest split-hashmap-tests
  (are [x y] (= x y)
    (split-hashmap (sorted-map)) [{} {} nil]
    (split-hashmap (sorted-map :a 11)) [{} {:a 11} nil]
    (split-hashmap (sorted-map :a 11 :b 22)) [{:a 11} {:b 22} nil]
    (split-hashmap (sorted-map :a 11 :b 22 :c 33)) [{:a 11} {:b 22, :c 33} nil]
    (split-hashmap (sorted-map :a 11 :b 22 :c 33 :d 44)) [{:a 11, :b 22} {:c 33, :d 44} nil]))


(deftest split-seq-tests
  (are [x y] (= x y)
    (split-seq (range 1 1)) [[] [] 0]
    (split-seq (range 1 2)) [[] [1] 0]
    (split-seq (range 1 3)) [[1] [2] 1]
    (split-seq (range 1 4)) [[1] [2 3] 1]
    (split-seq (range 1 5)) [[1 2] [3 4] 2]
    (split-seq (range 1 6)) [[1 2] [3 4 5] 2]))


(deftest transduce-tests
  (testing "`nil`' coll"
    (are [x] (= ::bar x)
      (transduce (constantly ::foo) (constantly ::bar) nil)))

  (testing "empty coll"
    (are [x] (= x ::empty-sentinel)
      (transduce (map identity) + (constantly ::empty-sentinel) [])))

  (testing "identity forumlations"
    (are [v core example-1 example-2] (= v core example-1 example-2 )
      [11 22 33 44 55]
      (clojure.core/transduce (map identity) conj [11 22 33 44 55])
      (transduce (map identity) conj [11 22 33 44 55])
      (transduce 2 (map identity) conj concatv [11 22 33 44 55])))

  (testing "equivalency of different sized sub-reductions"
    (are [n] (= (transduce n (map identity) + + (vec (range 1E3))))
      2 4 8 16 32 64 128 256 512 1032))

  (testing "sum numbers in a vector"
    (are [a r core example-1 example-2] (= 170 a r core example-1 example-2)
      (apply + (map inc [11 22 33 44 55]))
      (reduce #(+ %1 (inc %2)) 0 [11 22 33 44 55])
      (clojure.core/transduce (map inc) + [11 22 33 44 55])
      (transduce   (map inc) +   [11 22 33 44 55])
      (transduce 2 (map inc) + + [11 22 33 44 55])))

  (testing "compare to `clojure.core/transduce`"
    (are [x y] (= x y)
      (clojure.core/transduce (filter number?) + [11 :foo 22 :bar 33])
      (transduce (filter number?) + + [11 :foo 22 :bar 33])

      (clojure.core/transduce (map inc) conj [11 22 33 44 55 66 77 88 99])
      (transduce 3 (map inc) conj concatv [11 22 33 44 55 66 77 88 99])

      (reduce #(assoc %1 (key %2) (inc (val %2))) {} {:a 11 :b 22 :c 33 :d 44 :e 55})
      (clojure.core/transduce (map #(update % 1 inc)) conj {} {:a 11 :b 22 :c 33 :d 44 :e 55})))

  (testing "stack of xforms"
    (are [x y] (= x y)
      (transduce (comp (map inc)
                       (filter even?)
                       (remove #(< 70 %)))
                 conj
                 concatv
                 [11 22 33 44 55 66 77 88 99])
      [12 34 56]

      (transduce (comp (map #(update % 1 inc))
                       (filter #(even? (second %)))
                       (remove #(< 70 (second %))))
                 (completing
                  (fn
                    ([] {})
                    ([result value] (conj result value))))
                 merge
                 {:a 11 :b 22 :c 33 :d 44 :e 55 :f 66 :g 77 :h 88 :i 99})
      {:e 56, :c 34, :a 12}

      (transduce (comp (map inc)
                       (filter even?)
                       (remove #(< 70 %)))
                 conj
                 concatv
                 (range 11 99 11))
      [12 34 56])))


(deftest transduce-kv-tests
  (testing "`nil` coll"
    (are [x] (= ::bar x)
      (transduce-kv (constantly ::foo) (constantly ::bar) nil)))

  (testing "empty `coll`"
    (are [x] (= ::bar x)
      (transduce-kv (constantly ::foo) + (constantly ::bar) [])))

  (testing "basic examples"
    (testing "vector `coll`"
      (are [x y] (= x y)
        (transduce-kv 3
                      (comp-kv-offset (map #(vector *keydex* %)))
                      conj
                      concatv
                      [11 22 33 44 55])
        [[0 11] [1 22] [2 33] [3 44] [4 55]]

        (transduce-kv (comp-kv-offset (map #(vector *keydex* %)))
                      conj
                      concatv
                      [11 22 33 44 55])
        [[0 11] [1 22] [2 33] [3 44] [4 55]]))

    (testing "hashmaps `coll`"
      (are [x y] (= x y)
        (transduce-kv 3
                      (comp-kv-offset (map #(array-map :key *keydex*
                                                       :value (+ 100 %))))
                      (completing
                       (fn
                         ([] {})
                         ([result val] (assoc result *keydex* val))))
                      merge
                      (hash-map :a 11 :b 22 :c 33 :d 44 :e 55))
        {:a {:key :a :value 111}
         :b {:key :b :value 122}
         :c {:key :c :value 133}
         :d {:key :d :value 144}
         :e {:key :e :value 155}}

        (transduce-kv (comp-kv-offset (map #(array-map :key *keydex*
                                                       :value (+ 100 %))))
                      (completing
                       (fn
                         ([] {})
                         ([result val] (assoc result *keydex* val))))
                      merge
                      (hash-map :a 11 :b 22 :c 33 :d 44 :e 55))
        {:a {:key :a :value 111}
         :b {:key :b :value 122}
         :c {:key :c :value 133}
         :d {:key :d :value 144}
         :e {:key :e :value 155}}


        (transduce-kv 3
                      (comp-kv-offset (map #(array-map :key *keydex*
                                                       :value (+ 100 %))))
                      conj
                      concatv
                      (sorted-map :a 11 :b 22 :c 33 :d 44 :e 55))
        [{:key :a :value 111}
         {:key :b :value 122}
         {:key :c :value 133}
         {:key :d :value 144}
         {:key :e :value 155}])))

  (testing "stacked `xform`"
    (are [x y] (= x y)
      (transduce-kv 3
                    (comp-kv-offset (map inc)
                                    (filter (fn [_] (even? *keydex*)))
                                    (remove (fn [_] (= *keydex* 2)))
                                    (map #(array-map :idx *keydex* :value %)))
                    conj
                    concatv
                    [11 22 33 44 55 66 77 88 99])
      [{:idx 0 :value 12}
       {:idx 4 :value 56}
       {:idx 6 :value 78}
       {:idx 8 :value 100}])))


#_(run-tests)

