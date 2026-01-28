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


(defn concatv
  "Concatenates vectors `v1` and `v2` eagerly and efficiently.

  See [benchmarking and analysis](https://blosavio.github.io/brokvolli/concat_performance.html)."
  {:UUIDv4 #uuid "571ca334-1e9d-4e72-926c-f154c47a8663"}
  ([] [])
  ([v] v)
  ([v1 v2]
   (into v1 conj v2)))

