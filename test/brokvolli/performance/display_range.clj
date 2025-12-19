(ns brokvolli.performance.display-range
  "Display results of memory and time measurements of creating various kinds of
  range sequentials."
  (:require
   [brokvolli.performance.display-utilities :refer :all]
   [brokvolli.performance.measure-range :refer [memory-filename
                                                output-dir
                                                max-length-pow-10
                                                range-fns
                                                time-filename]]
   [hiccup2.core :as h2]
   [readmoi.core :refer [copyright
                         page-template
                         short-date
                         tidy-html-body
                         tidy-html-document]]))


(def img-dir "doc/img/")
(def memory-chart-filename (str img-dir "range_memory.svg"))
(def timing-chart-filename (str img-dir "range_timings.svg"))

(def html-directory "doc/")
(def html-filename "range_constructors.html")
(def markdown-directory "doc/")
(def markdown-filename "range_constructors.md")

(def page-title "Memory and timing measurements of various range sequentials")
(def page-copyright-holder "Brad Losavio")
(def page-uuid #uuid "0daf0dc7-b80c-4abd-a5d9-41ce4bf5eccf")


(def memory-data (-> memory-filename
                     load-memory-measurements
                     cleanup-fn-strs
                     adjust-memory-data))


(def timing-data (-> time-filename
                     load-timing-measurements
                     cleanup-fn-strs
                     (filter-timing-data max-length-pow-10)
                     sort-internal-hashmaps
                     arrange-timing-data))


(load-file "resources/subsections/ranges_subsections.clj")


(defn generate-html
  "Writes html to the filesystem, suitable for a web browser."
  {:UUIDv4 #uuid "c5cf8358-4c37-4388-b77c-28d49a4825fc"
   :no-doc true}
  []
  (let [fname (str html-directory html-filename)]
    (spit fname
          (-> (page-template
               page-title
               page-uuid
               (conj [:body
                      [:h1 page-title]
                      page-abstract
                      memory-subsection-preamble
                      (memory-chart memory-data memory-chart-filename)
                      (memory-table memory-data max-length-pow-10)
                      benchmark-subsection-preamble
                      (timings-chart timing-data timing-chart-filename)
                      (timings-table timing-data max-length-pow-10)])
               page-copyright-holder
               [:a {:href "https://github.com/blosavio/ReadMoi"} "ReadMoi"])))
    (tidy-html-document fname)))


(defn generate-markdown
  "Generates a markdown file, similar to [[generate-html]]."
  {:UUIDv4 #uuid "54d40cf3-0728-4bea-bdd4-9839f79601d6"
   :no-doc true}
  []
  (let [fname (str markdown-directory markdown-filename)]
    (spit fname
          (h2/html
           (vec (-> [:body
                     [:h1 page-title]
                     page-abstract
                     memory-subsection-preamble
                     (memory-chart memory-data memory-chart-filename)
                     (memory-table memory-data max-length-pow-10)
                     benchmark-subsection-preamble
                     (timings-chart timing-data timing-chart-filename)
                     (timings-table timing-data max-length-pow-10)]
                    (conj [:p#page-footer
                           (copyright page-copyright-holder)
                           [:br]
                           "Compiled by "
                           [:a {:href "https://github.com/blosavio/readmoi"}
                            "ReadMoi"]
                           " on "
                           (short-date)
                           " ."
                           [:span#uuid [:br]
                            page-uuid]])))))
    (tidy-html-body fname)))


(defn generate-documents
  "Generate both an html and markdown document.

  From command line:
  ```bash
  $ lein run -m brokvolli.performance.display-range/generate-documents
  ```"
  {:UUIDv4 #uuid "298652f8-387a-449d-bed3-d33e8f1f0a5e"}
  []
  (do
    (generate-html)
    (generate-markdown)
    (when (not *repl*)
      (shutdown-agents))))


#_(generate-documents)

