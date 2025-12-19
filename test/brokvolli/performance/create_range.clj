(ns brokvolli.performance.create-range
  "Explore different ways to create a sequential of monotonically increasing
  integers."
  (:require [clojure.test :refer [are
                                  deftest
                                  is
                                  run-test
                                  run-tests
                                  testing]]))


;; naive, base case

(defn long-range
  "Returns a fully-realized `clojure.lang.LongRange` from zero to `n`."
  {:UUIDv4 #uuid "b034f6a3-571f-462d-82ca-3578bf3d3cd6"}
  [n]
  (doall (range n)))


(defn vector-range-1
  "Returns a `clojure.lang.PersistentVector` from zero to `n`. Constructed with
  `into`."
  {:UUIDv4 #uuid "c51a1fd5-e51f-4fc1-93f0-8b04c3a51d83"}
  [n]
  (into [] (range n)))


(defn vector-range-2
  "Returns a 'clojure.lang.PersistentVector` from zero to `n`. Constructed with
  `vec`"
  {:UUIDv4 #uuid "15e0e67a-c7af-43cb-9e6a-1edd9dfd071f"}
  [n]
  (doall (vec (range n))))


(defn transducer-range
  "Returns a `clojure.lang.PersistentVector` from zero to `n`. Constructed with
  transducer variant of `into`."
  {:UUIDv4 #uuid "26422712-ca81-45a7-89c8-07291abd5e1a"}
  [n]
  (into [] identity (range n)))


(defn transient-range
  "Returns a `clojure.lang.PersistentVector` from zero to `n`. Constructed by
  conj-ing onto a transient vector."
  {:UUIDv4 #uuid "442f5863-03b7-4fd2-807e-e956c0c52a5c"}
  [n]
  (loop [i 0
         v (transient [])]
    (if (< i n)
      (recur (inc i) (conj! v i))
      (persistent! v))))


(defn long-array-range
  "Returns a Java LongArray from zero to `n`. Call `seq` on return value to
  create a strightforwardly comparable, printable, etc. object."
  {:UUIDv4 #uuid "8b80c50d-9b81-4152-97cf-e6c9a6877eab"}
  [n]
  (let [an-array (long-array n)]
    (amap ^longs an-array idx ret (aset-long ^longs an-array idx idx))))


(defn vec-range
  "Returns a `clojure.core.Vec` from zero to `n`."
  {:UUIDv4 #uuid "bdb18a5f-9703-4ce0-8efa-528331516fe6"}
  [n]
  (apply vector-of :long (range n)))


(deftest method-tests
  (are [x] (= x (range 99))
    (long-range 99)
    (vector-range-1 99)
    (vector-range-2 99)
    (transducer-range 99)
    (transient-range 99)
    (vec-range 99)
    (seq (long-array-range 99))))


#_(run-tests)

