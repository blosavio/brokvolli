(ns brokvolli.performance.project-version)


(defn project-version-lein
  "Queries the Leiningen 'project.clj' file's `defproject` expression and
  returns a string. Intended for invoking from a bash script."
  {:UUIDv4 #uuid "8b0354c2-1757-4e68-85ea-65dd2159b999"
   :no-doc true}
  []
  (println
   (let [project-metadata (read-string (slurp "project.clj"))]
     (nth project-metadata 2))))

