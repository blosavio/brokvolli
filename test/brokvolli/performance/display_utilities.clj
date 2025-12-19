(ns brokvolli.performance.display-utilities
  "Utilities for generating html and markdown files displaying results of memory
  and time measurements."
  (:require
   [brokvolli.performance.measure-utilities :refer [pow-10]]
   [clojure.edn :as edn]
   [clojure.math :as math]
   [clojure.string :as str]
   [com.hypirion.clj-xchart :as xc]
   [hiccup2.core :as h2]
   [hiccup.element :as element]
   [hiccup.page :as page]
   [readmoi.core :refer [copyright
                         page-template
                         short-date
                         tidy-html-body
                         tidy-html-document]]))


(defn load-memory-measurements
  "Reads memory data from file."
  {:UUIDv4 #uuid "a34d29d2-8a16-41e1-b1b8-b362836fc55d"}
  [fname]
  (edn/read-string (slurp fname)))


(defn cleanup-fn-strs
  "1. Trim ns from function names
  2. Replace `_` with `-` in function names"
  {:UUIDv4 #uuid "2cbde96c-f15a-445a-a938-6bb244210b41"}
  [data]
  (-> data
      (update-keys #(str/replace % #"^[\w\d\.]*\/" ""))
      (update-keys #(str/replace % #"_" "-"))))


(defn adjust-memory-data
  "Re-arrange data so that lengths are assoc-ed to `:x` and memory sizes are
  assoc-ed to `:y`."
  [data]
  (reduce-kv (fn [acc k v] (assoc acc k {:x (keys v)
                                         :y (vals v)}))
             {}
             data))


(def ^{:no-doc true}
  default-chart-style-dosctring
  "A hashmap containing `clj-xchart` chart options, governing theme, legend
position, plot border, etc.")


(def ^{:doc default-chart-style-dosctring}
  default-chart-style
  {:theme :xchart
   :chart {:background-color :white
           :title {:box {:visible? false}}}
   :legend {:position :outside-e
            :border-color :white}
   :plot {:border-visible? false
          :border-color :white}
   :error-bars-color :match-series})


(defn memory-chart
  "1. Writes an svg image to file containing an xchart.
  2. Returns a hiccup/html image element"
  {:UUIDv4 #uuid "a7e8dff0-271c-4bd6-91bb-6e56229236ab"}
  [data fname]
  (let [short-img-filename (str/replace fname #"doc/" "")
        img-alt-text "Memory usage vs ranged sequential lengths, comparing different construction tactics."]
    (do
      (xc/spit
       (xc/xy-chart
        data
        (merge default-chart-style
               {:title "Memory usage vs ranged sequential length"
                :x-axis {:title "range length"
                         :logarithmic? true}
                :y-axis {:title "memory size (bytes)"
                         :logarithmic? true}}))
       fname)
      (element/image short-img-filename img-alt-text))))


(defn memory-table
  "Returns a hiccup/html table element with memory usage data."
  {:UUIDv4 #uuid "9da1c788-12c5-4941-9b28-7f56e7adeb2c"}
  [data max-len]
  (let [shell [:table
               [:caption "size in bytes"]
               [:tr [:td] [:th {:colspan (count (pow-10 max-len))} "range length"]]
               (into
                [:tr [:th "constructor"]]
                (mapv #(vector :th %) (pow-10 max-len)))]
        red-fn (fn [acc k v] (conj acc (into [:tr [:td k]] (mapv #(vector :td %) (:y v)))))]
    (reduce-kv red-fn shell data)))


(defn load-timing-measurements
  "Returns a hash-map of benchmark data."
  {:UUIDv4 #uuid "26c56fbd-223f-4ad7-83ca-ba8070c1fce1"
   :no-doc true}
  [fname]
  (let [read-opts {:readers {'criterium.core.OutlierCount identity}}
        custom-read-string #(clojure.edn/read-string read-opts %)]
    (-> fname slurp custom-read-string)))


(defn filter-timing-data-one
  "Given data, a function string `fn-str`, and max size 10^`max-len`, pulls out
  the timing means and
  variances."
  [data fn-str max-len]
  (reduce (fn [acc v]
            (-> acc
                (assoc-in [fn-str :y v] (first (get-in data [fn-str v :mean])))
                (assoc-in [fn-str :err v] (first (get-in data [fn-str v :variance])))))
          {}
          (pow-10 max-len)))


(defn filter-timing-data
  "Given a sequential of function strings `fn-strs`, pulls out timing data into
  a hash-map."
  {:UUIDv4 #uuid "ed8d011a-4daf-4ba9-9500-de167c1b0352"}
  [data max-len]
  (let [fn-strs (keys data)
        red-fn (fn [acc this-fn] (merge acc (filter-timing-data-one data this-fn max-len)))]
    (reduce red-fn {} fn-strs)))


(defn sort-internal-hashmaps
  "Ensure all internal hash-maps are ordered so that later, when they're teased
  apart into sequences, the values are in monotonically-increasing order."
  {:UUIDv4 #uuid "be1da667-511a-4225-8925-698b42b0e610"}
  [data]
  (-> data
      (update-vals (fn [m] (update m :y #(into (sorted-map) %))))
      (update-vals (fn [m] (update m :err #(into (sorted-map) %))))))


(defn arrange-timing-data
  "1. Collect lengths into `:x`
  2. Collect mean times into `:y`
  3. Collect variances into `:var`
  4. Collect stdev into `:std`."
  [data]
  (reduce-kv (fn [acc k v] (assoc acc k {:x (keys (:y v))
                                         :y (vals (:y v))
                                         :var (vals (:err v))
                                         :std (map math/sqrt (vals (:err v)))}))
             {}
             data))


(defn timings-chart
  "1. Writes an svg image to file containing an xchart.
  2. Returns a hiccup/html image element"
  {:UUIDv4 #uuid "a7e8dff0-271c-4bd6-91bb-6e56229236ab"}
  [data fname]
  (let [short-img-filename (str/replace fname #"doc/" "")
        img-alt-text "XY chart of benchmark timings vs sequential lengths."]
    (do
      (xc/spit
       (xc/xy-chart
        data
        (merge default-chart-style
               {:title "Benchmark timings vs range sequential length"
                :x-axis {:title "range length"
                         :logarithmic? true}
                :y-axis {:title "execution time (sec)"
                         :logarithmic? true}}))
       fname)
      (element/image short-img-filename img-alt-text))))


(defn timings-table
  "Returns a hiccup/html table element with benchmark timings data."
  {:UUIDv4 #uuid "788257b5-dd1c-4369-ae17-37ead86c56e2"}
  [data max-len]
  (let [shell [:table
               [:caption "times in seconds, " [:em "mean±std"]]
               [:tr [:td] [:th {:colspan (count (pow-10 max-len))} "range length"]]
               (into
                [:tr [:th "constructor"]]
                (mapv #(vector :th %) (pow-10 max-len)))]
        fmt #(format "%1.1e" %)
        red-fn (fn [acc k v] (conj acc (into [:tr [:td k]]
                                             (mapv #(vector :td
                                                            (fmt %1)
                                                            "±"
                                                            (fmt %2))
                                                   (:y v)
                                                   (:std v)))))]
    (reduce-kv red-fn shell data)))

