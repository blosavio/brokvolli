(ns brokvolli.performance.deep
  "Benchmark brokvolli's transduce/tranduce-kv with a single, deep task per
  element."
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


(def transductions-benchmark-options-filename "./resources/deep_options.edn")
(def max-power 5) ;; must be <=5 or Java throws OutOfMemoryError with `:quick` thoroughness


(def vecs
  (reduce (fn [m n] (assoc m n (vec (repeatedly n #(rand)))))
          {}
          (range-pow-10 max-power)))


(defn mapper
  ([_ n] (mapper n))
  ([n]
   {:cbrt (math/cbrt n)
    :ceil (math/ceil n)
    :cos (math/cos n)
    :dec (dec n)
    :e-to-n (math/pow math/E n)
    :exp (math/exp n)
    :floor (math/floor n)
    :inc (inc n)
    :log (math/log (abs n))
    :log10 (math/log10 (abs n))
    :n n
    :n-pi (* math/PI n)
    :neg (- n)
    :pow (math/pow n 3)
    :round (math/round n)
    :sin (math/sin n)
    :sqrt (math/sqrt (abs n))
    :tan (math/tan n)}))


(def xform-1 (map-kv mapper))


(def tactics-1
  {"reduce"              (fn [v] (reduce    (fn [acc   x] (conj acc (mapper x))) [] v))
   "reduce-kv"           (fn [v] (reduce-kv (fn [acc _ x] (conj acc (mapper x))) [] v))
   "fold"                (fn [v] (r/fold concatv conj (r/map mapper v)))
   "core-transduce"      (fn [v] (transduce           xform-1  conj         v))
   "single-transduce-kv" (fn [v] (single/transduce-kv xform-1 tconj         v))
   "multi-transduce"     (fn [v] (multi/transduce     xform-1 tconj concatv v))
   "multi-transduce-kv"  (fn [v] (multi/transduce-kv  xform-1 tconj concatv v))})


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
    (vec (range 0 1E3))))


#_(run-test verify-tactics-1)


(defbench
  deep-1
  "Construct hashmap of fifteen mathmetical ops, per element"
  (fn [n] ((tactics-1 (project-version-lein)) (vecs n)))
  (range-pow-10 max-power))


(comment ;; sanity check
  (set! *print-length* 101)

  ((tactics-1 "core-transduce") (vecs 10))
  )


#_(run-one-defined-benchmark deep-1 :lightning)
#_(run-benchmarks transductions-benchmark-options-filename)
#_(generate-documents transductions-benchmark-options-filename)

