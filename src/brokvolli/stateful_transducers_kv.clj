(ns brokvolli.stateful-transducers-kv
  "Stateful, 'kv-ified' transducers. Not recommended for use with
  multi-threaded [[brokvolli.multi/transduce-kv]].

  Safe to use with single-threaded [[brokvolli.single/transduce-kv]].

  Each returns a transducer like their `clojure.core` namesakes, but with an
  additional arity-3 of *result*, *keydex*, and *value*. The bottom-level
  reducing function must also handle those three args.

  See also [[brokvolli.transducers-kv]].")


(defn take-kv
  "Retains the first `n` elements, similar to
  [`take`](https://clojure.github.io/clojure/clojure.core-api.html#clojure.core/take)
  .

  Example:
  ```clojure
  (transduce-kv (take-kv 3) tconj [11 22 33 44 55])
  ;; => [11 22 33]
  ```"
  {:UUIDv4 #uuid "efc87b4d-227f-4224-9342-d3e3203bd5fb"}
  [n]
  (fn [rf]
    (let [nv (volatile! n)]
      (fn tk
        ([] (rf))
        ([result] (rf result))
        ([result input]
         (let [n @nv
               nn (vswap! nv dec)
               result (if (pos? n)
                        (rf result input)
                        result)]
           (if (not (pos? nn))
             (ensure-reduced result)
             result)))
        ([result keydex input]
         (let [n @nv
               nn (vswap! nv dec)
               result (if (pos? n)
                        (rf result keydex input)
                        result)]
           (if (not (pos? nn))
             (ensure-reduced result)
             result)))))))


(defn take-while-kv
  "Retains elements while `(pred keydex element)` is truthy, similar to
  [`take-while`](https://clojure.github.io/clojure/clojure.core-api.html#clojure.core/take-while)
  .

  Example:
  ```clojure
  (transduce-kv (take-while-kv (fn [keydex x] (and (<= keydex 5)
                                                   (even? x))))
                tconj
                [11 22 33 44 55 66 77 88])
  ;; => [22 44 66]
  ```"
  {:UUIDv4 #uuid "f21b3d1f-fd72-45c0-8740-8a74023a5bfd"}
  [pred]
  (fn [rf]
    (fn
      ([] (rf))
      ([result] (rf result))
      ([result input]
       (if (pred input)
         (rf result input)
         (reduced result)))
      ([result keydex input]
       (if (pred keydex input)
         (rf result keydex input)
         (reduced result))))))


(defn take-nth-kv
  "Retains every `n`th element, similar to
  [`take-nth`](https://clojure.github.io/clojure/clojure.core-api.html#clojure.core/take-nth)
  .

  Example:
  ```clojure
  (transduce-kv (take-while-kv (fn [keydex x] (and (<= keydex 5)
                                                   (even? x))))
                tconj
                [11 22 33 44 55 66 77])
  ;; => [22 44 66]
  ```"
  [n]
  (fn [rf]
    (let [iv (volatile! -1)]
      (fn tknth
        ([] (rf))
        ([result] (rf result))
        ([result input]
         (let [i (vswap! iv inc)]
           (if (zero? (rem i n))
             (rf result input)
             result)))
        ([result keydex input]
         (let [i (vswap! iv inc)]
           (if (zero? (rem i n))
             (rf result keydex input)
             result)))))))


(defn drop-kv
  "Discards first `n` elements, similar to
  [`drop`](https://clojure.github.io/clojure/clojure.core-api.html#clojure.core/drop)
  .

  Example:
  ```clojure
  (transduce-kv (drop-kv 3) tconj [11 22 33 44 55])
  ;; => [44 55]
  ```"
  {:UUIDv4 #uuid "19a1de1d-af07-4042-b9c7-2acebd94074b"}
  [n]
  (fn [rf]
    (let [nv (volatile! n)]
      (fn drp
        ([] (rf))
        ([result] (rf result))
        ([result input]
         (let [n @nv]
           (vswap! nv dec)
           (if (pos? n)
             result
             (rf result input))))
        ([result keydex input]
         (let [n @nv]
           (vswap! nv dec)
           (if (pos? n)
             result
             (rf result keydex input))))))))


(defn drop-while-kv
  "Discards elements while `(pred keydex element)` returns truthy, similar to
  [`drop-while`](https://clojure.github.io/clojure/clojure.core-api.html#clojure.core/drop-while)
  .

  Example:
  ```clojure
  (transduce-kv (drop-while-kv (fn [keydex _] (<= keydex 2))) tconj [11 22 33 44 55])
  ;; => [44 55]
  ```"
  {:UUIDv4 #uuid "bc4da8d5-acc9-4dea-a343-cbd6c0382fd6"}
  [pred]
  (fn [rf]
    (let [dv (volatile! true)]
      (fn drpw
        ([] (rf))
        ([result] (rf result))
        ([result input]
         (let [drop? @dv]
           (if (and drop? (pred input))
             result
             (do
               (vreset! dv nil)
               (rf result input)))))
        ([result keydex input]
         (let [drop? @dv]
           (if (and drop? (pred keydex input))
             result
             (do
               (vreset! dv nil)
               (rf result keydex input)))))))))


(defn partition-by-kv
  "Splits each time `(f keydex element)` returns a new value, similar to
  [`partition-by`](https://clojure.github.io/clojure/clojure.core-api.html#clojure.core/partition-by)
  .

  Example:
  ```clojure
  (transduce-kv (partition-by-kv (fn [_ x] (even? x))) tconj [11 33 22 44 66 55 77 99 88])
  ;; => [[11 33] [22 44 66] [55 77 99] [88]]
  ```"
  {:UUIDv4 #uuid "fed3dc2e-9bf4-4bd4-9cfa-985d8aa2860e"}
  [f]
  (fn [rf]
    (let [a (java.util.ArrayList.)
          pv (volatile! ::none)]
      (fn
        ([] (rf))
        ([result]
         (let [result (if (.isEmpty a)
                        result
                        (let [v (vec (.toArray a))]
                          ;;clear first!
                          (.clear a)
                          (unreduced (rf result v))))]
           (rf result)))
        ([result input]
         (let [pval @pv
               val (f input)]
           (vreset! pv val)
           (if (or (identical? pval ::none)
                   (= val pval))
             (do
               (.add a input)
               result)
             (let [v (vec (.toArray a))]
               (.clear a)
               (let [ret (rf result v)]
                 (when-not (reduced? ret)
                   (.add a input))
                 ret)))))
        ([result keydex input]
         (let [pval @pv
               val (f keydex input)]
           (vreset! pv val)
           (if (or (identical? pval ::none)
                   (= val pval))
             (do
               (.add a input)
               result)
             (let [v (vec (.toArray a))]
               (.clear a)
               (let [ret (rf result keydex v)]
                 (when-not (reduced? ret)
                   (.add a input))
                 ret)))))))))


(defn partition-all-kv
  "Split into lists of `n` items each, may include fewer items than `n` at the
  end, similar to
  [`partition-all-kv`](https://clojure.github.io/clojure/clojure.core-api.html#clojure.core/partition-all)
  .

  Example:
  ```clojure
  (transduce-kv (partition-all-kv 3) tconj [11 22 33 44 55 66 77 88])
  ;; => [[11 22 33] [44 55 66] [77 88]]
  ```"
  {:UUIDv4 #uuid "911fa6bc-8102-4a05-a891-3fb21bf0fa07"}
  [^long n]
  (fn [rf]
    (let [a (java.util.ArrayList. n)]
      (fn
        ([] (rf))
        ([result]
         (let [result (if (.isEmpty a)
                        result
                        (let [v (vec (.toArray a))]
                          ;;clear first!
                          (.clear a)
                          (unreduced (rf result v))))]
           (rf result)))
        ([result input]
         (.add a input)
         (if (= n (.size a))
           (let [v (vec (.toArray a))]
             (.clear a)
             (rf result v))
           result))
        ([result keydex input]
         (.add a input)
         (if (= n (.size a))
           (let [v (vec (.toArray a))]
             (.clear a)
             (rf result keydex v))
           result))))))


(defn distinct-kv
  "Returns elements with duplicates removed, similar to
  [`distinct`](https://clojure.github.io/clojure/clojure.core-api.html#clojure.core/distinct)
  .

  Example:
  ```clojure
  (transduce-kv (distinct-kv) tconj [11 22 11 33 22 44 33 55 44])
  ;; => [11 22 33 44 55]
  ```"
  {:UUIDv4 #uuid "aa1df4b5-6712-4861-bf1b-48d0ccc7f6fc"}
  []
  (fn [rf]
    (let [seen (volatile! #{})]
      (fn
        ([] (rf))
        ([result] (rf result))
        ([result input]
         (if (contains? @seen input)
           result
           (do (vswap! seen conj input)
               (rf result input))))
        ([result keydex input]
         (if (contains? @seen input)
           result
           (do (vswap! seen conj input)
               (rf result keydex input))))))))


(defn interpose-kv
  "Returns elements separated by `sep`, similar to
  [`interpose`](https://clojure.github.io/clojure/clojure.core-api.html#clojure.core/interpose)
  .

  Example:
  ```clojure
  (transduce-kv (interpose-kv :foo) tconj [11 22 33])
  ;; => [11 :foo 22 :foo 33]
  ```"
  {:UUIDv4 #uuid "f619fb00-2a23-45db-a663-7ba6a94b5f5b"
   :note "Note: A bit iffy on 'kv-ized', arity-3 branch..."}
  [sep]
  (fn [rf]
    (let [started (volatile! false)]
      (fn intps
        ([] (rf))
        ([result] (rf result))
        ([result input]
         (if @started
           (let [sepr (rf result sep)]
             (if (reduced? sepr)
               sepr
               (rf sepr input)))
           (do
             (vreset! started true)
             (rf result input))))
        ([result keydex input]
         (if @started
           (let [sepr (rf result sep)]
             (if (reduced? sepr)
               sepr
               (rf sepr keydex input)))
           (do
             (vreset! started true)
             (rf result keydex input))))))))


(defn dedupe-kv
  "Removes consecutive duplicates, similar to
  [`dedupe`](https://clojure.github.io/clojure/clojure.core-api.html#clojure.core/dedupe)
  .

  Example:
  ```clojure
  (transduce-kv (dedupe-kv) tconj [11 11 22 22 22 33 33 33 33])
  ;; => [11 22 33]
  ```"
  {:UUIDv4 #uuid "62bac909-756d-4d1b-af31-e7c81fb149b2"}
  []
  (fn [rf]
    (let [pv (volatile! ::none)]
      (fn
        ([] (rf))
        ([result] (rf result))
        ([result input]
         (let [prior @pv]
           (vreset! pv input)
           (if (= prior input)
             result
             (rf result input))))
        ([result keydex input]
         (let [prior @pv]
           (vreset! pv input)
           (if (= prior input)
             result
             (rf result keydex input))))))))

