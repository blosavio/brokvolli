(ns brokvolli.single
  "Provides a single-threaded 'kv' variant of `clojure.core/transduce`,
  analogous to the way `reduce-kv` relates to `reduce`.")


(defn kv-ize
  " Given a composition of functions `composition` (an xform stack), returns an
  equivalent transformer stack that also accepts *result*, *key/index*, and
  *value*, suitable for use with [[transduce-kv]].

  The composition returned adds two capabilities.

  1. Diverts the key/index provided by `transduce-kv`, passing only the
  accumulated value and the next element to the outer/top transducer, and
  2. Establishes a binding context where the key/index is available from
  [[*keydex*]] at any layer of the transducer stack."
  {:UUIDv4 #uuid "66698ff3-6e41-46ac-b63c-6d0f7d22cfb6"
   :no-doc true}
  [composition]
  (fn [rf offset]
    (let [g (composition rf)]
      (fn
        ([] (g))
        ([result] (g result))
        ([result input] (g result input))
        ([acc k v]
         (binding [brokvolli.core/*keydex* (if offset (+ offset k) k)]
           (g acc v)))))))


(defn transduce-kv
  "A '-kv' variant of `transduce`, reducing with a transformation. The analogy
  is

 `reduce:reduce-kv::transduce:transduce-kv`.

  As with `transduce`, `xform` and `f` are functions of zero, one, and two
  arguments. Works with all the `clojure.core` transducers, excepting the
  `...-indexed` variants.

  `xform` is most straightforwardly composed with `comp`. While `transduce-kv`
  descends into the transducer stack, the key/index available via [[*keydex*]].

  `coll` must implement `clojure.lang.IKVReduce`.

  Examples, not using the key/index:
  ```clojure
  (transduce-kv (map inc) conj [11 22 33 44 55 66 77])
  ;; => [12 23 34 45 56 67 78]

  (transduce-kv (comp (map inc)
                      (filter even?)) conj [11 22 33 44 55 66 77])
  ;; => [12 34 56 78]

  (transduce-kv (comp (map inc)
                      (filter even?)
                      (take 3)) conj [11 22 33 44 55 66 77])
  ;; => [12 34 56]
  ```

  Example, using the key/index by consulting `*keydex*`:
  ```clojure
  (require '[brokvolli.core :refer [*keydex*]])

  (transduce-kv (map #(+ % (inc *keydex*))) conj [11 22 33])
  ;; => [12 24 36]

  ;; value  *keydex*  (+ value (inc *keydex*))  eval  result
  ;; 11     0         (+ 11 (inc 0))            12    [12]
  ;; 22     1         (+ 22 (inc 1))            24    [12 24]
  ;; 33     2         (+ 33 (inc 2))            36    [12 24 36]
  ```

  Another example, using the key/index by consulting `*keydex*`:
  ```clojure
  (transduce-kv (filter (fn [_] (<= *keydex* 2))) conj [11 22 33 44 55])
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
  (transduce-kv (comp (remove (fn [_] (= *keydex* 1)))
                      (map #(vector *keydex* %)))
                conj
                [:foo :bar :baz])
  ;; => [[0 :foo] [2 :baz]]
  ```

  Note: Some transducer functions may not involve the actual value, e.g.,
  `filter`-ing based on the index. In those cases, the `#(...)` anonymous
  function shorthand may be problematic because the value is always passed as an
  argument, but the expression won't contain a `%`. Without a `%`, the compiler
  assumes the function receives zero arguments, not one. Instead, use the
  `(fn [_] (...))` idiom to discard the argument. See the `remove` expression in
  the example above."
  {:UUIDv4 #uuid "7357eed9-67ea-4269-bd65-7ec23e125328"}
  ([xform f coll] (transduce-kv xform f (f) coll))
  ([xform f init coll] (transduce-kv xform f init coll nil))
  ([xform f init coll offset]
   (let [f ((kv-ize xform) f offset)
         err #(throw (Exception.
                      (str "`coll` must implement `clojure.lang.IKVReduce`; `coll` is "(type %) " ")))
         ret (if (instance? clojure.lang.IKVReduce coll)
               (.kvreduce ^clojure.lang.IKVReduce coll f init)
               (err coll))]
     (f ret))))

