(ns brokvolli.stateful-transducers-kv
  "Statefull, 'kv-ified' transducers. Not recommended for use with
  multi-threaded [[brokvolli.multi/transduce-kv]].

  Safe to use with single-threaded [[brokvolli.single/transduce-kv]].

  See also [[brokvolli.transducers-kv]].")


(defn take-kv
  "Returns a stateful take-ing transducer like `clojure.core/take`, but with an
  additional arity-3 of *result*, *keydex*, and *value*."
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
  "Returns a taking-while transducer like `clojure.core/take-while`, but with an
  additional arity-3 of *result*, *keydex*, and *value*."
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
         result)))))


(defn take-nth-kv
  "Returns a stateful take-ing transducer like `clojure.core/take-nth`, but with
  an additional arity-3 of *result*, *keydex*, and *value*."
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
  "Returns a stateful dropping transducer like `clojure.core/drop`, but with
  an additional arity-3 of *result*, *keydex*, and *value*."
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
  "Returns a stateful dropping transducer like `clojure.core/drop-while`, but
  with an additional arity-3 of *result*, *keydex*, and *value*."
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
  "..."
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
  "..."
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
  "Returns a stateful distinct-ing transducer like `clojure.core/distinct`, but
  with an additional arity-3 of *result*, *keydex*, and *value*."
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
  "Returns a stateful interpose-ing transducer like `clojure.core/interpose`,
  but with an additional arity-3 of *result*, *keydex*, and *value*.

  Note: A bit iffy on 'kv-ized', arity-3 branch..."
  {:UUIDv4 #uuid "f619fb00-2a23-45db-a663-7ba6a94b5f5b"}
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
  "Returns a stateful dedupe-ing transducer like `clojure.core/dedupe`, but with
  an additional arity-3 of *result*, *keydex*, and *value*."
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

