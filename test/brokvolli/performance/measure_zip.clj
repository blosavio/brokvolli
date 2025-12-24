(ns brokvolli.performance.measure-zip
  "Measure memory and time requirements for creating various kinds 'zipped'
  sequentials, i.e., a sequential of 2-tuples of `[idx val]`."
  (:require
   [brokvolli.performance.create-zipped-sequential :refer :all]
   [brokvolli.performance.measure-utilities :refer :all]
   ;;[clojure.java.io :as io]
   ;;[clojure.math :as math]
   ;;[clojure.pprint :as pp]
   ;;[clojure.set :as set]
   ;;[clojure.string :as str]
   ;;[clj-memory-meter.core :as mm]
   ;;[criterium.core :as crit]
   ))


(def output-dir "resources/performance_data/")
(def memory-filename (str output-dir "zipped_memory_usage.edn"))
(def time-filename (str output-dir "zipped_time_benchmarks.edn"))


(def zip-fns [mapv-zip
              mapv-entry-zip
              pmap-zip
              long-range-zip
              long-array-zip
              map-indexed-zip
              transduce-zip
              transient-loop-zip
              transient-first-next-zip])


(def max-length-pow-10 5)


;; pre-compute input range sequentials so their creation is not included in the
;; eval timings


(def index-sequentials (reduce #(assoc %1 %2 (range %2)) {} (pow-10 max-length-pow-10)))


;; Wrap each zipper function so that we only need a quick look-up to get an
;; costly arg instead of creating it anew each time.

#_(def zippered-fns (map (fn zip-with-lookup [f] #(f (index-sequentials %))) zip-fns))

;; Using an anonymous function discards the function symbol, so must do it manually.

(def mapv-zippered (fn [i] (mapv-zip (index-sequentials i))))
(def mapv-entry-zippered (fn [i] (mapv-entry-zip (index-sequentials i))))
(def pmap-zippered (fn [i] (pmap-zip (index-sequentials i))))
(def long-range-zippered (fn [i] (long-range-zip (index-sequentials i))))
(def long-array-zippered (fn [i] (long-array-zip (index-sequentials i))))
(def map-indexed-zippered (fn [i] (map-indexed-zip (index-sequentials i))))
(def transduce-zippered (fn [i] (transduce-zip (index-sequentials i))))
(def transient-loop-zippered (fn [i] (transient-loop-zip (index-sequentials i))))
(def transient-first-next-zippered (fn [i] (transient-first-next-zip (index-sequentials i))))


(def zippered-fns [mapv-zippered
                   mapv-entry-zippered
                   pmap-zippered
                   long-range-zippered
                   long-array-zippered
                   map-indexed-zippered
                   transduce-zippered
                   transient-loop-zippered
                   transient-first-next-zippered])


;; Need to alter `measure-memory-seq` -> `measure-memory-fn` to accept an arg
;; to insert instead of `pow-10`, so that input range can be pre-computed and
;; not included in memory and/or benchmark measurements.



#_(do-memory-measurements zippered-fns max-length-pow-10 memory-filename)
#_(do-benchmarks zippered-fns max-length-pow-10 time-filename :lightning)


(defn do-measurements
  "Do memory and timing measurements. Criterium benchmarks will be run with
  `thouroughness`, one of `:default`, `:quick`, or `:lightning`.

  From the REPL:
  ```clojure
  (do-measurements :lightning)
  ```

  From command line:
  ```bash
  $ lein run -m brokvolli.performance.measure-zip/do-measurements :quick
  ```

  From command line, with CPU affinity pinned:
  ```bash
  $ taskset --cpu-list 3 lein with-profiles default,benchmark run -m brokvolli.performance.measure-zip/do-measurements :default
  ```"
  {:UUIDv4 #uuid "cdf1e816-73ad-4ccc-b790-2783beb9adb6"}
  ([] (do-measurements :default))
  ([thoroughness]
   (do
     (do-memory-measurements zippered-fns
                             max-length-pow-10
                             memory-filename)
     (do-benchmarks zippered-fns
                    max-length-pow-10
                    time-filename
                    (cond
                      (keyword? thoroughness) thoroughness
                      (string? thoroughness) (keyword thoroughness)))
     (when (not *repl*)
       (shutdown-agents)))))


#_(do-measurements :lightning)

