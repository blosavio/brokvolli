(ns brokvolli.protocols
  "Demonstrate creating a protocol to extend `transduce-kv` to other collection
  types.

  1. Create a new protocol with a collection-first signature.
  2. Extend protocol to another collection type, supplying the implementation.
  3. Define a new function that invokes that implementation."
  (:require
   [brokvolli.core :refer [*keydex*]]
   [brokvolli.single :refer [transduce-kv]]))


(defprotocol KVTransduce
  (kv-transduce [coll xform f init]))


(extend-protocol KVTransduce
  clojure.lang.PersistentList
  (kv-transduce [coll xform f init] (transduce-kv xform f init (vec coll))))


(defn list-transduce-kv
  "Transduces with key/value over a `coll`, a list.

  See [[brokvolli.single/transduce-kv]]."
  {:UUIDv4 #uuid "2f945342-2585-414f-a193-ca690bdb1204"}
  ([xform f coll] (list-transduce-kv xform f (f) coll))
  ([xform f init coll] (kv-transduce coll xform f init)))


(comment
  (list-transduce-kv (comp (map inc)) conj (list 11 22 33 44 55))
  ;; [12 23 34 45 56]

  (list-transduce-kv (comp (map inc)
                           (filter even?)) conj (list 11 22 33 44 55))
  ;; [12 34 56]
  )

