(ns brokvolli.performance.transductions
  "Benchmark brokvolli's transduce/tranduce-kv in various situations."
  (:require
   [brokvolli.core :refer [tconj concatv]]
   [brokvolli.single :as single]
   [brokvolli.multi :as multi]
   [brokvolli.transducers-kv :refer [filter-kv
                                     map-kv
                                     remove-kv]]
   [clojure.math :as math]
   [clojure.core.reducers :as r]
   [clojure.test :refer [are
                         deftest
                         is
                         run-test
                         run-tests
                         testing]]
   [fastester.define :refer [defbench]]
   [fastester.display :refer [generate-documents]]
   [fastester.measure :refer [range-pow-10
                              run-benchmarks
                              run-one-defined-benchmark]]
   [fastester.options :refer [project-version-lein]]))


(def transductions-benchmark-options-filename "./resources/transductions_options.edn")
(def max-power 6) ;; must be <=6 or Java throws OutOfMemoryError with `:default` thoroughness


(def vecs
  (reduce (fn [m n] (assoc m n (vec (repeatedly n #(rand)))))
          {}
          (range-pow-10 max-power)))


(defn inc-er
  "Given `delta`, returns a function that increments number `n`. If a leading
  arg is provided (presumably the keydex), it is ignored."
  {:UUIDv4 #uuid "07273fad-fc52-4bdb-86ea-3ac6954d1ada"
   :no-doc true}
  [delta]
  (fn
    ([n]   (+ delta n))
    ([_ n] (+ delta n))))


(def xform-1 (map-kv (inc-er 1.0)))


(def tactics-1
  (let [inc-1_0 (inc-er 1.0)]
    {"reduce"              (fn [v] (reduce    (fn [acc   x] (conj acc (inc-1_0 x))) [] v))
     "reduce-kv"           (fn [v] (reduce-kv (fn [acc _ x] (conj acc (inc-1_0 x))) [] v))
     "fold"                (fn [v] (r/fold concatv conj (r/map inc-1_0 v)))
     "core-transduce"      (fn [v] (transduce                   xform-1  conj v))
     "single-transduce-kv" (fn [v] (single/transduce-kv         xform-1 tconj v))
     "multi-transduce"     (fn [v] (multi/transduce     concatv xform-1 tconj v))
     "multi-transduce-kv"  (fn [v] (multi/transduce-kv  concatv xform-1 tconj v))}))


(deftest verify-tactics-1
  (are [v] (= ((tactics-1 "reduce")              v)
              ((tactics-1 "reduce-kv")           v)
              ((tactics-1 "fold")                v)
              ((tactics-1 "core-transduce")      v)
              ((tactics-1 "single-transduce-kv") v)
              ((tactics-1 "multi-transduce")     v)
              ((tactics-1 "multi-transduce-kv" ) v))
    []
    [11 22 33]
    (vec (range 0 1E6))))


#_(run-test verify-tactics-1)


(defbench
  transductions-1
  "01 transform: increment integer"
  (fn [n] ((tactics-1 (project-version-lein)) (vecs n)))
  (range-pow-10 max-power))


#_(run-one-defined-benchmark transductions-1 :lightning)


(defn less-er
  "Given `cutoff`, returns a function that returns `true` if number `n` is
  less-than-or-equal-to `cutoff`. If two args are supplied, ignores first and
  compares second."
  {:UUIDv4 #uuid "e033b16e-cb25-4225-82f3-f530fd5cf717"
   :no-doc true}
  [cutoff]
  (fn
    ([n]   (<= n cutoff))
    ([_ n] (<= n cutoff))))


(defn more-er
  "Given `cutoff`, returns a function that returns `true` if number `n` is
  more-than-or-equal-to `cutoff`. If two args are supplied, ignores first and
  compares second."
  {:UUIDv4 #uuid "0f7614a6-54bb-4a8a-be0e-7a7431505e5d"
   :no-doc true}
  [cutoff]
  (fn
    ([n]   (<= cutoff n))
    ([_ n] (<= cutoff n))))


(def xform-2 (comp (map-kv    (inc-er  1.0))
                   (filter-kv (less-er 1.98))
                   (remove-kv (more-er 1.96))))


(def fold-em-2 #(r/remove (more-er 1.96)
                          (r/filter (less-er 1.98)
                                    (r/map (inc-er 1.0) %))))


(def tactics-2
  {"naive-seq"           (fn [v] (->> v
                                      (map (inc-er 1.0))
                                      (filter (less-er 1.98))
                                      (remove (more-er 1.96))
                                      (doall)))
   "fold"                (fn [v] (r/fold concatv conj (fold-em-2 v)))
   "core-transduce"      (fn [v] (transduce                   xform-2  conj v))
   "single-transduce-kv" (fn [v] (single/transduce-kv         xform-2 tconj v))
   "multi-transduce"     (fn [v] (multi/transduce     concatv xform-2 tconj v))
   "multi-transduce-kv"  (fn [v] (multi/transduce-kv  concatv xform-2 tconj v))})


(deftest verify-tactics-2
  (are [v] (= ((tactics-2 "naive-seq")           v)
              ((tactics-2 "core-transduce")      v)
              ((tactics-2 "single-transduce-kv") v)
              ((tactics-2 "multi-transduce")     v)
              ((tactics-2 "multi-transduce-kv")  v))
    []
    [11 22 33]
    (vec (range 0 1E6))))


#_(run-test verify-tactics-2)


(defbench
  transductions-2
  "03 transforms: Transduce with `map`+`filter`+`remove`, 1X"
  (fn [n] ((tactics-2 (project-version-lein)) (vecs n)))
  (range-pow-10 max-power))


#_(run-one-defined-benchmark transductions-2 :lightning)


(def xform-3 (comp xform-2
                   (map-kv (inc-er 1.0))
                   (filter-kv (less-er 2.94))
                   (remove-kv (more-er 2.92))))


(def fold-em-3 #(r/remove (more-er 2.92)
                          (r/filter (less-er 2.94)
                                    (r/map (inc-er 1.0)
                                           (fold-em-2 %)))))


(def tactics-3
  {"fold"                (fn [v] (r/fold concatv conj (fold-em-3 v)))
   "core-transduce"      (fn [v] (transduce                   xform-3  conj v))
   "single-transduce-kv" (fn [v] (single/transduce-kv         xform-3 tconj v))
   "multi-transduce"     (fn [v] (multi/transduce     concatv xform-3 tconj v))
   "multi-transduce-kv"  (fn [v] (multi/transduce-kv  concatv xform-3 tconj v))})


(deftest verify-tactics-3
  (are [v] (= ((tactics-3 "core-transduce")      v)
              ((tactics-3 "single-transduce-kv") v)
              ((tactics-3 "multi-transduce")     v)
              ((tactics-3 "multi-transduce-kv")  v))
    []
    [11 22 33]
    (vec (range 0 1E6))))


#_(run-test verify-tactics-3)


(defbench
  transductions-3
  "06 transforms: Transduce with `map`+`filter`+`remove`, 2X"
  (fn [n] ((tactics-3 (project-version-lein)) (vecs n)))
  (range-pow-10 max-power))


#_(run-one-defined-benchmark transductions-3 :lightning)


(def xform-4 (comp xform-3
                   (map-kv (inc-er 1.0))
                   (filter-kv (less-er 3.90))
                   (remove-kv (more-er 3.88))
                   (map-kv (inc-er 1.0))
                   (filter-kv (less-er 4.86))
                   (remove-kv (more-er 4.84))))


(def fold-em-4 #(r/remove (more-er 4.84)
                          (r/filter (less-er 4.86)
                                    (r/map (inc-er 1.0)
                                           (r/remove (more-er 3.88)
                                                     (r/filter (less-er 3.90)
                                                               (r/map (inc-er 1.0)
                                                                      (fold-em-3 %))))))))

(def tactics-4
  {"fold"                (fn [v] (r/fold concatv conj (fold-em-4 v)))
   "core-transduce"      (fn [v] (transduce                   xform-4  conj v))
   "single-transduce-kv" (fn [v] (single/transduce-kv         xform-4 tconj v))
   "multi-transduce"     (fn [v] (multi/transduce     concatv xform-4 tconj v))
   "multi-transduce-kv"  (fn [v] (multi/transduce-kv  concatv xform-4 tconj v))})


(deftest verify-tactics-4
  (are [v] (= ((tactics-4 "core-transduce")      v)
              ((tactics-4 "single-transduce-kv") v)
              ((tactics-4 "multi-transduce")     v)
              ((tactics-4 "multi-transduce-kv")  v))
    []
    [11 22 33]
    (vec (range 0 1E6))))


#_(run-test verify-tactics-4)


(comment ;; sanity check

  (set! *print-length* 101)

  (let [t tactics-4
        f "fold" #_"core-transduce"]
    (->>
     ((t f) (vec (range 0.0 1.01 0.01)))
     (mapv #(clojure.math/round (* 100 %)))))
  )


(defbench
  transductions-4
  "12 transforms: Transduce with `map`+`filter`+`remove`, 4X"
  (fn [n] ((tactics-4 (project-version-lein)) (vecs n)))
  (range-pow-10 max-power))


#_(run-benchmarks transductions-benchmark-options-filename)
#_(generate-documents transductions-benchmark-options-filename)

