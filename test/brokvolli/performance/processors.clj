(ns brokvolli.performance.processors
  "Benchmark brokvolli's multi-threaded transduce with different numbers of
  CPUs, pinned externally with Linux `taskset`."
  (:require
   [brokvolli.core :refer [tconj concatv]]
   [brokvolli.single :as single]
   [brokvolli.multi :as multi]
   [brokvolli.transducers-kv :refer [filter-kv
                                     map-kv
                                     remove-kv]]
   [clojure.math :as math]
   [clojure.core.reducers :as r]
   [clojure.string :as str]
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


(def transductions-benchmark-options-filename "./resources/processors_options.edn")
(def partition-at 500)
(def max-power 5)


(def vecs
  (reduce (fn [m n] (assoc m n (into [] (repeatedly n #(rand)))))
          {}
          (range-pow-10 max-power)))


(def max-mapper 6)


(defn mapper
  [n]
  (mapv #(assoc {}
                :cbrt (math/cbrt (* % n))
                :ceil (math/ceil (* % n))
                :cos (math/cos (* % n))
                :dec (dec (* % n))
                :e-to-n (math/pow math/E (* % n))
                :exp (math/exp (* % n))
                :floor (math/floor (* % n))
                :inc (inc (* % n))
                :log (math/log (abs (* % n)))
                :log10 (math/log10 (abs (* % n)))
                :n (* % n)
                :n-pi (* math/PI (* % n))
                :neg (- (* % n))
                :pow (math/pow (* % n) 3)
                :round (math/round (* % n))
                :sin (math/sin (* % n))
                :sqrt (math/sqrt (abs (* % n)))
                :tan (math/tan (* % n)))
        (range 1 max-mapper)))


(def xform-1 (map-kv mapper))


(defbench
  processors-1
  "Ninety mathematical operations per element"
  (fn [n] (multi/transduce partition-at concatv xform-1 tconj (vecs n)))
  (range-pow-10 max-power))


#_(run-one-defined-benchmark processors-1 :lightning)
#_(run-benchmarks transductions-benchmark-options-filename)
#_(generate-documents transductions-benchmark-options-filename)

