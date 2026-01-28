(ns brokvolli.scratch
  "`clojure.core/transduce` requires coll implement clojure.lang.IReduceInit.
  `clojure.core/reduce-kv` delegates to `clojure.core.protocols/kv-reduce`
  `clojure.core.protocols/kv-reduce` is declared by `IKVReduce` in protocols
")


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
                {:clojure-lang-IKReduce? (instance? clojure.lang.IKVReduce %2)
                 :clojure-core-protocols-IKVReduce? (instance? clojure.core.protocols.IKVReduce %2)})
        (sorted-map)
        [(vector)
         (array-map)
         (hash-map :a 11)
         (sorted-map :a 11)
         (hash-set)
         (sorted-set)
         (list 11)])

{"class clojure.lang.PersistentArrayMap" {:clojure-lang-IKReduce? true,  :clojure-core-protocols-IKVReduce? false},
 "class clojure.lang.PersistentHashMap"  {:clojure-lang-IKReduce? true,  :clojure-core-protocols-IKVReduce? false},
 "class clojure.lang.PersistentHashSet"  {:clojure-lang-IKReduce? false, :clojure-core-protocols-IKVReduce? false},
 "class clojure.lang.PersistentList"     {:clojure-lang-IKReduce? false, :clojure-core-protocols-IKVReduce? false},
 "class clojure.lang.PersistentTreeMap"  {:clojure-lang-IKReduce? true,  :clojure-core-protocols-IKVReduce? false},
 "class clojure.lang.PersistentTreeSet"  {:clojure-lang-IKReduce? false, :clojure-core-protocols-IKVReduce? false},
 "class clojure.lang.PersistentVector"   {:clojure-lang-IKReduce? true,  :clojure-core-protocols-IKVReduce? false}}

