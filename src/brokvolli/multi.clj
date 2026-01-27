(ns brokvolli.multi
  "Multi-threaded variants of [`transduce`](https://clojure.github.io/clojure/clojure.core-api.html#clojure.core/transduce)
  and [[transduce-kv]]."
  (:refer-clojure :exclude [transduce])
  (:require
   [brokvolli.core]
   [brokvolli.single :as single]
   [clojure.core.reducers :as r]
   [extended-extend-protocol.core :refer [multi-extend-protocol]]))


(defn split-vector
  "Given vector `v`, returns a 3-ple of two subvectors and the split index."
  {:UUIDv4 #uuid "377dc05e-0195-4133-b05d-cdf4a6aa1e45"}
  [v]
  (let [split (quot (count v) 2)]
    [(subvec v 0 split) (subvec v split (count v)) split]))


(defn split-hashmap
  "Given hashmap `m`, returns a 3-ple of two sub-hashmaps and `nil`."
  {:UUIDv4 #uuid "f2d0e8ce-b953-4d81-8c40-1ecc80efaca8"}
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
  {:UUIDv4 #uuid "b1a7033c-a13b-4244-82a7-53e866115525"}
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


(def pool (delay (java.util.concurrent.ForkJoinPool.)))

(defn fjtask [^Callable f]
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
    [n xform f init combine coll]
    (combine
     (cond
       (empty-pred coll) (combine)
       (<= (count coll) n) (clojure.core/transduce xform f init coll)
       :else
       (let [[c1 c2 _] (splitter-fn coll)
             fc (fn [child] #(tduce n xform f init combine child))]
         (fjinvoke
          #(let [f1 (fc c1)
                 t2 (fjtask (fc c2))]
             (fjfork t2)
             (combine (f1) (fjjoin t2)))))))))


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
    [n xform f init combine coll offset]
    (combine
     (cond
       (empty-pred coll) (combine)
       (<= (count coll) n) (brokvolli.single/transduce-kv xform f init coll offset)
       :else
       (let [[c1 c2 split] (splitter-fn coll)
             fc (fn [child delta] #(tduce n xform f init combine child delta))]
         (fjinvoke
          #(let [f1 (fc c1 offset)
                 t2 (fjtask (fc c2 (if offset (+ offset split) offset)))]
             (fjfork t2)
             (combine (f1) (fjjoin t2)))))))))


(defprotocol PTransduce
  (ptransduce [coll n xform f init combine])

  (ptransduce-kv [coll n xform f init combine]))


(multi-extend-protocol
 PTransduce
 clojure.lang.PersistentVector
 (ptransduce [v n xform f init combine] ((transduce- split-vector empty?) n xform f init combine v))
 (ptransduce-kv [v n xform f init combine] ((transduce-kv- split-vector empty?) n xform f init combine v 0))

 clojure.lang.PersistentArrayMap
 clojure.lang.PersistentHashMap
 clojure.lang.PersistentTreeMap
 (ptransduce [m n xform f init combine] ((transduce- split-hashmap empty?) n xform f init combine m))
 (ptransduce-kv [m n xform f init combine] ((transduce-kv- split-hashmap empty?) n xform f init combine m nil))

 clojure.lang.PersistentList$EmptyList
 clojure.lang.LongRange
 clojure.lang.Range
 (ptransduce [s n xform f init combine] ((transduce- split-seq #(not (seq %))) n xform f init combine s))

 nil
 (ptransduce [_ _ _ _ _ combine] (combine))
 (ptransduce-kv [_ _ _ _ _ combine] (combine)))


(defn transduce
  "TODO: docstring expansion/edits.
  Like `clojure.core/transduce`, but potentially parallel.

  `combine` defaults to `f`, `n` defaults to 512.

  `xform` and `f` like `clojure.core/transduce`.
  `xform` is a composed transformer 'stack'.

  `xform` has three arities:
  1. zero args provides the 'init' value of the sub-reduction
  2. one arg provides the 'completing' action on the sub-collection
  3. two args is the step function, applying the function to the accumulating
  value and the next element.

  `combine` is a function of three arities that governs how to gather the
  results of the sub-reductions.
  1. zero args provides the output if `coll` is emtpy.
  2. one arg is the 'completing' action applied just before the final value
  3. two args combines the 'left' and 'right' sub-reduction results"
  {:UUIDv4 #uuid "6ec6c582-e6f2-4320-ac0d-4fb69e3b6c0e"}
  ([xform f coll] (transduce xform f f coll))
  ([xform f combine coll] (transduce 512 xform f combine coll))
  ([n xform f combine coll] (ptransduce coll n xform f (f) combine)))


(defn transduce-kv
  "Multi-threaded variant of [[brokvolli.single/transduce-kv]]."
  {:UUIDv4 #uuid "e9a2bb27-5d14-48e7-9bd9-ffa1f2ffb7d9"}
  ([xform f coll] (transduce-kv xform f f coll))
  ([xform f combine coll] (transduce-kv 512 xform f combine coll))
  ([n xform f combine coll] (ptransduce-kv coll n xform f (f) combine)))

