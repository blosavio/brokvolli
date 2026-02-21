(ns readmoi-generator
  "Generate project ReadMe.

  From emacs/CIDER, eval buffer C-c C-k generates an html page and a markdown
  chunk.

  From command line:
  ```bash
  $ lein run -m readmoi-generator
  ```"
  {:no-doc true}
  (:require
   [clojure.string :as str]
   [hiccup2.core :refer [raw]]
   [readmoi.core :refer [*project-group*
                         *project-name*
                         *project-version*
                         *wrap-at*
                         print-form-then-eval]]))


(defn adjust-hiccup-str
  "Given a hiccup `[:code ...]` block `h`, insert newlines and `n` spaces after
  occurences of `regexes`, a sequence of regular expressions or strings."
  {:UUIDv4 #uuid "dffb3b9b-4b40-48f2-9748-257130f1e7c4"
   :no-doc true}
  [h regexes n]
  (update h 1 (fn [hiccup] (reduce #(str/replace %1 %2 (str %2 "\n" (str/join "" (repeat n " ")))) hiccup regexes))))


(readmoi.core/-main)

