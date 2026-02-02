(ns brokvolli.core
  "Vars used from all namespaces, plus helper functions.")


(def ^{:no-doc true} *keydex*-docstring
  "Dynamically bound to the 'current' key/index within a transducer stack
 consumed with [[brokvolli.single/transduce-kv]] and
 [[brokvolli.multi/transduce-kv]]. Do not manually re-bind.")


(def ^{:dynamic true
       :doc *keydex*-docstring}
  *keydex*)


;; Considerations around using `concat`:
;; https://stuartsierra.com/2015/04/26/clojure-donts-concat/
;; https://groups.google.com/g/clojure-dev/c/ewBuyloeiFs


(defn kv-ize
  "Given a composition of functions `composition` (an xform stack) that only
  provides arities zero, one , and two, returns an equivalent transformer stack
  that also accepts *result*, *key/index*, and *value*, suitable for use with
  [[transduce-kv]] variants.

  The returned composition adds two capabilities.

  1. Diverts the key/index provided by `transduce-kv`, passing only the
  accumulated value and the next element to the outer/top transducer, and
  2. Establishes a binding context where the key/index is available by
  referencing [[*keydex*]] from any layer of the transducer stack."
  {:UUIDv4 #uuid "66698ff3-6e41-46ac-b63c-6d0f7d22cfb6"
   :no-doc true}
  [composition]
  (fn [rf]
    (let [g (composition rf)]
      (fn
        ([] (g))
        ([result] (g result))
        ([result input] (g result input))
        ([acc k v]
         (binding [brokvolli.core/*keydex* k]
           (g acc v)))))))


(defn concatv
  "Concatenates vectors `v1` and `v2` eagerly and efficiently.

  See [benchmarking and analysis](https://blosavio.github.io/brokvolli/concat_performance.html)."
  {:UUIDv4 #uuid "571ca334-1e9d-4e72-926c-f154c47a8663"}
  ([] [])
  ([v] v)
  ([v1 v2]
   (into v1 conj v2)))


(defn tconj
  "TODO: `tconj` docstring"
  {:UUIDv4 #uuid "040caf71-393f-4ccd-8fce-f794c7fba85d"}
  ([] [])
  ([x] x)
  ([x y] (conj x y))
  ([x _ z] (conj x z)))


(defn tassoc
  "TODO: `tassoc` docstring"
  {:UUIDv4 #uuid "0c734031-2406-492f-93c6-7689bc3d4e03"}
  ([] {})
  ([x] x)
  ([x y] (assoc x *keydex* y))
  ([x y z] (assoc x y z)))

