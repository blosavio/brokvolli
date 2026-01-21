(ns brokvolli.multi-tests
  (:refer-clojure :exclude [transduce])
  (:require
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
    (split-vector []) [[] []]
    (split-vector [11]) [[] [11]]
    (split-vector [11 22]) [[11] [22]]
    (split-vector [11 22 33]) [[11] [22 33]]
    (split-vector [11 22 33 44]) [[11 22] [33 44]]))


(deftest split-hashmap-tests
  (are [x y] (= x y)
    (split-hashmap {}) [{} {}]
    (split-hashmap {:a 11}) [{} {:a 11}]
    (split-hashmap {:a 11 :b 22}) [{:a 11} {:b 22}]
    (split-hashmap {:a 11 :b 22 :c 33}) [{:a 11} {:b 22, :c 33}]
    (split-hashmap {:a 11 :b 22 :c 33 :d 44}) [{:a 11, :b 22} {:c 33, :d 44}]))


(deftest split-seq-tests
  (are [x y] (= x y)
    (split-seq (range 1 1)) [[] []]
    (split-seq (range 1 2)) [[] [1]]
    (split-seq (range 1 3)) [[1] [2]]
    (split-seq (range 1 4)) [[1] [2 3]]
    (split-seq (range 1 5)) [[1 2] [3 4]]))


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


#_(run-tests)

