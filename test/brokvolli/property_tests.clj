(ns brokvolli.property-tests
  "Property tests for brokvolli's `transduce` variants.

  See also `brokvolli.serial-property-tests-amiller` for adaptations of Alex
  Miller's transducers property tests."
  (:require
   [brokvolli.serial :as serial]
   [clojure.string :as str]
   [clojure.test.check :as chk]
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


(def num-alts #{inc dec -})
(def num-preds #{even? odd?})

(def str-cutoff 3) ;; avoid catching too many and collapsing the coll to empty
(def str-alts #{str/upper-case str/lower-case str/capitalize})
(def str-preds #{#(<= (count %) str-cutoff)
                 #(< str-cutoff (count %))})


(def max-shortening-xforms 3) ;; don't use too many shortening xforms
(def min-xforms 1)
(def max-xforms 8)


(def gen-vec-xforms
  (gen/let
      [element-type (gen/elements [:nums
                                   :strings])
       size gen/nat
       v (case element-type
           :nums (gen/vector gen/nat size)
           :strings (gen/vector gen/string-ascii size))
       xforms-shortening (gen/vector
                          (gen/elements [(take (int (* 0.9 size))) ;; would prefer to make `n` inside `take` and `drop` an integer generator, but...
                                         (drop (int (* 0.1 size)))]) ;; ... evaling the form swallows the generator, masking it from `prop/for-all`
                          0 max-shortening-xforms)
       xforms-expanding (gen/vector
                         (gen/elements [(mapcat #(repeat 3 %))]) ;; ... same as above for `n` within `repeat` expression
                         0 max-xforms)
       xforms-altering (gen/vector
                        (case element-type
                          :nums (gen/elements (concat
                                               (transducerize map num-alts)
                                               (transducerize filter num-preds)))
                          :strings (gen/elements (concat
                                                  (transducerize map str-alts)
                                                  (transducerize filter str-preds))))
                        min-xforms max-xforms)
       xforms (gen/vector (gen/elements (concat xforms-shortening
                                                xforms-expanding
                                                xforms-altering))
                          min-xforms max-xforms)]
    {:type element-type
     :v v
     :xforms xforms}))


#_(gen/sample gen-vec-xforms)


#_(let [{type :type
         v :v
         xforms :xforms} (gen/generate gen-vec-xforms)]
    (= (transduce (apply comp xforms) conj v)
       (serial/transduce-kv (apply serial/comp-kv xforms) conj v)))


(def sequential-properties
  (prop/for-all [info gen-vec-xforms]
                (let [{element-type :type
                       v :v
                       xforms :xforms} info]
                  (= (transduce (apply comp xforms) conj v)
                     (serial/transduce-kv (apply serial/comp-kv xforms) conj v)))))


(chk/quick-check 1000 sequential-properties)




;; Sketch -kv property tests

;; ops on numbers: inc, dec, -

(serial/transduce-kv (comp-kv (map inc))
                     (completing
                      (fn
                        ([] {})
                        ([result value] (assoc result serial/*keydex* value))))
                     {:a 11 :b 22 :c 33})

(transduce (comp (map #(update % 1 inc)))
           (completing
            (fn
              ([] {})
              ([result [k v]] (assoc result k v))))
           {:a 11 :b 22 :c 33})


;; ops on strings: upper-case, lower-case, capitalize

(serial/transduce-kv (serial/comp-kv (map str/upper-case))
                     (completing
                      (fn
                        ([] {})
                        ([result value] (assoc result serial/*keydex* value))))
                     {:a "a" :b "bc" :c "def"})

(transduce (comp (map #(update % 1 str/upper-case)))
           (completing
            (fn
              ([] {})
              ([result [k v]] (assoc result k v))))
           {:a "a" :b "bc" :c "def"})


;; get shorter: filter, remove, take, drop

(serial/transduce-kv (serial/comp-kv (filter even?))
                     (completing
                      (fn
                        ([] {})
                        ([result value] (assoc result serial/*keydex* value))))
                     {:a 11 :b 22 :c 33})

(transduce (comp (filter #(even? (% 1))))
           (completing
            (fn
              ([] {})
              ([result [k v]] (assoc result k v))))
           {:a 11 :b 22 :c 33})


(serial/transduce-kv (serial/comp-kv (filter #(<= (count %) 3)))
                     (completing
                      (fn
                        ([] {})
                        ([result value] (assoc result serial/*keydex* value))))
                     {:a "a"
                      :b "bc"
                      :c "def"
                      :d "ghij"
                      :e "klmno"})

(transduce (comp (filter #(<= (count (% 1)) 3)))
           (completing
            (fn
              ([] {})
              ([result [k v]] (assoc result k v))))
           {:a "a"
            :b "bc"
            :c "def"
            :d "ghij"
            :e "klmno"})

