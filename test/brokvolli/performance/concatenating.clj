(ns brokvolli.performance.concatenating
  "Benchmark various tactics for concatenating two vectors.

  Tactics:
  * lazy concat (doall (concat v1 v2))
  * vectored concat (vec (concat v1 v2))
  * basic into (into v1 v2)
  * reducers cat (r/cat v1 v2) ;; Note: returns an instance of `clojure.core.reducers.Cat`
  * transient cat <conj! onto transient-ized v1 in a loop>
  * transducers into (into v1 conj v2)"
  (:require
   [clojure.math :as math]
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


(def concat-benchmark-options-filename "./resources/concat_options.edn")
(def max-power 6)


(defn transient-concat
  "Concatenates vectors `v1` and `v2` by looping over a transient."
  {:UUIDv4 #uuid "f1af0288-c9b8-4263-b938-1b89cd8e5a6f"
   :no-doc true}
  [v1 v2]
  (let [size (count v2)]
    (loop [i 0
           v (transient v1)]
      (if (= i size)
        (persistent! v)
        (recur (inc i) (conj! v (.nth v2 i)))))))


(def vecs
  (reduce (fn [m n] (assoc m n {:left (vec (repeatedly n #(rand-int 8))) 
                                :right (vec (repeatedly n #(rand-int 8)))}))
          {}
          (range-pow-10 max-power)))


(def tactics
  {"realized-concat" (fn [v1 v2] (doall (concat v1 v2)))
   "vectored-concat" (fn [v1 v2] (vec (concat v1 v2)))
   "basic-into" (fn [v1 v2] (into v1 v2))
   "transient-cat" transient-concat
   "transducer-cat" (fn [v1 v2] (into v1 conj v2))})


(deftest verify-tactics
  (are [n] (every? #(= % (concat (get-in vecs [n :left])
                                 (get-in vecs [n :right])))
                   (map (fn [f] (f (get-in vecs [n :left])
                                   (get-in vecs [n :right]))) (vals tactics)))
    1 10 100 1000 10000))


#_(run-test verify-tactics)


(defbench
  measure-tactics
  "Concatenation tactics"
  (fn [n] ((tactics (project-version-lein))
           (get-in vecs [n :left])
           (get-in vecs [n :right])))
  (range-pow-10 max-power))


#_(run-one-defined-benchmark measure-tactics :lightning)
#_(run-benchmarks concat-benchmark-options-filename)
#_(generate-documents concat-benchmark-options-filename)

