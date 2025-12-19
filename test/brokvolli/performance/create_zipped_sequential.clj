(ns brokvolli.performance.create-zipped-sequential
  "Exploring tactics to 'zip' two vectors. Each element of the input sequential
  becomes the value, while and index is prepending, forming a 2-ple for each
  element."
  (:require
   [clojure.test :refer [are
                         deftest
                         is
                         run-test
                         run-tests
                         testing]]))


(defn mapv-zip
  "Naive, base case using `mapv`."
  {:UUIDv4 #uuid "934b494a-f7fa-4711-bf44-058f1d8524fb"}
  [v]
  (mapv #(vector %1 %2) (range) v))


(defn map-entry
  "Given key `k` and value `v`, returns a MapEntry."
  {:UUIDv4 #uuid "d8cc8298-9e42-443b-bb3b-e0b135582a27"}
  [k v]
  (clojure.lang.MapEntry. k v))


(defn mapv-entry-zip
  "Similar to `mapv-zip`, but uses a MapEntry instead of a plain persistent
  vector."
  {:UUIDv4 #uuid "58253d05-320c-4109-9d1d-6c2e21403ac1"}
  [v]
  (mapv #(map-entry %1 %2) (range) v))


(defn pmap-zip
  "Similar to `mapv-zip`, but uses `pmap`."
  {:UUIDv4 #uuid "3a1f73ec-6a54-4c5b-a914-4d23026d019e"}
  [v]
  (pmap #(map-entry %1 %2) (range) v))


(defn long-range-zip
  "Uses a primitive vector to supply the indexes."
  {:UUIDv4 #uuid "3fc3e6b4-525d-4c28-87d4-d886fd2b4325"}
  [v]
  (mapv #(map-entry %1 %2)
        (apply vector-of :long (range (count v)))
        v))


(defn long-array-zip
  "Indexes in a Java array of longs."
  {:UUIDv4 #uuid "0226ce82-98f8-4f14-b94e-cc75634af212"}
  [v]
  (mapv #(map-entry ^long %1 %2)
        (long-array (range (count v)))
        v))


(defn map-indexed-zip
  "Uses plain `map-indexed`."
  {:UUIDv4 #uuid "71b5faee-e304-41d9-b16d-960c2132d8ac"}
  [v]
  (map-indexed #(map-entry %1 %2) v))


(defn transduce-zip
  "Transduces using `map-indexed`."
  {:UUIDv4 #uuid "3af88032-8879-415f-a535-e7dbc4faa40d"}
  [v]
  (transduce (map-indexed #(map-entry %1 %2)) conj v))


(defn transient-loop-zip
  "Conj-ing onto a transient vector in a `loop`. Avoids creating a second
  sequential for the indexes."
  {:UUIDv4 #uuid "92dd0dd1-5180-44d8-ba4d-3b2a40945b73"}
  [v]
  (let [stop (count v)
        f nth]
    (loop [i 0
           tv (transient [])]
      (if (< i stop)
        (recur (inc i) (conj! tv (map-entry i (.nth v i))))
        (persistent! tv)))))

;; `get` versus `nth` versus `.nth`
;; Obs: `clojure.lang.APersistentVector/get` (line 180) delegates to `.nth`
;;      `clojure.lang.PersistentVector/nth` has efficient lookup.
;;      `clojure.lang.PersistentVector$TransientVector/nth` also on line 852.
;; Conc: `.nth` on a known vector requires fewest chaining.


(defn transient-first-next-zip
  "Conj-ing onto a transient vector using recurive `first`/`next` idiom. Avoids
  creating a second sequential for the indexes."
  {:UUIDv4 #uuid "5b62d14e-a29a-4327-9db4-20f426e86b1f"}
  [v]
  (loop [v1 v
         i 0
         v2 (transient [])]
    (if (seq v1)
      (recur (next v1) (inc i) (conj! v2 [i (first v1)]))
      (persistent! v2))))


(deftest method-tests
  (are [f] (= (f [1 2 3])
              (mapv-zip [1 2 3]))
    mapv-zip
    mapv-entry-zip
    pmap-zip
    long-range-zip
    long-array-zip
    map-indexed-zip
    transduce-zip
    transient-loop-zip
    transient-first-next-zip))


#_(run-tests)

