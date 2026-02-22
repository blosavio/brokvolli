(ns brokvolli.single
  "Provides a single-threaded 'kv' variant of `clojure.core/transduce`,
  analogous to the way
  [`reduce-kv`](https://clojure.github.io/clojure/clojure.core-api.html#clojure.core/reduce-kv)
  relates to
  [`reduce`](https://clojure.github.io/clojure/clojure.core-api.html#clojure.core/reduce).")


(defn transduce-kv*
  "Performs the reduction, with an offset."
  {:UUIDv4 #uuid "7357eed9-67ea-4269-bd65-7ec23e125328"
   :no-doc true}
  ([offset xform f init coll]
   (let [xf (xform f)
         f-off (fn [f] (fn [acc keydex x] (f acc (if offset (+ offset keydex) keydex) x)))
         err #(throw (Exception.
                      (str "`coll` must implement `clojure.lang.IKVReduce`; `coll` is "(type %) " ")))
         ret (if (instance? clojure.lang.IKVReduce coll)
               (.kvreduce ^clojure.lang.IKVReduce coll (f-off xf) init)
               (err coll))]
     (xf ret))))


(defn transduce-kv
  "A '-kv' variant of `transduce`, reducing with a transducing stack. The
  analogy is

  `transduce-kv:transduce::reduce-kv:reduce`.

  Analogous to
  [`transduce`](https://clojure.github.io/clojure/clojure.core-api.html#clojure.core/transduce),
  `xform` and `f` are functions of zero, one, and two arguments, but with an
  additional arity accepting three arguments. The [[brokvolli.transducers-kv]]
  and [[brokvolli.stateful-transducers-kv]] namespaces provide such transducers
  (variants of `map`, `filter`, etc.) appropriate for use with `transduce-kv`.

  For exploratory work, or if the stack is constructed with `clojure.core`
  transducers or otherwise not under our control, [[kv-ize]] may be used to
  adapt the transducer, excepting the `...-indexed` variants..

  `xform` is most straightforwardly composed with `comp`.

  `coll` must implement `clojure.lang.IKVReduce`.

  See [[tconj]] and [[tassoc]] for utilities that provide a reducing function
  that properly shuttles the key/indexes.

  Prepare the environment for following examples:
  ```clojure
  (require '[brokvolli.core :refer [tconj]]
           '[brokvolli.transducers-kv :refer [map-kv filter-kv remove-kv]]
           '[brokvolli.stateful-transducers-kv :refer [take-kv]])
  ```

  Examples, ignoring the key/index:
  ```clojure
  (transduce-kv (map-kv (fn [_ x] (inc x)))
                tconj
                [11 22 33 44 55 66 77])
  ;; => [12 23 34 45 56 67 78]

  (transduce-kv (comp (map-kv (fn [_ x] (inc x)))
                      (filter-kv (fn [_ x] (even? x))))
                tconj
                [11 22 33 44 55 66 77])
  ;; => [12 34 56 78]

  (transduce-kv (comp (map-kv (fn [_ x] (inc x)))
                      (filter-kv (fn [_ x] (even? x)))
                      (take-kv 3))
                tconj
                [11 22 33 44 55 66 77])
  ;; => [12 34 56]
  ```

  Example, using the key/index:
  ```clojure
  (transduce-kv (map-kv (fn [keydex x] (+ x (inc keydex))))
                tconj
                [11 22 33])
  ;; => [12 24 36]

  ;; value  keydex  (+ value (inc keydex))  eval  result
  ;; 11     0       (+ 11 (inc 0))          12    [12]
  ;; 22     1       (+ 22 (inc 1))          24    [12 24]
  ;; 33     2       (+ 33 (inc 2))          36    [12 24 36]
  ```

  Another example, using the key/index:
  ```clojure
  (transduce-kv (filter-kv (fn [keydex _] (<= keydex 2)))
                tconj
                [11 22 33 44 55])
  ;; =>[11 22 33]

  ;; value  keydex  (<= keydex 2)  result
  ;; 11     0       true           [11]
  ;; 22     1       true           [11 22]
  ;; 33     2       true           [11 22 33]
  ;; 44     3       false          [11 22 33]
  ;; 55     4       false          [11 22 33]
  ```

  The key/index always refers to the element's location within the input
  collection, regardless of any elements \"removed\" from or \"added\" to the
  output.

  Example, illustrating how key/index refers to original \"location\" after an
  element is removed:
  ```clojure
  (transduce-kv (comp (remove-kv (fn [keydex _] (= keydex 1)))
                      (map-kv (fn [keydex x] [keydex x])))
                tconj
                [:foo :bar :baz])
  ;; => [[0 :foo] [2 :baz]]
  ```

  `:baz` remains \"located\" at index `2` throughout the transduction, even
  though `:bar` was removed.

  Note: Some transducer functions may not involve the actual value, e.g.,
  `filter`-ing based only on the index. In those cases, the `#(...)` anonymous
  function shorthand may be problematic because two arguments are always passed,
  but the expression won't contain a `%2`. Without a `%2`, the compiler
  assumes the function receives one argument, not two. Instead, use the
  `(fn [foo _] (...))` idiom to discard the second argument. See the `remove-kv`
  expression in the example above."
  {:UUIDv4 #uuid "4c782821-725a-4cd7-a0a2-2d33cd381d0f"}
  ([xform f coll] (transduce-kv xform f (f) coll))
  ([xform f init coll] (transduce-kv* nil xform f init coll)))

