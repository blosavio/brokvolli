(ns brokvolli.experimental
  "Exploring implementations when not limited by internal `reduce` and core
  transducers.

  **Note:** The functions in this namespace are merely sketches. They are not
  complete, have not been tested well, nor edge cases considered. They make no
  commitment to updates nor corrections, and they may be deprecated or removed
  without further notice."
  (:require
   [brokvolli.multi :refer [split-vector]]))


(defn reduce-offset-index
  "Reduce seqeuntial `coll`, supplying `f` with an integer index shifted by
  integer `offset`. `f` must have two arities:
  * `(f)` supplies the initial value.
  * `(f acc idx el)` is the reduction arity, consuming the initial/accumulating
  value `acc`, the offset index `idx`, and the next element of `coll`.

  Note: `coll` must be sequential, but not necessarily the output.

  Implemented with the classic recursive `first`/`next` idiom, so performance is
  *O(n)*.

  Example, conjoining 2-tuples of incremented-index-offset-by-100 and element:
  ```clojure
  (reduce-offset-index 100
                       (fn
                         ([] [])
                         ([acc idx x] (conj acc (vector (inc idx) x))))
                       [11 22 33])
  ;; => [[101 11] [102 22] [103 33]]
  ```"
  {:UUIDv4 #uuid "5b43ff29-e2b0-4405-974c-b69fb6464196"}
  ([f coll] (reduce-offset-index 0 f coll))
  ([offset f coll]
   (loop [i offset
          acc (f)
          c coll]
     (if (seq c)
       (recur (inc i)
              (f acc i (first c))
              (next c))
       acc))))


(defn transduce-kv
  "Like `clojure.core/transduce`, but passes three arguments to the reducing
  functions: *result*, *key/index*, and *value*. `xform` and `f` must therefore
  support arity-3.

  An optional integer `offset` may be supplied (default 0), that will be added
  to the index before passing to the transducer stack.

  See [[reduce-offset-index]]."
  {:UUIDv4 #uuid "529256f2-ea96-45f6-bd18-e98c8b79814b"}
  ([xform f coll] (transduce-kv 0 xform f coll))
  ([offset xform f coll]
   (let [f (xform f)
         ret (reduce-offset-index offset f coll)]
     (f ret))))


;; The following section is sourced from the `clojure.core.reducers` namespace.
;; Would have preferred directly `require`-ing these fns, but they are `defn-`ed.

;; https://github.com/clojure/clojure/blob/master/src/clj/clojure/core/reducers.clj
;; lines 20--36
;; author Rich Hickey
;; Eclipse Public License 1.0

;; Copyright (c) Rich Hickey. All rights reserved.
;; The use and distribution terms for this software are covered by the
;; Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
;; which can be found in the file epl-v10.html at the root of this distribution.
;; By using this software in any fashion, you are agreeing to be bound by
;; the terms of this license.
;; You must not remove this notice, or any other, from this software.


(def ^:no-doc pool (delay (java.util.concurrent.ForkJoinPool.)))

(defn ^:no-doc fjtask [^Callable f]
  (java.util.concurrent.ForkJoinTask/adapt f))

(defn- fjinvoke [f]
  (if (java.util.concurrent.ForkJoinTask/inForkJoinPool)
    (f)
    (.invoke ^java.util.concurrent.ForkJoinPool @pool ^java.util.concurrent.ForkJoinTask (fjtask f))))

(defn- fjfork [task] (.fork ^java.util.concurrent.ForkJoinTask task))

(defn- fjjoin [task] (.join ^java.util.concurrent.ForkJoinTask task))

;; End of borrowed section


(defn multi-threaded-transduce-kv
  "Experimental, multi-threaded variant of [[transduce-kv]] that works only on
  vectors. See [[multi/transduce-kv]] for usage details."
  {:UUIDv4 #uuid "d6145286-b54f-4491-9dab-7c6b6806722a"}
  ([xform f combine coll]
   (multi-threaded-transduce-kv xform f (f) combine coll))
  ([xform f init combine coll]
   (multi-threaded-transduce-kv 512 xform f init combine coll))
  ([n xform f init combine coll]
   (multi-threaded-transduce-kv 0 n xform f init combine coll))
  ([offset n xform f init combine coll]
   (combine
    (cond
      (empty? coll) (combine)
      (<= (count coll) n) (transduce-kv offset xform f coll)
      :else
      (let [[c1 c2 split] (split-vector coll)
            fc (fn [child delta] #(multi-threaded-transduce-kv delta n xform f init combine child))]
        (fjinvoke
         #(let [f1 (fc c1 offset)
                t2 (fjtask (fc c2 (if offset (+ offset split) offset)))]
            (fjfork t2)
            (combine (f1) (fjjoin t2)))))))))


;; General principle: Everytime a transducer passes a result down the stack, it must also pass down the keydex, too.

;; Strategies for 'kv-izing' transducers:
;;   New arity-3, with most instances of `(rf result input)` expanded to `(rf result keydex input)`.
;;   Certain instances do not get expanded (?), such as in `interpose`.
;;   Instances of `(pred input)` expand to `(pred keydex input)`.
;;   Composed transducers, like `mapcat`, must use 'kv-ized' components.
;;   Helper functions, like `preserving-reduced` must be adapated to also handle functions of three args.

;; Instead of macro-ing away the arity-1/2/3 of these core transducers, for now, repeat them so that they're easier to eyeball check.


(defn map-kv
  "Returns a mapping transducer like `clojure.core/map`, but with an additional
  arity-3 of *result*, *keydex*, and *value*. The bottom-level reducing function
  must also handle those three args.

  Note: This additional arity subsumes `map`'s multi-collection variadic arity."
  {:UUIDv4 #uuid "f4c81f48-fa5c-4c95-b6b3-22c21877a54c"}
  [f]
  (fn [rf]
    (fn
      ([] (rf))
      ([result] (rf result))
      ([result input]
       (rf result (f input)))
      ([result keydex input]
       (rf result keydex (f keydex input))))))


(defn filter-kv
  "Returns a filtering transducer like `clojure.core/filter`, but with an
  additional arity-3 of *result*, *keydex*, and *value*. The bottom-level
  reducing function must also handle those three args.

  `pred` is a predicate of *keydex* and *value*."
  {:UUIDv4 #uuid "2a43a48b-1a84-4a47-9346-e3a8d341f318"}
  [pred]
  (fn [rf]
    (fn
      ([] (rf))
      ([result] (rf result))
      ([result input]
       (if (pred input)
         (rf result input)
         result))
      ([result keydex input]
       (if (pred keydex input)
         (rf result keydex input)
         result)))))


(defn replace-kv
  "Returns a replacing transducer like `clojure.core/replace`, but with an
  additional arity-3 of *result*, *keydex*, and *value*."
  {:UUIDv4 #uuid "d93028d6-c324-42c3-a144-5d68b623484b"}
  [smap]
  (map-kv (fn repl
            ([_ x] (repl x))
            ([x] (if-let [e (find smap x)]
                   (val e)
                   x)))))


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


(defn ^:private preserving-reduced
  [rf]
  (fn
    ([x y] (let [ret (rf x y)]
             (if (reduced? ret)
               (reduced ret)
               ret)))
    ([x y z] (let [ret (rf x y z)]
               (if (reduced? ret)
                 (reduced ret)
                 ret)))))


(defn cat-kv
  "Returns a concatenating transducer like `clojure.core/cat`, but with an
  additional arity-3 of *result*, *keydex*, and *value*."
  {:UUIDv4 #uuid "3097982a-c233-4616-8391-2e4f06d10653"}
  [rf]
  (let [rrf (preserving-reduced rf)]
    (fn ct
      ([] (rf))
      ([result] (rf result))
      ([result input]
       (reduce rrf result input))
      ([result keydex input]
       (reduce #(rrf %1 keydex %2) result input)))))


(defn mapcat-kv
  "Returns a mapcatting transducer like `clojure.core/mapcat`, but with an
  additional arity-3 of *result*, *keydex*, and *value*."
  {:UUIDv4 #uuid "7d837294-dda6-4579-b27c-9a67b23410c3"}
  [f]
  (comp (map-kv f) cat-kv))


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


(defn remove-kv
  "Returns a remove-ing transducer like `clojure.core/remove`, but
  with an additional arity-3 of *result*, *keydex*, and *value*."
  {:UUIDv4 #uuid "136792f2-c9d5-45be-ba7d-afc49cbaf91e"}
  [pred]
  (filter-kv (complement pred)))


;; skip `partition-by` and `partition-all`


(defn keep-kv
  "Returns a keeping transducer like `clojure.core/keep`, but with an
  additional arity-3 of *result*, *keydex*, and *value*."
  {:UUIDv4 #uuid "c8f980bb-af95-40e0-9f06-7883a5770518"}
  [f]
  (fn [rf]
    (fn
      ([] (rf))
      ([result] (rf result))
      ([result input]
       (let [v (f input)]
         (if (nil? v)
           result
           (rf result v))))
      ([result keydex input]
       (let [v (f keydex input)]
         (if (nil? v)
           result
           (rf result keydex v)))))))


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


(defn random-sample-kv
  "Returns a dedupe-ing transducer like `clojure.core/dedupe`, but with an
  additional arity-3 of *result*, *keydex*, and *value*."
  {:UUIDv4 #uuid "a8987e4f-eb0a-4ee4-af27-30e1b8504b50"}
  [prob]
  (filter-kv (fn [_ _] (< (rand) prob))))


(comment ;; not straightforwardly unit-testable
  (transduce-kv (random-sample-kv 0.5)
                  (fn
                    ([] [])
                    ([x] x)
                    ([x _ z] (conj x z)))
                  [11 22 33 44 55])
  )

