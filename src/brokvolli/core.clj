(ns brokvolli.core
  "Vars used from other namespaces, plus helper functions.")


(def ^{:no-doc true} *keydex*-docstring
  "Within a transducer stack modified by [[kv-ize]], dynamically bound to the
 \"current\" key/index while being invoked with [[single/transduce-kv]] and
 [[multi/transduce-kv]]. Do not manually re-bind.")


(def ^{:dynamic true
       :doc *keydex*-docstring}
  *keydex*)


(defn kv-ize
  "Given a `composition` of transducing functions (a transducing stack) that
  only provides arities zero, one, and two, returns an equivalent transducer
  stack that also accepts *result*, *key/index*, and *value*, suitable for use
  with both `transduce-kv` variants, [[single/transduce-kv]] and
  [[multi/transduce-kv]].

  Intended for exploratory work or when the transducer stack is pre-composed.
  Otherwise, prefer composing transducer stacks with
  [[brokvolli.transducers-kv]] and [[brokvolli.stateful-transducers-kv]].

  The returned composition provides two additional capabilities.

  1. Diverts the key/index provided by `transduce-kv`, passing only the
  accumulated value and the next element to the outer/top transducer, and
  2. Establishes a binding context where the key/index is available by
  referencing [[*keydex*]] from any layer of the transducer stack.

  Example, a transducer stack, out of our control:
  ```clojure
  (def xf-from-somewhere (comp (map inc)
                               (filter even?)
                               (take 3)))

  (transduce-kv (kv-ize xf-from-somewhere) conj [11 22 33 44 55 66 77 88 99])
  ;; => [12 34 56]
  ```

  Example, using `clojure.core/filter` instead of [[filter-kv]], key/index
  accessible through dynamic var [[*keydex*]]:
  ```clojure
  (transduce-kv (kv-ize (filter (fn [_] (<= *keydex* 2)))) conj [11 22 33 44 55])
  ;; => [11 22 33]
  ```"
  {:UUIDv4 #uuid "66698ff3-6e41-46ac-b63c-6d0f7d22cfb6"}
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


;; Considerations around using `concat`:
;; https://stuartsierra.com/2015/04/26/clojure-donts-concat/
;; https://groups.google.com/g/clojure-dev/c/ewBuyloeiFs


(defn concatv
  "Concatenates vectors `v1` and `v2` eagerly and efficiently.

  See [benchmarking and analysis](https://blosavio.github.io/brokvolli/concat_performance.html).

  Examples:
  ```
  (concatv) ;; => []
  (concatv [11 22 33]) ;; => [11 22 33]
  (concatv [11 22 33] [44 55 66]) ;; => [11 22 33 44 55 66]
  ```"
  {:UUIDv4 #uuid "571ca334-1e9d-4e72-926c-f154c47a8663"}
  ([] [])
  ([v] v)
  ([v1 v2]
   (into v1 conj v2)))


(defn tconj
  "A [conj](https://clojure.github.io/clojure/clojure.core-api.html#clojure.core/conj)
  variant, useful as the reducing function in a `transduce` operation.

  * With no args, returns `[]`.
  * With one arg, returns arg.
  * With two args, conjoins `y` onto `x`.
  * With three args, conjoins `z` onto `x`, ignoring the second arg (which is
  typically the key/index).

  See also [[tassoc]] for a similar utility used in transducing over associative
  collections.

  Example:
  ```clojure
  (transduce-kv (map-kv (fn [keydex x] [keydex x])) tconj [11 22 33])
  ;; => [[0 11] [1 22] [2 33]]
  ```"
  {:UUIDv4 #uuid "040caf71-393f-4ccd-8fce-f794c7fba85d"}
  ([] [])
  ([x] x)
  ([x y] (conj x y))
  ([x _ z] (conj x z)))


(defn tassoc
  "An [assoc](https://clojure.github.io/clojure/clojure.core-api.html#clojure.core/assoc)
  variant, useful as the reducing function in a `transduce` operation.

  * With no args, returns `{}`.
  * With one arg, returns arg.
  * With two args, associates `y` to [[*keydex*]] within `x`.
  * With three args, associates `y` to `z` within `x`.

  See also [[tconj]] for a similar utility used in transducing over sequential
  collections.

  Example:
  ```clojure
  (transduce-kv (map-kv (fn [keydex x] {:keydex (inc keydex) :value x})) tassoc [11 22 33])
  ;; => {0 {:keydex 1, :value 11}
  ;;     1 {:keydex 2, :value 22}
  ;;     2 {:keydex 3, :value 33}}
  ```"
  {:UUIDv4 #uuid "0c734031-2406-492f-93c6-7689bc3d4e03"}
  ([] {})
  ([x] x)
  ([x y] (assoc x *keydex* y))
  ([x y z] (assoc x y z)))

