(ns brokvolli.transducers-kv
  "Stateless, 'kv-ified' transducers. Safe to use with multi-threaded
  [[brokvolli.multi/transduce-kv]].

  See also [[brokvolli.stateful-transducer-kv]].")


;; General principle: Every time a transducer passes a result down the stack, it must also pass down the keydex, too.

;; Strategies for 'kv-izing' transducers:
;;   New arity-3, with _most_ instances of `(rf result input)` expanded to `(rf result keydex input)`.
;;   Certain instances do not get expanded (?), such as in `interpose`.
;;   Instances of `(pred input)` expand to `(pred keydex input)`.
;;   Composed transducers, like `mapcat`, must use 'kv-ized' components.
;;   Helper functions, like `preserving-reduced` must be adapated to also handle functions of three args.

;; Instead of macro-ing away the arity-1/2/3 of these core transducers, for now, repeat them so that they're easier to eyeball check.


(defn map-kv
  "Returns a mapping transducer like `clojure.core/map`, but with an additional
  arity-3 of *result*, *keydex*, and *value*. The bottom-level reducing function
  must also handle those three args.

  Note: This additional arity subsumes `map`'s multi-collection variadic arity."
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
  "Returns a filtering transducer like `clojure.core/filter`, but with an
  additional arity-3 of *result*, *keydex*, and *value*. The bottom-level
  reducing function must also handle those three args.

  `pred` is a predicate of *keydex* and *value*."
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
  "Returns a replacing transducer like `clojure.core/replace`, but with an
  additional arity-3 of *result*, *keydex*, and *value*."
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
  "Returns a concatenating transducer like `clojure.core/cat`, but with an
  additional arity-3 of *result*, *keydex*, and *value*."
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
  "Returns a mapcatting transducer like `clojure.core/mapcat`, but with an
  additional arity-3 of *result*, *keydex*, and *value*."
  {:UUIDv4 #uuid "7d837294-dda6-4579-b27c-9a67b23410c3"}
  [f]
  (comp (map-kv f) cat-kv))


(defn remove-kv
  "Returns a remove-ing transducer like `clojure.core/remove`, but
  with an additional arity-3 of *result*, *keydex*, and *value*."
  {:UUIDv4 #uuid "136792f2-c9d5-45be-ba7d-afc49cbaf91e"}
  [pred]
  (filter-kv (complement pred)))


(defn keep-kv
  "Returns a keeping transducer like `clojure.core/keep`, but with an
  additional arity-3 of *result*, *keydex*, and *value*."
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
  "Returns a dedupe-ing transducer like `clojure.core/dedupe`, but with an
  additional arity-3 of *result*, *keydex*, and *value*."
  {:UUIDv4 #uuid "a8987e4f-eb0a-4ee4-af27-30e1b8504b50"}
  [prob]
  (filter-kv (fn [_ _] (< (rand) prob))))


(comment ;; not straightforwardly unit-testable
  (transduce-kv (random-sample-kv 0.5)
                (fn
                  ([] [])
                  ([x] x)
                  ([x _ z] (conj x z)))
                [11 22 33 44 55])
  )

