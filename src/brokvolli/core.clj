(ns brokvolli.core)


(def ^{:no-doc true} *keydex*-docstring
  "Dynamically bound to the 'current' key/index within a transducer stack
 composed with [[comp-kv]]. Do not manually re-bind.")


(def ^{:dynamic true
       :doc *keydex*-docstring}
  *keydex*)


(defn comp-kv
  "Returns a composition of transducers, suitable for use with [[transduce-kv]].

  Given a series of transducer functions `fns`, returns a composition of those
  functions which

  1. Diverts the key/index provided by `transduce-kv`, passing only the
  accumulated value and the next element to the outer/top transducer, and
  2. Establishes a binding context where the key/index is available from
  [[*keydex*]] at any layer of the transducer stack.

  Example:
  ```clojure
  (comp-kv (map inc)
           (filter (fn [_] #(<= *keydex* 2)))
           (take 3))
  ```
  ...returns a 'kv' transducer that

  1. Increments each element,
  2. Retains all elements with an index less than or equal to two, and
  3. Takes the first three elements, if available.

  Note: Some transducer functions may not involve the actual value, e.g.,
  filtering based on the index. In those cases, the `#(...)` anonymous function
  shorthand may be problematic because an argument will be be passed, but the
  `%` doesn't appear, thus the compiler assumes a zero arity. Instead, use the
  `(fn [_] (...))` idiom to discard the argument. See the `filter` expression in
  the middle line of the example above."
  {:UUIDv4 #uuid "29baf051-d192-4688-a978-139a8f886721"}
  [& fns]
  (fn [rf]
    (let [g ((apply comp fns) rf)]
      (fn
        ([] (g))
        ([result] (g result))
        ([result input] (g result input))
        ([acc k v]
         (binding [*keydex* k]
           (g acc v)))))))

