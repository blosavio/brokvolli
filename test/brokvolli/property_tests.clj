(ns brokvolli.property-tests
  "Property tests for brokvolli's `transduce` variants.

  Part 1: Round-tripping properties
  Part 2: Input/output relationships
  Part 3: Oracle/gold-standard tests

  See also `brokvolli.property-tests-amiller` for adaptations of Alex Miller's
  transducers property tests."
  (:require
   [brokvolli.core :refer [concatv
                           *keydex*
                           kv-ize
                           tconj]]
   [brokvolli.multi :as multi]
   [brokvolli.single :as single]
   [brokvolli.stateful-transducers-kv :refer :all]
   [brokvolli.transducers-kv :refer :all]
   [clojure.string :as str]
   [clojure.test :refer [run-test
                         run-tests]]
   [clojure.test.check :as chk]
   [clojure.test.check.clojure-test :as clj-test]
   [clojure.test.check.generators :as gen]
   [clojure.test.check.properties :as prop]))


;; Clojure's transducer property testing ns needed to test both the transducing
;; contexts (`transduce`, `into`, etc.)  and the transducers themselves (`map`,
;; `filter`, etc.). When something went wrong, they needed detailed information
;; about whether the transducer or the context was the problem. Brokvolli only
;; introduces new contexts and merely _adapted_ transducers, so let's see if we
;; can get away with simpler property tests.

;; Only numbers and strings have interesting operations (`inc`, `upper-case`,
;; etc.), so only generate collections containing those scalar types. When
;; testing the transducing contexts, don't exercise every off-the-shelf
;; transducer, perhaps only `map`, `filter`, `remove`, `take`, `drop`, and
;; `mapcat`. When testing the transducers, stick with a basic vector of numbers.


(def n-checks 1000)


;; helper fns

(def step-kv
  (fn
    ([] {})
    ([result] result)
    ([result value] (assoc result *keydex* value))
    ([result keydex value] (assoc result keydex value))))

(def step
  (completing
   (fn
     ([] {})
     ([result [k v]] (assoc result k v)))))


;;;; Part 1: Round-tripping tests


(comment ;; demonstrate upper-, then lower-casing
  (single/transduce-kv (kv-ize (comp (map str/upper-case)
                                     (map str/lower-case)))
                       conj
                       ["a" "bc" "def"])

  (single/transduce-kv (comp (map-kv (fn [_ s] (str/upper-case s)))
                             (map-kv (fn [_ s] (str/lower-case s))))
                       tconj
                       ["a" "bc" "def"])
  )


