(ns brokvolli.performance.probe-memory-limits
  "In the context of benchmarking Brokvolli's various transducing functions,
  I previously encountered Java memory errors with collections containing about
  one million elements.

  This namespace systematically probes the memory limits of the machine, and
  tests possible Java runtime settings (i.e., -Xmx) to mitigate. It mimics the
  partitioning benchmarks.

  Observed results
  * desktop computer, `:lightning` benchmark thoroughness
  1E5 okay
  1E6 maxes CPUS, memory, and swap for several minutes, errors out with
      `*** Closed on Tue Feb 24 10:52:43 2026 ***`, killing the CIDER/nREPL
  session.

  * desktop computer, `:quick` benchmark thoroughness
  1E5 okay
  1E6 `Error printing return value (OutOfMemoryError) at clojure.lang.PersistentHashMap$BitmapIndexedNode/assoc (PersistentHashMap.java:890). Java heap space`

  * desktop computer, `:default` benchmark throughness
  1E4 okay
  1E5 `Unhandled clojure.lang.ExceptionInfo; Caused by java.lang.OutOfMemoryError`

  Watching the real-time resource statistics suggests that the machine is
  merely running out of resources.

  https://docs.oracle.com/javase/8/docs/technotes/guides/troubleshoot/memleaks002.html"
  (:require
   [brokvolli.core :refer [tconj concatv]]
   [brokvolli.multi :as multi]
   [brokvolli.transducers-kv :refer [map-kv]]
   [clojure.math :as math]
   [fastester.measure :refer [range-pow-10
                              run-manual-benchmark]]))



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


(defn -main
  "Given vector `length` (integer) and Criterium benchmark `thoroughness` (one
  of `:default`, `:quick`, or `:lightning`, runs a benchmark to test if Java
  can handle evaluation on this machine under current run-time conditions.

  See also bash script `run_mem_probe`."
  {:UUIDv4 #uuid "bd86e894-3310-49f3-a83b-1548ebb29914"}
  [length thoroughness]
  (let [_ (println "thoroughness:" thoroughness ", vec length:" length)
        n (parse-long length)
        V (vec (repeatedly n #(rand)))
        msg #(println "vec length" n "completed successfully.")]
    (run-manual-benchmark
     (fn [v] (multi/transduce 100 concatv xform-1 tconj v))
     V
     (keyword thoroughness))
    (msg)))


#_(-main "10" ":lightning")

