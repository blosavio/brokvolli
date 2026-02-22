(ns brokvolli.multi
  "Multi-threaded variants of [`transduce`](https://clojure.github.io/clojure/clojure.core-api.html#clojure.core/transduce)
  and [[brokvolli.single/transduce-kv]].

  **Warning:** Use stateful transducers only with extreme caution."
  (:refer-clojure :exclude [transduce])
  (:require
   [brokvolli.core]
   [brokvolli.single :as single]
   [extended-extend-protocol.core :refer [multi-extend-protocol]]))


(defn split-vector
  "Given vector `v`, returns a 3-ple of two subvectors and the split index."
  {:UUIDv4 #uuid "377dc05e-0195-4133-b05d-cdf4a6aa1e45"
   :no-doc true}
  [v]
  (let [split (quot (count v) 2)]
    [(subvec v 0 split) (subvec v split (count v)) split]))


(defn split-hashmap
  "Given hashmap `m`, returns a 3-ple of two sub-hashmaps and `nil`."
  {:UUIDv4 #uuid "f2d0e8ce-b953-4d81-8c40-1ecc80efaca8"
   :no-doc true}
  [m]
  (let [ks (keys m)
        split (quot (count ks) 2)
        left-keys (take split ks)
        right-keys (drop split ks)]
    (-> (mapv #(select-keys m %) [left-keys right-keys])
        (conj nil))))


(defn split-seq
  "Given sequence `s`, returns a 3-ple of two sub-sequences and the split
  index."
  {:UUIDv4 #uuid "b1a7033c-a13b-4244-82a7-53e866115525"
   :no-doc true}
  [s]
  (let [split (quot (count s) 2)]
    [(take split s) (drop split s) split]))


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


;;;; Java Fork/Join framework

;; https://docs.oracle.com/javase/8/docs/api/java/util/concurrent/ForkJoinPool.html
;; https://docs.oracle.com/javase/8/docs/api/java/util/concurrent/ForkJoinTask.html
;; https://gee.cs.oswego.edu/dl/papers/fj.pdf
;; https://gee.cs.oswego.edu/dl/concurrency-interest/index.html
;; https://homes.cs.washington.edu/~djg/teachingMaterials/grossmanSPAC_forkJoinFramework.html#useful


;; ForkJoinTask = a light-weight thread-like entity
;; begins execution when submitted to a ForkJoinPool with fork() or invoke()
;; typically involved creating other subtasks
;; restrict to computational tasks (e.g., evaluating pure functions)
;; avoid blocking, cyclic references
;; fork() arranges async execution
;; join() awaits computed result
;; joins should be  performed innermost-first
;; a.fork();
;;   b.fork();
;;   b.join();
;; a.join();
;; split large tasks into small subtasks (100--10000 basic computations)
;; adapt() method invokes a supplied Runnable/Callable object

;; ForkJoinPool = service for running ForkJoinTasks, main entry point
;; employs 'work-stealing' pattern
;; commonPool() is most appropriate (i.e., most efficient, parallelism = num procs)
;; from external    from fork/join
;; invoke = await and obtain result
;; only sequential code should call invoke(), not from within


;; Clojure Fork/Join libs
;; https://github.com/rm-hull/task-scheduler
;; https://github.com/ane/task
;; https://gist.github.com/opqdonut/9e95c4cbc19b0c927b8530428485bfe3


(defn transduce-
  "Constructs a function to reduce a collection, possibly in parallel with
  multiple threads. `splitter-fn` is a function that splits a specific type of
  collection, and `empty-pred` is a predicate that tests for emptiness.

  See [[split-vector]], [[split-hashmap]], and [[split-seq]] for example
  splitter functions.

  Most collections use `empty?` for the empty predicate, but use `seq` to test
  for empty sequences."
  {:UUIDv4 #uuid "3c8c1733-eb39-4c7f-b539-d744f3c65ea3"
   :no-doc true}
  [splitter-fn empty-pred]
  (fn tduce
    [n combine xform f init coll]
    (combine
     (cond
       (empty-pred coll) (combine)
       (<= (count coll) n) (clojure.core/transduce xform f init coll)
       :else
       (let [[c1 c2 _] (splitter-fn coll)
             fc (fn [child] #(tduce n combine xform f init child))]
         (fjinvoke
          #(let [f1 (fc c1)
                 t2 (fjtask (fc c2))]
             (fjfork t2)
             (combine (f1) (fjjoin t2)))))))))


;; `transduce-` above and `transduce-kv-` below share quite a bit of structure,
;; but parametrizing their difference would obscure the mechanics. For now,
;; let's tolerate this repetition.


(defn transduce-kv-
  "Constructs a function to reduce a collection, possibly in parallel with
  multiple threads. `xduce-fn` is a transducing function, `splitter-fn` is a
  function that splits a specific type of collection, and `empty-pred` is a
  predicate that tests for emptiness.

  See [[split-vector]], [[split-hashmap]], and [[split-seq]] for example
  splitter functions.

  Most collections use `empty?` for the empty predicate, but use `seq` to test
  for empty sequences."
  {:UUIDv4 #uuid "e093edf7-f800-4545-b2ba-3ba643f4ab47"
   :no-doc true}
  [splitter-fn empty-pred]
  (fn tduce
    [offset n combine xform f init coll]
    (combine
     (cond
       (empty-pred coll) (combine)
       (<= (count coll) n) (brokvolli.single/transduce-kv* offset xform f init coll)
       :else
       (let [[c1 c2 split] (splitter-fn coll)
             fc (fn [child delta] #(tduce delta n combine xform f init child))]
         (fjinvoke
          #(let [f1 (fc c1 offset)
                 t2 (fjtask (fc c2 (if offset (+ offset split) offset)))]
             (fjfork t2)
             (combine (f1) (fjjoin t2)))))))))


(defprotocol ^:no-doc PTransduce
  (ptransduce [coll n combine xform f init])

  (ptransduce-kv [coll n combine xform f init]))


(multi-extend-protocol
 PTransduce
 clojure.lang.PersistentVector
 (ptransduce [v n combine xform f init] ((transduce- split-vector empty?) n combine xform f init v))
 (ptransduce-kv [v n combine xform f init] ((transduce-kv- split-vector empty?) 0 n combine xform f init v))

 clojure.lang.PersistentArrayMap
 clojure.lang.PersistentHashMap
 clojure.lang.PersistentTreeMap
 (ptransduce [m n combine xform f init] ((transduce- split-hashmap empty?) n combine xform f init m))
 (ptransduce-kv [m n combine xform f init] ((transduce-kv- split-hashmap empty?) nil n combine xform f init m))

 clojure.lang.ArraySeq
 clojure.lang.Cycle
 clojure.lang.LongRange
 clojure.lang.PersistentList
 clojure.lang.PersistentList$EmptyList
 clojure.lang.Range
 clojure.lang.Repeat
 clojure.lang.StringSeq
 (ptransduce [s n combine xform f init] ((transduce- split-seq #(not (seq %))) n combine xform f init s))

 nil
 (ptransduce [_ _ combine _ _ _] (combine))
 (ptransduce-kv [_ _ combine _ _ _] (combine)))


(defn transduce
  "Like `clojure.core/transduce`, but potentially multi-threaded. `xform` is a
  composed transducer 'stack' and `f` is a reducing function, each analogous to
  their [`clojure.core/transduce`](https://clojure.github.io/clojure/clojure.core-api.html#clojure.core/transduce)
  counterparts. This variant of `transduce` does not accept an explicit 'init'
  parameter; it must be provided by the arity-0 of `f`. Returns `(combine)` when
  `coll` is empty or `nil`.

  `xform` has three arities:

  1. Zero args provides the 'init' value of the sub-reduction.
  2. One arg provides the 'completing' action on the sub-collection.
  3. Two args is the step function, applying the function to the accumulating
  value and the next element.

  **Warning:** Use stateful transducers only with extreme caution.

  `combine` is a function of three arities that governs how to gather the
  results of the sub-reductions:

  1. Zero args provides the output if `coll` is empty or `nil`.
  2. One arg is the 'completing' action applied just before the final value.
  3. Two args combines the 'left' and 'right' sub-reduction results.

  `combine` defaults to `f`.

  `coll` must implement `clojure.lang.IReduceInit`. `n` is size at which `coll`
  is partitioned for multi-threaded processing, defaulting to 512. When `n` does
  not divide evenly into `(size coll)`, the locations of the partition
  boundaries are an implementation detail.

  Example, `f` also provides `combine`:
  ```clojure
  (transduce (map identity) + [1 2 3]) ;; => 6
  ```

  Example with explicit partitioning,

    1. Partition `coll` into partitions with max of two elements,
    2. Compute (increment) on different threads, and
    3. Combine (concatenate):
  ```clojure
  (require '[brokvolli.core :refer [concatv]])

  (transduce 2 concatv (map inc) conj [11 22 33]) ;; => [12 23 34]
  ```
  See [[concatv]] for a helper function that concatenates eagerly and
  efficiently.

  Example, 'stack of xforms':
  ```clojure
  (transduce 3
             concatv
             (comp (map inc)
                   (filter even?)
                   (remove #(< 70 %)))
             conj
             [11 22 33 44 55 66 77 88])
  ;; => [12 34 56]
  ```

  Like [clojure.core/reduce](https://clojure.github.io/clojure/clojure.core-api.html#clojure.core/reduce),
  elements of hash-maps are peeled off as *map entries*, 2-tuples of *key* and
  *value*.

  Example, working with an associative collection (i.e., a hash-map):
  ```clojure
  (transduce merge
             (comp (map #(update % 1 inc))
                   (filter #(even? (second %)))
                   (remove #(< 70 (second %))))
             (completing
              (fn
                ([] {})
                ([result value] (conj result value))))
             {:a 11 :b 22 :c 33 :d 44 :e 55 :f 66 :g 77 :h 88 :i 99})
  ;; => {:e 56, :c 34, :a 12}
  ```
  See [[transduce-kv]] for a `transduce` variant that behaves like
  [reduce-kv](https://clojure.github.io/clojure/clojure.core-api.html#clojure.core/reduce-kv)."
  {:UUIDv4 #uuid "6ec6c582-e6f2-4320-ac0d-4fb69e3b6c0e"}
  ([xform f coll] (transduce f xform f coll))
  ([combine xform f coll] (transduce 512 combine xform f coll))
  ([n combine xform f coll] (ptransduce coll n combine xform f (f))))


(defn transduce-kv
  "Multi-threaded variant of [[single/transduce-kv]]. `xform`, `f`, and
  `combine` are the similar to [[transduce]]. `xform` is a transducer stack,
  `f` is a reducing function with an additional arity three which consumes the
  accumulated value, the key/index, and the next element of `coll`.

  As with [[single/transduce-kv]], when used with a transducer stack adapted
  with [[kv-ize]], the key/index is available as [[*keydex*]] at any \"layer\"
  within the transducer stack; it may be ignored to suit.

  `coll` must implement `clojure.lang.IKVReduce`.

  `n` is size at which `coll` is partitioned for multi-threaded processing,
  defaulting to 512. When `n` does not divide evenly into `(size coll)`, the
  locations of the partition boundaries are an implementation detail.

  **Warning:** Use stateful transducers only with extreme caution.

  Examples:
  ```clojure
  (require '[brokvolli.core :refer [concatv tconj]]
           '[brokvolli.transducers-kv :refer [map-kv]])

  (transduce-kv (map-kv (fn [keydex x] (+ x (inc keydex)))) tconj [11 22 33])
  ;; => [12 24 36]

  ;; same result, but with explicit splitting into threads (also, must use a dedicated combining function, e.g., `concatv`)
  (transduce-kv 2 concatv (map-kv (fn [keydex x] (+ x (inc keydex)))) tconj [11 22 33])
  ;; => [12 24 36]

  ;; value  keydex  (+ value (inc keydex))  eval  result
  ;; 11     0       (+ 11 (inc 0))          12    [12]
  ;; 22     1       (+ 22 (inc 1))          24    [12 24]
  ;; 33     2       (+ 33 (inc 2))          36    [12 24 36]
  ```

  See also [[single/transduce-kv]] and [[multi/transduce]]."
  {:UUIDv4 #uuid "e9a2bb27-5d14-48e7-9bd9-ffa1f2ffb7d9"}
  ([xform f coll] (transduce-kv f xform f coll))
  ([combine xform f coll] (transduce-kv 512 combine xform f coll))
  ([n combine xform f coll] (ptransduce-kv coll n combine xform f (f))))

