(ns brokvolli.multi
  "Multi-threaded variants of [`transduce`](https://clojure.github.io/clojure/clojure.core-api.html#clojure.core/transduce)
  and [[transduce-kv]]."
  (:refer-clojure :exclude [transduce])
  (:require
   [brokvolli.single :refer [transduce-kv]]
   [clojure.core.reducers :as r]
   [extended-extend-protocol.core :refer [multi-extend-protocol]]))


(def multi-threaded-capable #{map
                              cat
                              filter
                              mapcat
                              remove
                              take-while
                              drop-while
                              keep
                              #_distinct
                              #_dedupe
                              #_random-sample})


;; Considerations around using `concat`:
;; https://stuartsierra.com/2015/04/26/clojure-donts-concat/
;; https://groups.google.com/g/clojure-dev/c/ewBuyloeiFs


(defn concatv
  "Concatenates vectors `v1` and `v2`, efficiently and fastly.

  TODO: Objectively evaluate different implementations for speed/efficiency."
  {:UUIDv4 #uuid "571ca334-1e9d-4e72-926c-f154c47a8663"}
  ([] [])
  ([v] v)
  ([v1 v2]
   #_(vec (concat v1 v2)) ;; beware: `concat` is lazy, and returns a sequence
   #_(into v1 v2)         ;; compare to transducer variant (below)
   #_(r/cat v1 v2) ;; returns an instance of `clojure.core.reducers.Cat`
   (into v1 conj v2)))


(defn split-vector
  "Given vector `v`, returns a 2-ple of two subvectors."
  {:UUIDv4 #uuid "377dc05e-0195-4133-b05d-cdf4a6aa1e45"}
  [v]
  (let [split (quot (count v) 2)]
    [(subvec v 0 split) (subvec v split (count v))]))


(defn split-hashmap
  "Given hashmap `m`, returns a 2-ple of two sub-hashmaps."
  {:UUIDv4 #uuid "f2d0e8ce-b953-4d81-8c40-1ecc80efaca8"}
  [m]
  (let [ks (keys m)
        split (quot (count ks) 2)
        left-keys (take split ks)
        right-keys (drop split ks)]
    (mapv #(select-keys m %) [left-keys right-keys])))


(defn split-seq
  "Given sequence `s`, returns a 2-ple of two sub-sequences."
  {:UUIDv4 #uuid "b1a7033c-a13b-4244-82a7-53e866115525"}
  [s]
  (let [split (quot (count s) 2)]
    [(take split s) (drop split s)]))


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


(defn transduce-
  "Constructs a function to reduce a collection, possibly in parallel with
  multiple threads. `xduce-fn` is a transducing function, `splitter-fn` is a
  function that splits a specific type of collection, and `empty-pred` is a
  predicate that tests for emptiness.

  See [[split-vector]], [[split-hashmap]], and [[split-seq]] for example
  splitter functions.

  Most collections use `empty?` for the empty predicate, but use `seq` to test
  for empty sequences."
  {:UUIDv4 #uuid "3c8c1733-eb39-4c7f-b539-d744f3c65ea3"
   :no-doc true}
  [xduce-fn splitter-fn empty-pred]
  (fn tduce
    [n xform f init combine coll]
    (combine
     (cond
       (empty-pred coll) (combine)
       (<= (count coll) n) (xduce-fn xform f init coll)
       :else
       (let [[c1 c2] (splitter-fn coll)
             fc (fn [child] #(tduce n xform f init combine child))]
         (fjinvoke
          #(let [f1 (fc c1)
                 t2 (fjtask (fc c2))]
             (fjfork t2)
             (combine (f1) (fjjoin t2)))))))))


(defprotocol PTransduce
  (ptransduce [coll n xform f init combine])

  #_(ptransduce-kv [coll xform f init combine]))


(multi-extend-protocol
 PTransduce
 clojure.lang.PersistentVector
 (ptransduce [v n xform f init combine] ((transduce- clojure.core/transduce split-vector empty?) n xform f init combine v))

 clojure.lang.PersistentArrayMap
 clojure.lang.PersistentHashMap
 (ptransduce [m n xform f init combine] ((transduce- clojure.core/transduce split-hashmap empty?) n xform f init combine m))

 clojure.lang.PersistentList$EmptyList
 clojure.lang.LongRange
 clojure.lang.Range
 (ptransduce [s n xform f init combine] ((transduce- clojure.core/transduce split-seq #(not (seq %))) n xform f init combine s))

 nil
 (ptransduce [_ _ _ _ _ combine] (combine)))


(defn transduce
  "TODO...Like `clojure.core/transduce`, but potentially parallel.

  `combine` defaults to `f`, `n` defaults to 512.

  `xform` and `f` like `clojure.core/transduce`.
  `xform` is a composed transformer 'stack'.

  `f` has three arities:
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

