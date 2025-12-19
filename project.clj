(defproject com.sagevisuals/brokvolli "0-SNAPSHOT0"
  :description "A Clojure library exploring parallel tranduce and transduce-kv"
  :url "https://github.com/blosavio/brokvolli"
  :license {:name "MIT License"
            :url "https://opensource.org/license/mit"
            :distribution :repo}
  :dependencies [[org.clojure/clojure "1.12.4"]]
  :repl-options {:init-ns brokvolli.core}
  :main brokvolli.core
  :plugins []
  :profiles {:dev {:dependencies [[com.clojure-goes-fast/clj-async-profiler "1.6.2"]
                                  [com.clojure-goes-fast/clj-memory-meter "0.4.0"]
                                  [com.hypirion/clj-xchart "0.2.0"]
                                  [com.sagevisuals/chlog "5"]
                                  [com.sagevisuals/readmoi "6"]
                                  [criterium "0.4.6"]]
                   :plugins [[dev.weavejester/lein-cljfmt "0.12.0"]
                             [lein-codox "0.10.8"]]
                   :jvm-opts ["-Djdk.attach.allowAttachSelf"
                              "-XX:+UnlockDiagnosticVMOptions"
                              "-XX:+DebugNonSafepoints"
                              "-Dclj-async-profiler.output-dir=./resources/profiler_data/"]}
             :benchmark {:jvm-opts ["-XX:+TieredCompilation"
                                    "-XX:TieredStopAtLevel=4"]}
             :repl {}}
  :aliases {"readmoi" ["run" "-m" "readmoi-generator"]
            "chlog" ["run" "-m" "chlog-generator"]}
  :codox {:metadata {:doc/format :markdown}
          :namespaces [#"^brokvolli\.(?!scratch)"]
          :target-path "doc"
          :output-path "doc"
          :doc-files []
          :source-uri "https://github.com/blosavio/brokvolli/blob/main/{filepath}#L{line}"
          :html {:transforms [[:div.sidebar.primary] [:append [:ul.index-link [:li.depth-1 [:a {:href "https://github.com/blosavio/brokvolli"} "Project Home"]]]]]}
          :project {:name "brokvolli" :version "version 0"}}
  :scm {:name "git" :url "https://github.com/blosavio/brokvolli"})

