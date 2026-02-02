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
;; introduces new contexts, not new transducers, so let's see if we can get away
;; with simpler property tests.

;; Only numbers and strings have interesting operations (`inc`, `upper-case`,
;; etc.), so only generate collections containing those scalar types. Don't
;; exercise every off-the-shelf transducer, perhaps only `map`, `filter`,
;; `remove`, `take`, `drop`, and `mapcat`.


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


(comment ;; sketch property tests for reduce-kv type processes

  ;; mapping (reduce+conj)
  (reduce-kv (fn [acc k v] (conj acc (inc v))) [] [11 22 33])
  (single/transduce-kv (map-kv (fn [_ x] (inc x))) tconj [11 22 33])

  ;; filtering
  (reduce-kv (fn [acc k v] (if (= 0 (rem v 3)) (conj acc v) acc)) [] [11 22 33 44 55 66 77 88 99])
  (single/transduce-kv (filter-kv (fn [_ x] (= 0 (rem x 3)))) tconj [11 22 33 44 55 66 77 88 99])

  ;; remove-ing
  (reduce-kv (fn [acc k v] (if (not= k 5) (conj acc v) acc)) [] [11 22 33 44 55 66 77 88 99])
  (single/transduce-kv (remove-kv (fn [keydex _] (= keydex 5))) tconj [11 22 33 44 55 66 77 88 99])


  (defn mappers
    [f coll]
    {:reduce-kv (reduce-kv (fn [acc k v] (conj acc (f v))) [] coll)
     :multi-transduce (multi/transduce (map inc) conj coll)
     :single-transduce-kv (single/transduce-kv (map-kv (fn [_ x] (f x))) tconj coll)
     :multi-transduce-kv (multi/transduce-kv 3 (map-kv (fn [_ x] (f x))) tconj core/concatv coll)})

  (mappers inc [11 22 33])

  )



#_(run-tests)

