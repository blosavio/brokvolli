(ns brokvolli.single
  "Provides a single-threaded 'kv' variant of `clojure.core/transduce`,
  analogous to the way `reduce-kv` relates to `reduce`.")


(defn transduce-kv
  "A '-kv' variant of `transduce`, reducing with a transformation. The analogy
  is `reduce`:`reduce-kv`::`transduce`:`transduce-kv`.

  As with `transduce`, `xform` and `f` are functions of zero, one, and two
  arguments. Works with all the `clojure.core` transducers, excepting the
  `...-indexed` variants.

  `xform` is most straightforwardly composed with [[comp-kv]], a utility that
  composes a series of transducer functions and automatically adds an additional
  arity that accepts the accumulating value, the key/index, and the next
  element. While the transducer stack is walked, similarly to `transduce`,
  `comp-kv` makes the key/index available via [[*keydex*]].

  `coll` must implement `clojure.lang.IKVReduce`.

  Example, not using the key/index:
  ```clojure
  (transduce-kv (comp-kv (map inc)) conj [11 22 33 44 55 66 77])
  ;; => [12 23 34 45 56 67 78]

  (transduce-kv (comp-kv (map inc)
                         (filter even?)) conj [11 22 33 44 55 66 77])
  ;; => [12 34 56 78]

  (transduce-kv (comp-kv (map inc)
                         (filter even?)
                         (take 3)) conj [11 22 33 44 55 66 77])
  ;; => [12 34 56]
  ```

  Example, using the key/index by consulting `*keydex*`:
  ```clojure
  (transduce-kv (comp-kv (map #(+ % (inc *keydex*)))) conj [11 22 33])
  ;; => [12 24 36]

  ;; value  *keydex*  (+ value (inc *keydex*))  eval  result
  ;; 11     0         (+ 11 (inc 0))            12    [12]
  ;; 22     1         (+ 22 (inc 1))            24    [12 24]
  ;; 33     2         (+ 33 (inc 2))            36    [12 24 36]
  ```

  Another example, using the key/index by consulting `*keydex*`:
  ```clojure
  (transduce-kv (comp-kv (filter (fn [_] (<= *keydex* 2)))) conj [11 22 33 44 55])
  ;; [11 22 33]

  ;; value  *keydex*  (<= *keydex* 2)  result
  ;; 11     0         true             [11]
  ;; 22     1         true             [11 22]
  ;; 33     2         true             [11 22 33]
  ;; 44     3         false            [11 22 33]
  ;; 55     4         false            [11 22 33]
  ```

  `*keydex*` always refers to the element's location within the input
  collection, regardless of any elements 'removed' from or 'added' to the
  output.

  Example, illustrating how `*keydex*` refers to original location after an
  element is removed:
  ```clojure
  (transduce-kv (comp-kv (remove (fn [_] (= *keydex* 1)))
                         (map #(vector *keydex* %)))
                conj
                [:foo :bar :baz])
  ;; => [[0 :foo] [2 :baz]]
  ```"
  {:UUIDv4 #uuid "7357eed9-67ea-4269-bd65-7ec23e125328"}
  ([xform f coll] (transduce-kv xform f (f) coll))
  ([xform f init coll]
   (let [f (xform f)
         err #(throw (Exception.
                      (str "`coll` must implement `clojure.lang.IKVReduce`; `coll` is "(type %) " ")))
         ret (if (instance? clojure.lang.IKVReduce coll)
               (.kvreduce ^clojure.lang.IKVReduce coll f init)
               (err coll))]
     (f ret))))


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
  `*keydex*` at any layer of the transducer stack.

  Example with `comp-kv`:
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

