(ns brokvolli.scratch)


(defn reduce-kv-offset
  "Like `reduce-kv`, but applies `offset` to idx, the second arg of `f`."
  {:UUIDv4 #uuid "a7804734-5134-45e6-81fc-5fae835fd1b3"}
  [f init coll offset]
  (let [f-mod (fn [acc idx vl] (f acc (+ offset idx) vl))]
    (reduce-kv f-mod init coll)))


(comment
  (reduce-kv-offset #(+ %1 %2 %3) 900 [1 2 3] 10)
  (reduce-kv-offset #(+ %1 %2 %3) 900 [10 20 30] 10)
  )


(defrecord TestRec [field-1 field-2])
(defstruct TestStruct :field-1 :field-2)


(defn create-ArraySeq
  "Returns a `clojure.lang.ArraySeq"
  {:UUIDv4 #uuid "2bea61f3-6e22-4752-8036-f4ae7bb88330"}
  [& args]
  args)



(reduce #(assoc %1
                (str (type %2))
                {:IReduceInit? (instance? clojure.lang.IReduceInit %2)
                 :IReduce? (instance? clojure.lang.IReduce %2)})
        (sorted-map)
        [(vector)
         (list)
         (list 1 2 3)
         (array-map)
         (seq (array-map :a 11))
         (hash-map :a 11)
         (hash-set)
         (sorted-set)
         (lazy-seq [])
         (cycle [1 2 3])
         (iterate inc 0)
         (repeat 99)
         "abc"
         (seq "abc")
         (range 0 3)
         (range 0.0 3.0)
         (first {:a 11})
         (->TestRec 11 22)
         (struct-map TestStruct :field-1 11 :field-2 22)
         (create-ArraySeq 99)])

{"class brokvolli.core.TestRec"                {:IReduceInit? false, :IReduce? false},
 "class clojure.lang.ArraySeq"                 {:IReduceInit? true, :IReduce? true},
 "class clojure.lang.Cycle"                    {:IReduceInit? true, :IReduce? true},
 "class clojure.lang.Iterate"                  {:IReduceInit? true, :IReduce? true},
 "class clojure.lang.LazySeq"                  {:IReduceInit? false, :IReduce? false},
 "class clojure.lang.LongRange"                {:IReduceInit? true, :IReduce? true},
 "class clojure.lang.MapEntry"                 {:IReduceInit? false, :IReduce? false},
 "class clojure.lang.PersistentArrayMap"       {:IReduceInit? false, :IReduce? false},
 "class clojure.lang.PersistentArrayMap$Seq"   {:IReduceInit? true, :IReduce? true},
 "class clojure.lang.PersistentHashSet"        {:IReduceInit? false, :IReduce? false},
 "class clojure.lang.PersistentList"           {:IReduceInit? true, :IReduce? true},
 "class clojure.lang.PersistentList$EmptyList" {:IReduceInit? false, :IReduce? false},
 "class clojure.lang.PersistentStructMap"      {:IReduceInit? false, :IReduce? false},
 "class clojure.lang.PersistentTreeSet"        {:IReduceInit? false, :IReduce? false},
 "class clojure.lang.PersistentVector"         {:IReduceInit? true, :IReduce? true},
 "class clojure.lang.Range"                    {:IReduceInit? true, :IReduce? true},
 "class clojure.lang.Repeat"                   {:IReduceInit? true, :IReduce? true},
 "class clojure.lang.StringSeq"                {:IReduceInit? true, :IReduce? false},
 "class java.lang.String"                      {:IReduceInit? false, :IReduce? false}}



(reduce #(assoc %1
                (str (type %2))
                (instance? clojure.lang.IKVReduce %2))
        (sorted-map)
        [(vector)
         (array-map)
         (hash-map :a 11)
         (sorted-map :a 11)
         (hash-set)
         (sorted-set)
         (list 11)])

{"class clojure.lang.PersistentArrayMap" true,
 "class clojure.lang.PersistentHashMap" true,
 "class clojure.lang.PersistentHashSet" false,
 "class clojure.lang.PersistentList" false,
 "class clojure.lang.PersistentTreeMap" true,
 "class clojure.lang.PersistentTreeSet" false,
 "class clojure.lang.PersistentVector" true}




;;;; explore accumulating functions


(defn roll
  "Given one or more numbers, returns a function that accumulates one or more
  numbers, or exits with a conj-ed sequence of all accumulations."
  {:UUIDv4 #uuid "d3b3ca8b-53bd-4702-918e-f1b0896bc444"}
  ([[x :as all]] (if (nil? x)
                   all
                   #(comp all (roll %&)))))


(transduce #_((roll (map inc)
                  (filter even?)) nil)
           (comp (map inc)
                 (filter even?))
           conj
           []
           [11 22 33 44 55 66 88 99 99])


((roll 1 2 3) nil)


((roll
  (roll
   (roll 1 2 3 4)
   5 6 7 8)
  9 11 12)
 nil)


(defn example-bar
  [x] x)

(reduce #(conj %1 {:f %2
                   :ifn? (ifn? %2)
                   :fn? (fn? %2)})
        []
        [#(%1)
         (fn example-foo [x] x)
         example-bar])
;; [{:f #function[brokvolli.scratch/eval16016/fn--16019], :ifn? true, :fn? true}
;;  {:f #function[brokvolli.scratch/eval16016/example-foo--16021], :ifn? true, :fn? true}
;;  {:f #function[brokvolli.scratch/example-bar], :ifn? true, :fn? true}]

(type #())