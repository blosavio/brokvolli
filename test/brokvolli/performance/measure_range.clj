(ns brokvolli.performance.measure-range
  "Measure memory and time requirements for creating various kinds range
  sequentials, e.g., persistent vectors, primitive vectors, Java arrays, etc,
  constructed by various tactics.

  Normally, we might not want to optimize to this level of detail, but this a
  very specific case with well-defined properties (i.e., not generic):

  * A sequential of longs.
  * Fully realized.
  * Fast creation, but don't necessarily need fast random-access.
  * Efficient size.
  * Used immediately, then discarded."
  (:require
   [brokvolli.performance.create-range :refer :all]
   [brokvolli.performance.measure-utilities :refer :all]
   [clojure.java.io :as io]
   [clojure.math :as math]
   [clojure.pprint :as pp]
   [clojure.set :as set]
   [clojure.string :as str]
   [clj-memory-meter.core :as mm]
   [criterium.core :as crit]))


(def max-length-pow-10 6)


(def output-dir "resources/performance_data/")


(def memory-filename (str output-dir "range_memory_usage.edn"))


(def time-filename (str output-dir "range_time_benchmarks.edn"))


(def range-fns [long-range
                ;; range-range ;; runs out of Java heap space with `n` 1E6
                vector-range-1
                vector-range-2
                transducer-range
                transient-range
                long-array-range
                vec-range])


#_(do-memory-measurements range-fns max-length-pow-10 memory-filename)
#_(do-benchmarks range-fns max-length-pow-10 time-filename :lightning)


(defn do-measurements
  "Do memory and timing measurements. Criterium benchmarks will be run with
  `thouroughness`, one of `:default`, `:quick`, or `:lightning`.

  From the REPL:
  ```clojure
  (do-measurements :lightning)
  ```

  From command line:
  ```bash
  $ lein run -m brokvolli.performance.measure-range/do-measurements :quick
  ```

  From command line, with CPU affinity pinned:
  ```bash
  $ taskset --cpu-list 3 lein with-profiles default,benchmark run -m brokvolli.performance.measure-range/do-measurements :default
  ```"
  {:UUIDv4 #uuid "9f688119-6ce0-4693-86dc-46384ec4d772"}
  ([] (do-measurements :default))
  ([thoroughness]
   (do
     (do-memory-measurements range-fns
                             max-length-pow-10
                             memory-filename)
     (do-benchmarks range-fns
                    max-length-pow-10
                    time-filename
                    (cond
                      (keyword? thoroughness) thoroughness
                      (string? thoroughness) (keyword thoroughness)))
     (when (not *repl*)
       (shutdown-agents)))))


#_(do-measurements :lightning)

