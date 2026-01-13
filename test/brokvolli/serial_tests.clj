(ns brokvolli.serial-tests
  (:require
   [brokvolli.serial :refer :all]
   [clojure.test :refer [are
                         deftest
                         is
                         run-test
                         run-tests
                         testing]]))


(deftest kv-ify-tests
  (let [f ((kv-ify (map #(inc (second %)))) conj)]
    (are [x y] (= x y)
      (f) []
      (f 99) 99
      (f [99] [11 22]) [99 23])))


(deftest transduce-kv-tests
  (testing "Throwing when supplied with unsupported coll"
    (is (thrown? Exception (transduce-kv identity conj (list 1 2 3)))))
  
  (testing "Using `comp` + `kv-ify`"
    (are [x y] (= x y)
      (transduce-kv (kv-ify (map #(vector (% 0) (inc (% 1)))))
                    conj
                    [11 22 33 44 55 66 77 88 99])
      [[0 12] [1 23] [2 34] [3 45] [4 56] [5 67] [6 78] [7 89] [8 100]]

      (transduce-kv (comp
                     (kv-ify (map #(vector (% 0) (inc (% 1)))))
                     (filter #(even? (% 1))))
                    conj
                    [11 22 33 44 55 66 77 88 99])
      [[0 12] [2 34] [4 56] [6 78] [8 100]]

      (transduce-kv (comp
                     (kv-ify (map #(vector (% 0) (inc (% 1)))))
                     (filter #(even? (% 1)))
                     (take 3)
                     (map second)) ;; peel off keydexes
                    conj
                    [11 22 33 44 55 66 77 88 99])
      [12 34 56]))

  (testing "Using `comp-kv`"
    (are [x y] (= x y)
      (transduce-kv (comp-kv (map #(vector (% 0) (inc (% 1)))))
                    conj
                    [11 22 33 44 55 66 77 88 99])
      [12 23 34 45 56 67 78 89 100]

      (transduce-kv (comp-kv
                     (map #(vector (% 0) (inc (% 1))))
                     (filter #(even? (% 1)))
                     (take 3))
                    conj
                    [11 22 33 44 55 66 77 88 99])
      [12 34 56]))

  (testing "off-the-shelf core transducers, group 1"
    (are [xform result] (= (transduce-kv (comp-kv xform) conj [11 22 33 44 55])
                           result)
      (map #(vector (% 0) (inc (% 1)))) [12 23 34 45 56]
      (filter #(<= (% 1) 33)) [11 22 33]
      (remove #(even? (% 1))) [11 33 55]
      (take 3) [11 22 33]
      (take-while #(<= (% 0) 2)) [11 22 33]
      (take-nth 2) [11 33 55]
      (drop 2) [33 44 55]
      (drop-while #(<= (% 0) 2)) [44 55]
      (replace {[0 11] [0 :foo]
                [2 33] [2 :bar]
                [4 55] [4 :baz]}) [:foo 22 :bar 44 :baz]
      (interpose [:ignored :foo]) [11 :foo 22 :foo 33 :foo 44 :foo 55]))

  (testing "off-the-shelf core transducers, group 2"
    (are [x y] (= x y)
      (transduce-kv (kv-ify cat) conj [[11] [22] [22]])
      [0 [11] 1 [22] 2 [22]]

      (transduce-kv (kv-ify (mapcat #(reverse (% 1))))
                    conj
                    [[22 11] [44 33] [66 55]])
      [11 22 33 44 55 66]

      (transduce-kv (kv-ify (partition-by #(<= (% 0) 2)))
                    conj
                    [11 22 33 44 55])
      [[[0 11] [1 22] [2 33]] [[3 44] [4 55]]]

      (transduce-kv (kv-ify (partition-all 2))
                    conj
                    [11 22 33 44 55])
      [[[0 11] [1 22]]
       [[2 33] [3 44]]
       [[4 55]]]

      (transduce-kv (kv-ify (keep #(even? (% 0))))
                    conj
                    [11 22 33 44 55])
      [true false true false true]

      #_(transduce-kv (kv-ify (distinct))
                      conj
                      [11 22 33 44 55])

      #_(transduce-kv (kv-ify (dedupe))
                      conj
                      [11 11 22 33 33 44 55 55 55 55 55])

      #_(transduce-kv (kv-ify (random-sample 0.5))
                      conj
                      [11 22 33 44 55]))))


#_(run-tests)

