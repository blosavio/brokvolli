(ns brokvolli.core)


(def ^{:no-doc true} *keydex*-docstring
  "Dynamically bound to the 'current' key/index within a transducer stack
 composed with [[comp-kv]]. Do not manually re-bind.")


(def ^{:dynamic true
       :doc *keydex*-docstring}
  *keydex*)


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

