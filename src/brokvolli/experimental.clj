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


(defn reduce-offset-kv
  "Like `clojure.core/reduce-kv`, but adds integer `offset` (default `nil`) to
  the index passed as the second argument to reducing function `f`.

  If `offset` is `nil`, the key/index is passed to `f` unchanged."
  {:UUIDv4 #uuid "8da62ab4-7900-449a-8ba4-b827bb2686d8"
   :no-doc true}
  ([f init coll] (reduce-offset-kv nil f init coll))
  ([offset f init coll]
   (let [f-off (fn [acc keydex x] (f acc (if offset (+ offset keydex) keydex) x))]
     (reduce-kv f-off init coll))))


(comment
  (reduce-offset-kv #(conj %1 [%2 %3]) [] [11 22 33 44 55])
  (reduce-offset-kv 900 #(conj %1 [%2 %3]) [] [11 22 33 44 55])
  )


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

