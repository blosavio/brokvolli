(ns brokvolli.transducers-kv
  "Stateless, 'kv-ified' transducers. Safe to use with multi-threaded
  [[brokvolli.multi/transduce-kv]].

  Each returns a transducer like their `clojure.core` namesakes, but with an
  additional arity-3 of *result*, *keydex*, and *value*. The bottom-level
  reducing function must also handle those three args.


  See also [[brokvolli.stateful-transducers-kv]].")


;; General principle: Every time a transducer passes a result down the stack, it must also pass down the keydex, too.

;; Strategies for 'kv-izing' transducers:
;;   New arity-3, with _most_ instances of `(rf result input)` expanded to `(rf result keydex input)`.
;;   Certain instances do not get expanded (?), such as in `interpose`.
;;   Instances of `(pred input)` expand to `(pred keydex input)`.
;;   Composed transducers, like `mapcat`, must use 'kv-ized' components.
;;   Helper functions, like `preserving-reduced` must be adapated to also handle functions of three args.

;; Instead of macro-ing away the arity-1/2/3 of these core transducers, for now, repeat them so that they're easier to eyeball check.


(defn map-kv
  "Apply `f` to each input, similar to
  [`map`](https://clojure.github.io/clojure/clojure.core-api.html#clojure.core/map)
  . `f` is a function of two arguments: key/index and next element.

  Note: This implementation subsumes `map`'s multi-collection variadic arity.

  Example:
  ```clojure
  (transduce-kv (map-kv (fn [keydex x] (vector (inc keydex) (* 10 x))))
                tconj
                [11 22 33])
  ;; => [[1 110] [2 220] [3 330]]
  ```"
  {:UUIDv4 #uuid "f4c81f48-fa5c-4c95-b6b3-22c21877a54c"}
  [f]
  (fn [rf]
    (fn
      ([] (rf))
      ([result] (rf result))
      ([result input]
       (rf result (f input)))
      ([result keydex input]
       (rf result keydex (f keydex input))))))


(defn filter-kv
  "Keeps only elements for which `pred` returns truthy, similar to
  [`filter`](https://clojure.github.io/clojure/clojure.core-api.html#clojure.core/filter)
  . `pred` is a predicate of two arguments: key/index and next element.

  Example:
  ```clojure
  (transduce-kv (filter-kv (fn [keydex x] (and (<= keydex 3)
                                               (even? x))))
                tconj
                [11 22 33 44 55])
  ;; => [22 44]
  ```"
  {:UUIDv4 #uuid "2a43a48b-1a84-4a47-9346-e3a8d341f318"}
  [pred]
  (fn [rf]
    (fn
      ([] (rf))
      ([result] (rf result))
      ([result input]
       (if (pred input)
         (rf result input)
         result))
      ([result keydex input]
       (if (pred keydex input)
         (rf result keydex input)
         result)))))


(defn replace-kv
  "Given a hashmap of replacement pairs, replaces elements equal to the key
  with corresponding value in `smap`, similar to
  [`replace`](https://clojure.github.io/clojure/clojure.core-api.html#clojure.core/replace)
  .

  Note: Unlike many other transducers in this namespace, `replace-kv` ignores
  key/index.

  Example:
  ```clojure
  (transduce-kv (replace-kv {11 :foo 33 :bar}) tconj [11 22 33 44 55])
  ;; => [:foo 22 :bar 44 55]
  ```"
  {:UUIDv4 #uuid "d93028d6-c324-42c3-a144-5d68b623484b"}
  [smap]
  (map-kv (fn repl
            ([_ x] (repl x))
            ([x] (if-let [e (find smap x)]
                   (val e)
                   x)))))


(defn ^:private preserving-reduced
  [rf]
  (fn
    ([x y] (let [ret (rf x y)]
             (if (reduced? ret)
               (reduced ret)
               ret)))
    ([x y z] (let [ret (rf x y z)]
               (if (reduced? ret)
                 (reduced ret)
                 ret)))))


(defn cat-kv
  "Concatenates the contents of input collection, similar to
  [`cat`](https://clojure.github.io/clojure/clojure.core-api.html#clojure.core/cat).

  Note: Unlike many other transducers in this namespace, `cat-kv` does not take
  an explicit argument.

  Example:
  ```clojure
  (transduce-kv cat-kv tconj [[11] [22] [33]])
  ;; => [11 22 33]
  ```
  "
  {:UUIDv4 #uuid "3097982a-c233-4616-8391-2e4f06d10653"}
  [rf]
  (let [rrf (preserving-reduced rf)]
    (fn ct
      ([] (rf))
      ([result] (rf result))
      ([result input]
       (reduce rrf result input))
      ([result keydex input]
       (reduce #(rrf %1 keydex %2) result input)))))


(defn mapcat-kv
  "Applies `f` to the inputs and concatenates the resulting collections, similar
  to [`mapcat`](https://clojure.github.io/clojure/clojure.core-api.html#clojure.core/mapcat)
  . `f` is a function of key/index and the next value, and must return a collection.

  Example:
  ```clojure
  (transduce-kv (mapcat-kv (fn [_ x] (repeat 3 x))) tconj [11 22 33])
  ;; => [11 11 11 22 22 22 33 33 33]
  ```
  "
  {:UUIDv4 #uuid "7d837294-dda6-4579-b27c-9a67b23410c3"}
  [f]
  (comp (map-kv f) cat-kv))


(defn remove-kv
  "Removes elements for which `pred` returns falsey, similar to
  [`remove`](https://clojure.github.io/clojure/clojure.core-api.html#clojure.core/remove)
  . `pred` is a predicate of two arguments: key/index and next element.

  Example:
  ```clojure
  (transduce-kv (remove-kv (fn [keydex _] (even? keydex))) tconj [11 22 33 44 55])
  ;; => [22 44]
  ```"
  {:UUIDv4 #uuid "136792f2-c9d5-45be-ba7d-afc49cbaf91e"}
  [pred]
  (filter-kv (complement pred)))


(defn keep-kv
  "Returns non-`nil` results of `(f keydex value)`, similar to
  [`keep`](https://clojure.github.io/clojure/clojure.core-api.html#clojure.core/keep)
  .

  Example:
  ```clojure
  (transduce-kv (keep-kv (fn [keydex _] (even? keydex))) tconj [11 22 33 44 55])
  ;; => [true false true false true]
  ```"
  {:UUIDv4 #uuid "c8f980bb-af95-40e0-9f06-7883a5770518"}
  [f]
  (fn [rf]
    (fn
      ([] (rf))
      ([result] (rf result))
      ([result input]
       (let [v (f input)]
         (if (nil? v)
           result
           (rf result v))))
      ([result keydex input]
       (let [v (f keydex input)]
         (if (nil? v)
           result
           (rf result keydex v)))))))


(defn random-sample-kv
  "Returns elements as with
  [`random-sample`](https://clojure.github.io/clojure/clojure.core-api.html#clojure.core/random-sample)
  .

  Example:
  ```clojure
  (transduce-kv (random-sample-kv 0.5) tconj [11 22 33 44 55])
  ;; => [11 44]
  ;; different upon each evaluation...
  ```"
  {:UUIDv4 #uuid "a8987e4f-eb0a-4ee4-af27-30e1b8504b50"}
  [prob]
  (filter-kv (fn [_ _] (< (rand) prob))))


(comment ;; not straightforwardly unit-testable
  (transduce-kv (random-sample-kv 0.5)
                tconj
                [11 22 33 44 55])
  )