(def str-gen
  (let [f #(-> %
               str/lower-case
               (str/replace #"[^a-zA-Z]" ""))]
    (gen/vector (gen/fmap f gen/string-alphanumeric))))


(def xduce-all-gen (gen/elements [clojure.core/transduce
                                  single/transduce-kv
                                  multi/transduce
                                  multi/transduce-kv]))


(def xduce-gen (gen/elements [clojure.core/transduce
                              multi/transduce]))


(def xduce-kv-gen (gen/elements [single/transduce-kv
                                 multi/transduce-kv]))



(def round-trip-casing
  (prop/for-all
   [s str-gen
    xduce xduce-gen]
   (= s
      (xduce (comp (map str/upper-case)
                   (map str/lower-case))
             conj
             s)
      (->> s
           (xduce (comp (map str/upper-case)) conj)
           (xduce (comp (map str/lower-case)) conj)))))


(clj-test/defspec
  test-round-trip-casing
  n-checks
  round-trip-casing)


(def round-trip-casing-kv
  (prop/for-all
   [s str-gen
    xduce xduce-kv-gen]
   (= s
      (xduce (kv-ize (comp (map str/upper-case)
                           (map str/lower-case)))
             conj
             s)
      (->> s
           (xduce (kv-ize (map str/upper-case)) conj)
           (xduce (kv-ize (map str/lower-case)) conj))
      (xduce (comp (map-kv (fn [_ s] (str/upper-case s)))
                   (map-kv (fn [_ s] (str/lower-case s))))
             tconj
             s)
      (->> s
           (xduce (map-kv (fn [_ s] (str/upper-case s))) tconj)
           (xduce (map-kv (fn [_ s] (str/lower-case s))) tconj)))))



(clj-test/defspec
  test-round-trip-casing-kv
  n-checks
  round-trip-casing-kv)


(comment ;; demonstrate incrementing, then decrementing
  (single/transduce-kv (kv-ize (comp (map inc)
                                     (map dec)))
                       conj
                       [11 22 33])
  )


(def int-gen (gen/vector gen/small-integer))


(def round-trip-incrementing
  (prop/for-all
   [i int-gen
    xduce xduce-gen]
   (= i
      (xduce (kv-ize (comp (map inc)
                           (map dec)))
             conj i)
      (->> i
           (xduce (kv-ize (map inc)) conj)
           (xduce (kv-ize (map dec)) conj)))))


(clj-test/defspec
  test-round-trip-incrementing
  n-checks
  round-trip-incrementing)


(def round-trip-incrementing-kv
  (prop/for-all
   [i int-gen
    xduce xduce-kv-gen]
   (= i
      (xduce (kv-ize (comp (map inc)
                           (map dec)))
             conj i)
      (->> i
           (xduce (kv-ize (map inc)) conj)
           (xduce (kv-ize (map dec)) conj))
      (xduce (comp (map-kv (fn [_ i] (inc i)))
                   (map-kv (fn [_ i] (dec i))))
             tconj i)
      (->> i
           (xduce (map-kv (fn [_ i] (inc i))) tconj)
           (xduce (map-kv (fn [_ i] (dec i))) tconj)))))



(clj-test/defspec
  test-round-trip-incrementing-kv
  n-checks
  round-trip-incrementing-kv)


;;;; Part 2: Input/output tests


(comment ;; demonstrate filtering to keep keys that are even integers
  (keys (single/transduce-kv (kv-ize (filter (fn [_] (even? *keydex*))))
                             step-kv
                             {0 :foo, 1 :bar, 2 :baz, 3 :qux}))

  (keys (single/transduce-kv (filter-kv (fn [keydex _] (even? keydex)))
                             step-kv
                             {0 :foo 1 :bar 2 :baz 3 :qux}))
  )


(def hashmap-int-int-gen (gen/map gen/nat gen/small-integer))


(def filter-by-keys
  (prop/for-all
   [m hashmap-int-int-gen
    xduce-kv xduce-kv-gen]
   (and
    (every? even? (keys (xduce-kv (kv-ize (filter (fn [_] (even? *keydex*)))) step-kv m)))
    (every? even? (keys (xduce-kv (filter-kv (fn [keydex _] (even? keydex))) step-kv m))))))


(clj-test/defspec
  test-filter-by-keys
  n-checks
  filter-by-keys)


(comment ;; demonstrate 'take'-ing three elements
  (single/transduce-kv (kv-ize (take 3))
                       conj
                       [11 22 33 44 55])

  (single/transduce-kv (take-kv 3) tconj [11 22 33 44 55])

  )


(def shorten-with-take
  (prop/for-all
   [v (gen/vector gen/simple-type)
    tk gen/nat
    xduce xduce-gen]
   (= (min tk (count v))
      (count (xduce (kv-ize (take tk)) conj v))
      (count (xduce (take-kv tk) tconj v)))))


(clj-test/defspec
  test-shorten-with-take
  n-checks
  shorten-with-take)


(comment ;; demonstrate expanding 3X
  (single/transduce-kv (kv-ize (mapcat #(repeat 3 %))) conj [11 22 33])
  (single/transduce-kv (mapcat-kv (fn [_ x] (repeat 3 x))) tconj [11 22 33])
  )


(def expand-with-mapcat
  (prop/for-all
   [v (gen/vector gen/simple-type)
    x gen/nat
    xduce xduce-kv-gen]
   (= (* x (count v))
      (count (xduce (kv-ize (mapcat #(repeat x %))) conj v))
      (count (xduce (mapcat-kv (fn [_ y] (repeat x y))) tconj v)))))


(clj-test/defspec
  test-expand-with-mapcat
  n-checks
  expand-with-mapcat)


;;;; Part 3: Oracle tests


(comment ;; sketch property tests of transducing over sequential collections
  ;; ops on numbers
  (single/transduce-kv (kv-ize (map inc)) conj [11 22 33])
  (transduce (map inc) conj [11 22 33])

  ;; ops on strings
  (single/transduce-kv (kv-ize (map str/upper-case)) conj ["a" "bc" "def"])
  (transduce (map str/upper-case) conj ["a" "bc" "def"])

  ;; get shorter: filter, remove, take, drop
  (single/transduce-kv (kv-ize (filter odd?)) conj [11 22 33])
  (transduce (filter odd?) conj [11 22 33])

  (single/transduce-kv (kv-ize (take 3)) conj [11 22 33 44 55])
  (transduce (take 3) conj [11 22 33 44 55])

  ;; expand
  (single/transduce-kv (kv-ize (mapcat #(repeat 3 %))) conj [11 22 33])
  (transduce (mapcat #(repeat 3 %)) conj [11 22 33])
  )


(defn transducerize
  "Given sequence `s` of functions, returns a sequence of transducers by
  applying transducer `xducer`.

  Example:
  ```clojure
  (transducerize map [inc dec])
  ```
  ...returns `[(map inc) (map dec)]`."
  {:UUIDv4 #uuid "7ee6ea2e-af0d-444c-9867-2915657ca858"
   :no-doc true}
  [xducer s]
  (map #(xducer %) s))


(defn gen-vec-ele
  "Returns `(gen/vector (gen/elements coll) mx mn)`."
  {:UUIDv4 #uuid "76ad7b16-06a2-4da4-a61b-8a34e608a6f2"
   :no-doc true}
  [coll mn mx]
  (gen/vector (gen/elements coll) mn mx))


(def num-alts #{inc dec -})
(def num-preds #{even? odd?})

(def str-cutoff 3) ;; avoid catching too many and collapsing the coll to empty

(def str-alts #{str/upper-case str/lower-case str/capitalize})
(def str-preds #{#(<= (count %) str-cutoff)
                 #(< str-cutoff (count %))})

(def max-shortening-xforms 3) ;; don't use too many shortening xforms
(def min-xforms 1)
(def max-xforms 8)


;; Rejected using `(gen/let [i gen/small-integer] (take i))` pattern, because
;; don't want to whittle coll away if `i` gets too large after repeated
;; `take`-ing and `remove`-ing. Analogous idea applies regarding `n` within the
;; the `mapcat`'s `repeat`: don't want the length to explode.


(def gen-vec-xforms
  (gen/let
      [element-type (gen/elements [:nums
                                   :strings])
       size gen/nat
       v (case element-type
           :nums (gen/vector gen/nat size)
           :strings (gen/vector gen/string-ascii size))
       xforms-shortening (gen-vec-ele [(take (int (* 0.9 size)))
                                       (drop (int (* 0.1 size)))]
                                      0 max-shortening-xforms)
       xforms-expanding (gen-vec-ele [(mapcat #(repeat 3 %))]
                                     0 max-xforms)
       xforms-altering (case element-type
                         :nums (gen-vec-ele (concat
                                             (transducerize map num-alts)
                                             (transducerize filter num-preds))
                                            min-xforms max-xforms)
                         :strings (gen-vec-ele (concat
                                                (transducerize map str-alts)
                                                (transducerize filter str-preds))
                                               min-xforms max-xforms))
       xforms (gen-vec-ele (concat xforms-shortening
                                   xforms-expanding
                                   xforms-altering)
                           min-xforms max-xforms)]
    {:type element-type
     :v v
     :xforms xforms}))


(def sequential-properties
  (prop/for-all [info gen-vec-xforms]
                (let [{element-type :type
                       v :v
                       xforms :xforms} info]
                  (= (transduce                   (apply comp xforms)  conj v)
                     (single/transduce-kv (kv-ize (apply comp xforms)) conj v)
                     (multi/transduce             (apply comp xforms)  conj v)
                     (multi/transduce-kv  (kv-ize (apply comp xforms)) conj v)))))


(clj-test/defspec
  test-seq-props
  n-checks
  sequential-properties)


(comment
  (gen/sample gen-vec-xforms)


  (let [{type :type
         v :v
         xforms :xforms} (gen/generate gen-vec-xforms)]
    (= (transduce                   (apply comp xforms)  conj v)
       (single/transduce-kv (kv-ize (apply comp xforms)) conj v)))

  (chk/quick-check n-checks sequential-properties)
  )


(comment ;; Sketch property tests of transducing over associative colls

  ;; ops on numbers: inc, dec, -
  (single/transduce-kv (kv-ize (map inc)) step-kv {:a 11 :b 22 :c 33})
  (transduce (comp (map #(update % 1 inc))) step {:a 11 :b 22 :c 33})

  ;; ops on strings: upper-case, lower-case, capitalize
  (single/transduce-kv (kv-ize (map str/upper-case)) step-kv {:a "a" :b "bc" :c "def"})
  (transduce (comp (map #(update % 1 str/upper-case))) step {:a "a" :b "bc" :c "def"})


  ;; get shorter: filter, remove, take, drop
  (single/transduce-kv (kv-ize (filter (fn [_] (even? *keydex*)))) step-kv {0 11, 1 22, 2 33})
  (transduce (comp (filter #(even? (% 0)))) step {0 11, 1 22, 2 33})

  (single/transduce-kv (kv-ize (filter (fn [_] (< *keydex* 3)))) step-kv {0 "a", 1 "bc", 2 "def", 3 "ghij", 4 "klmno"})
  (transduce (comp (filter #(< (% 0) 3))) step {0 "a", 1 "bc", 2 "def", 3 "ghij", 4 "klmno"})
  )


(defn transducerize-two-keydex
  "Given sequence `s` of functions, returns a sequence of hashmaps by applying
  transducer `xducer`. The hashmap elements are `:core` and `:brokvolli`
  assoc-ed to transducerized functions of key/indexes.

  See also [[transducerize-two-value]] and [[transducerize]]."
  {:UUIDv4 #uuid "e3aa9ca8-ded4-4cdb-988f-ab1dab36f3c0"
   :no-doc true}
  [xducer s]
  (map #(hash-map :core (xducer (fn [[k v]] (% k)))
                  :brokvolli (xducer (fn [_] (% *keydex*))))
       s))


(defn transducerize-two-value
  "Given sequence `s` of functions, returns a sequence of hashmaps by applying
  transducer `xducer`. The hashmap elements are `:core` and `:brokvolli`
  assoc-ed to transducerized functions of values.

  See also [[transducerize-two-keydex]] and [[transducerize]]."
  {:UUIDv4 #uuid "d1b46c6a-437f-4c4e-a199-6ade8429dfd9"
   :no-doc true}
  [xducer s]
  (map #(hash-map :core (xducer (fn [kv] (update kv 1 %)))
                  :brokvolli (xducer (fn [v] (% v))))
       s))


(def gen-hashmap-xforms
  (gen/let
      [element-type (gen/elements [:nums
                                   :strings])
       m (gen/map gen/nat (case element-type
                            :nums gen/nat
                            :strings gen/string-ascii))
       xforms-altering (case element-type
                         :nums (gen-vec-ele (transducerize-two-value map num-alts) min-xforms max-xforms)
                         :strings (gen-vec-ele (transducerize-two-value map str-alts) min-xforms max-xforms))
       ;; Note: 'expanding' is ~invisible in associative collections
       xforms-shortening (gen-vec-ele (transducerize-two-keydex filter num-preds)
                                      min-xforms max-shortening-xforms)
       xforms (gen-vec-ele (concat xforms-shortening
                                   xforms-altering)
                           min-xforms max-xforms)]
    {:type element-type
     :m m
     :xforms xforms}))


(def associative-properties
  (prop/for-all
   [info gen-hashmap-xforms]
   (let [{element-type :type
          m :m
          xforms :xforms} info]
     (= (transduce                   (apply comp (map :core xforms))       step    m)
        (multi/transduce             (apply comp (map :core xforms))       step    m)
        (single/transduce-kv (kv-ize (apply comp (map :brokvolli xforms))) step-kv m)
        (multi/transduce-kv  (kv-ize (apply comp (map :brokvolli xforms))) step-kv m)))))


(comment
  (gen/sample gen-hashmap-xforms)

  (let [g (gen/sample gen-hashmap-xforms)
        x (last g)
        xforms (x :xforms)
        m (x :m)]
    (= (single/transduce-kv (kv-ize (apply comp (map :brokvolli xforms))) step-kv m)
       (transduce (apply comp (map :core xforms)) step m)))

  (chk/quick-check n-checks associative-properties)
  )


(clj-test/defspec
  test-assoc-props
  n-checks
  associative-properties)


;;;; sketch property tests for reduce-kv type processes; stick with non-stateful transducers

;; `map` increment, decrement, and identity


(defn er-er
  "Given a function of one argument `f`, returns a function that replicates
  that arity, with an additional 2-arg arity that ignores the first arg."
  {:UUIDv4 #uuid "6b71093c-4f3f-457c-ba9d-b5875a20c985"
   :no-doc true}
  [f]
  (fn
    ([x] (f x))
    ([_ x] (f x))))


(def inc-er (er-er inc))
(def dec-er (er-er dec))
(def ident-er (er-er identity))


#_(let [f inc-er #_dec-er #_ident-er]
    (=
     (transduce           (map-kv f)  conj [11 22 33])
     (single/transduce-kv (map-kv f) tconj [11 22 33])
     (multi/transduce     (map-kv f)  conj [11 22 33])
     (multi/transduce     (map-kv f) tconj [11 22 33])))


;; `filter` and `remove` odds and evens


(def even?-er (er-er even?))
(def odd?-er (er-er odd?))
(def any?-er (er-er any?))

#_(let [f #_even?-er #_odd?-er any?-er
        xducer #_remove-kv filter-kv]
    (=
     (transduce           (xducer even?-er)  conj [11 22 33 44 55 66])
     (single/transduce-kv (xducer even?-er) tconj [11 22 33 44 55 66])
     (multi/transduce     (xducer even?-er)  conj [11 22 33 44 55 66])
     (multi/transduce-kv  (xducer even?-er) tconj [11 22 33 44 55 66])))


;; replace elements


#_(let [replacer {11 :foo
                  33 :bar
                  55 :baz}]
    (=
     (transduce           (replace-kv replacer)  conj [11 22 33 44 55])
     (single/transduce-kv (replace-kv replacer) tconj [11 22 33 44 55])
     (multi/transduce    2 (replace-kv replacer) tconj concatv [11 22 33 44 55])
     (multi/transduce-kv 2 (replace-kv replacer) tconj concatv [11 22 33 44 55])))


;; vary:
;; * length of `coll`
;; * number and order of transducers in stack
;; * `map-kv` with inc, dec, ident
;; * `filter-kv`/`remove-kv` with `even?`, `odd?`, `any?`
;; * `replace-kv` with various replacements (extract a few elements at random from coll)
;; * `n`-sized partitions for the multi-threaded variants



;; Integrated property tests of `transduce` variants, kv transducer variants,
;; and multi-threading equivalence


(def integrated-tests-gen
  (gen/let
      [size gen/nat
       n (gen/choose 2 (max 2 size))
       v (gen/vector gen/small-integer size)
       xform (gen/vector
              (gen/elements [(map-kv inc-er)
                             (map-kv dec-er)
                             (map-kv ident-er)
                             (filter-kv even?-er)
                             (filter-kv odd?-er)
                             (filter-kv any?-er)
                             (remove-kv even?-er)
                             (remove-kv odd?-er)
                             (remove-kv any?-er)
                             (replace-kv
                              (if (empty? v)
                                {}
                                (gen/map (gen/elements v) gen/small-integer)))])
              min-xforms max-xforms)]
    {:size size
     :v v
     :n n
     :xform xform}))


#_(gen/sample integrated-tests-gen)


(def integrated-props
  (prop/for-all
   [info integrated-tests-gen]
   (let [{v :v
          n :n
          xform :xform} info]
     (= (transduce             (apply comp xform)  conj         v)
        (single/transduce-kv   (apply comp xform) tconj         v)
        (multi/transduce     n (apply comp xform) tconj concatv v)
        (multi/transduce-kv  n (apply comp xform) tconj concatv v)))))


#_(chk/quick-check n-checks integrated-props)


(clj-test/defspec
  test-integrated
  n-checks
  integrated-props)



;;;; stateless transducers-kv properties


(let [v [11 22 33]]
  (=
   (map inc v)
   (transduce (map inc) conj v)))


(def gen-vec-and-splits
  (gen/let
      [v (gen/vector gen/small-integer)
       n (gen/choose 2 (max 2 (count v)))
       replacements (if (empty? v)
                      (gen/hash-map)
                      (gen/map
                       (gen/elements v)
                       gen/small-integer {:min-elements 0
                                          :max-elements 8}))]
    {:v v
     :n n
     :replacements replacements}))


(def map-kv-properties
  (prop/for-all
   [info gen-vec-and-splits]
   (let [{v :v
          n :n} info]
     (= (mapv inc v)
        (transduce             (map    inc)     conj         v)
        (single/transduce-kv   (map-kv inc-er) tconj         v)
        (multi/transduce     n (map-kv inc-er) tconj concatv v)
        (multi/transduce-kv  n (map-kv inc-er) tconj concatv v)))))


(clj-test/defspec
  test-map-kv
  n-checks
  map-kv-properties)


(def filter-kv-properties
  (prop/for-all
   [info gen-vec-and-splits]
   (let [{v :v
          n :n} info]
     (= (filterv even? v)
        (transduce             (filter               even?)      conj         v)
        (single/transduce-kv   (filter-kv (fn [_ x] (even? x))) tconj         v)
        (multi/transduce     n (filter-kv            even?)     tconj concatv v)
        (multi/transduce-kv  n (filter-kv (fn [_ x] (even? x))) tconj concatv v)))))


(clj-test/defspec
  test-filter-kv
  n-checks
  filter-kv-properties)


(def gen-vec-of-vecs-and-splits
  (gen/let
      [vv (gen/vector (gen/vector gen/small-integer))
       n (gen/choose 2 (max 2 (count vv)))]
    {:vv vv
     :n n}))


(def cat-kv-properties
  (prop/for-all
   [info gen-vec-of-vecs-and-splits]
   (let [{vv :vv
          n :n} info]
     (= (apply concat vv)
        (transduce             cat     conj         vv)
        (single/transduce-kv   cat-kv tconj         vv)
        (multi/transduce     n cat-kv tconj concatv vv)
        (multi/transduce-kv  n cat-kv tconj concatv vv)))))


(clj-test/defspec
  test-cat-kv
  n-checks
  cat-kv-properties)


(def keep-kv-properties
  (prop/for-all
   [info gen-vec-and-splits]
   (let [{v :v
          n :n} info]
     (= (keep even? v)
        (transduce             (keep               even?)      conj         v)
        (single/transduce-kv   (keep-kv (fn [_ x] (even? x))) tconj         v)
        (multi/transduce     n (keep-kv            even?)     tconj concatv v)
        (multi/transduce-kv  n (keep-kv (fn [_ x] (even? x))) tconj concatv v)))))


(clj-test/defspec
  test-keep-kv
  n-checks
  keep-kv-properties)


(def mapcat-kv-properties
  (prop/for-all
   [info gen-vec-of-vecs-and-splits]
   (let [{vv :vv
          n :n} info]
     (= (mapcat reverse vv)
        (transduce             (mapcat               reverse)      conj         vv)
        (single/transduce-kv   (mapcat-kv (fn [_ v] (reverse v))) tconj         vv)
        (multi/transduce     n (mapcat-kv            reverse)     tconj concatv vv)
        (multi/transduce-kv  n (mapcat-kv (fn [_ v] (reverse v))) tconj concatv vv)))))


(clj-test/defspec
  test-mapcat-kv
  n-checks
  mapcat-kv-properties)


;; punt `random-sample-kv` property tests


(def remove-kv-properties
  (prop/for-all
   [info gen-vec-and-splits]
   (let [{v :v
          n :n} info]
     (= (remove even? v)
        (transduce             (remove               even?)      conj         v)
        (single/transduce-kv   (remove-kv (fn [_ x] (even? x))) tconj         v)
        (multi/transduce     n (remove-kv            even?)     tconj concatv v)
        (multi/transduce-kv  n (remove-kv (fn [_ x] (even? x))) tconj concatv v)))))


(clj-test/defspec
  test-remove-kv
  n-checks
  remove-kv-properties)


(def replace-kv-properties
  (prop/for-all
   [info gen-vec-and-splits]
   (let [{v :v
          n :n
          replacements :replacements} info]
     (= (replace replacements v)
        (transduce             (replace    replacements)  conj         v)
        (single/transduce-kv   (replace-kv replacements) tconj         v)
        (multi/transduce     n (replace-kv replacements) tconj concatv v)
        (multi/transduce-kv  n (replace-kv replacements) tconj concatv v)))))


(clj-test/defspec
  test-replace-kv
  n-checks
  replace-kv-properties)



;;;; stateful transducers-kv


(def dupe-gen
  (gen/let [n gen/nat]
    (gen/fmap #(repeat n %) gen/small-integer)))


(def gen-vec-of-dupes
  (gen/fmap (fn [s] (-> s flatten vec)) (gen/vector dupe-gen)))


(def dedupe-kv-properties
  (prop/for-all
   [v gen-vec-of-dupes]
   (= (dedupe v)
      (transduce           (dedupe)     conj v)
      (single/transduce-kv (dedupe-kv) tconj v))))


(clj-test/defspec
  test-dedupe-kv
  n-checks
  dedupe-kv-properties)


(def non-distinct-gen
  (gen/fmap shuffle gen-vec-of-dupes))


(def distinct-kv-properties
  (prop/for-all
   [v non-distinct-gen]
   (= (distinct v)
      (transduce           (distinct)     conj v)
      (single/transduce-kv (distinct-kv) tconj v))))


(clj-test/defspec
  test-distinct-kv
  n-checks
  distinct-kv-properties)


(def drop-kv-properties
  (prop/for-all
   [v (gen/vector gen/small-integer)
    n gen/nat]
   (= (drop n v)
      (transduce           (drop    n)  conj v)
      (single/transduce-kv (drop-kv n) tconj v))))


(clj-test/defspec
  test-drop-kv
  n-checks
  drop-kv-properties)


(def drop-while-kv-properties
  (prop/for-all
   [v (gen/vector gen/small-integer)
    limit gen/nat]
   (let [f (fn lim
             ([i] (<= i limit))
             ([_ i] (lim i)))]
     (= (drop-while f v)
        (transduce           (drop-while    f)  conj v)
        (single/transduce-kv (drop-while-kv f) tconj v)))))


(clj-test/defspec
  test-drop-while
  n-checks
  drop-while-kv-properties)


(def interpose-kv-properties
  (prop/for-all
   [v (gen/vector gen/small-integer)
    kw gen/keyword-ns]
   (= (interpose kw v)
      (transduce           (interpose    kw)  conj v)
      (single/transduce-kv (interpose-kv kw) tconj v))))


(clj-test/defspec
  test-interpose-kv
  n-checks
  interpose-kv-properties)


(def partition-all-kv-properties
  (prop/for-all
   [v (gen/vector gen/small-integer)
    n (gen/such-that #(not= % 0) gen/nat)]
   (= (partition-all n v)
      (transduce           (partition-all    n ) conj v)
      (single/transduce-kv (partition-all-kv n) tconj v))))


(clj-test/defspec
  test-partition-all-kv
  n-checks
  partition-all-kv-properties)


(def partition-by-kv-properties
  (prop/for-all
   [v (gen/vector gen/small-integer)]
   (let [f (fn ev?
             ([x] (even? x))
             ([_ x] (ev? x)))]
     (= (partition-by f v)
        (transduce           (partition-by    f)  conj v)
        (single/transduce-kv (partition-by-kv f) tconj v)))))


(clj-test/defspec
  test-partition-by-kv
  n-checks
  partition-by-kv-properties)


(def take-kv-properties
  (prop/for-all
   [v (gen/vector gen/small-integer)
    n gen/nat]
   (= (take n v)
      (transduce           (take    n)  conj v)
      (single/transduce-kv (take-kv n) tconj v))))


(clj-test/defspec
  test-take-kv
  n-checks
  take-kv-properties)


(def take-nth-kv-properties
  (prop/for-all
   [v (gen/vector gen/small-integer)
    n (gen/choose 1 8)]
   (= (take-nth n v)
      (transduce           (take-nth    n)  conj v)
      (single/transduce-kv (take-nth-kv n) tconj v))))


(clj-test/defspec
  test-take-nth-kv
  n-checks
  take-nth-kv-properties)


(def take-while-kv-properties
  (prop/for-all
   [v (gen/vector gen/small-integer)
    limit gen/nat]
   (let [f (fn lim
             ([x] (<= x limit))
             ([_ x] (lim x)))]
     (= (take-while f v)
        (transduce           (take-while    f)  conj v)
        (single/transduce-kv (take-while-kv f) tconj v)))))


(clj-test/defspec
  test-take-while-kv
  n-checks
  take-while-kv-properties)


#_(run-tests)

