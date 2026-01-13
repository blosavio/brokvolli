(ns brokvolli.serial
  "Provides a 'kv' variant of `clojure.core/transduce`, analogous how
  `reduce-kv` relates to `reduce`.")


(defn transduce-kv
  "The '-kv' variant of `transduce`. The analogy is
  `transduce`:`reduce`::`transduce`:`transduce-kv`.

  Like `reduce-kv`, `xform` and `f` are functions of zero, two, or three
  arguments."
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


(defn kv-ify
  "Given a transducer function `f` of the standard three arities (zero, one, and
  two) returns a 'kv-ified' transducer with a fourth arrity of
  accumulation, keydex, and value that accepts the arguments from `reduce-kv`.

  Useful to adjust the outer ('top') transducer function of a compostion. See
  [[comp-kv]] for a utility that composes transducers while automatically
  applying `kv-ify`.

  Note: Does not preserve varargs arities, e.g., `map` transducer.

  Example:
  ```clojure
  (kv-ify (filter #(even? (%1))))
  ```"
  {:UUIDv4 #uuid "5706173d-d907-4df1-8bb0-8c140e22a9bc"}
  [f]
  (fn [rf]
    (let [g (f rf)]
      (fn
        ([] (g))
        ([result] (g result))
        ([result input] (g result input))
        ([acc k v] (g acc [k v]))))))


(defn comp-kv
  "Returns a compostion of transducers, suitable for use with [[transduce-kv]].

  Given a series of transducer functions `fns` returns a composition of those
  functions with
  1. The outer/top transducer [[kv-ify]]-ed, ready to accept the initial
  discrete key+values supplied by `transduce-kv`, and
  2. A final transducer appended that removes the in-band keydexes.

  `(comp-kv)` returns `identity`.

  Example with `comp-kv`:
  ```clojure
  (transduce-kv (comp-kv
                 (map #(vector (% 0) (inc (% 1))))
                 (filter #(even? (% 0)))
                 (take 3))
                conj
                [11 22 33 44 55 66 77 88 99])
  ;; => [12 34 56]
  ```

  Is equivalent to this example with `clojure.core/comp`:

  ```clojure
  (transduce-kv (comp
                 (kv-ify (map #(vector (% 0) (inc (% 1)))))
                 (filter #(even? (% 0)))
                 (take 3)
                 (map second))
                conj
                [11 22 33 44 55 66 77 88 99])
  ;; => [12 34 56]
  ```

  To retain the final keydex+value 2-tuple, explicitly add a trailing transducer
  `(map #(vector :ignored (% 0) (% 1)))`.

  Example:
  ```clojure
  (transduce-kv (comp-kv
                 (map #(vector (% 0) (% 1)))
                 (map #(vector :foo [(% 0) (% 1)])))
                conj
                [11 22 33])
  ;; => [[0 11] [1 22] [2 33]]
  ```"
  {:UUIDv4 #uuid "29baf051-d192-4688-a978-139a8f886721"
   :implementation "`fns` is a clojure.core.ArraySeq, conj-ing onto the head."}
  [& fns]
  (let [appended-args (conj (vec fns) (map second))]
    (apply comp (kv-ify (first appended-args)) (next appended-args))))

