[:section#setup
 [:h2 "Setup"]
 
 [:h3 "Leiningen/Boot"]
 
 [:pre [:code (str "["
                   *project-group*
                   "/"
                   *project-name*
                   " \""
                   *project-version*
                   "\"]")]]
 
 [:h3 "Clojure CLI/deps.edn"]
 
 [:pre [:code (str *project-group*
                   "/"
                   *project-name*
                   " {:mvn/version \""
                   *project-version*
                   "\"}")]]
 
 [:h3 "Require"]
 
 [:pre (print-form-then-eval
        "(require '[brokvolli.core :refer [concatv tassoc]]
                  '[brokvolli.single :refer [transduce-kv]]
                  '[brokvolli.transducers-kv :refer [map-kv filter-kv replace-kv]])")]]

