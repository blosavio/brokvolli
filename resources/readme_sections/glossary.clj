[:section#glossary
 [:h2 "Glossary"]

 [:dl
  [:dt#accumulator "accumulator"]
  [:dd
   [:p "Synonym: " [:em "accumulating value"]
    ". The on-going value that is produced by evaluating the "
    [:a {:href "#reducing-function"} "reducing function"]
    " with all the previous elements. The "
    [:code "acc"]
    " in a reducing function's signatures, "
    [:code "(fn [" [:strong "acc"] " element] ...)"]
    "."]]

  [:dt#combine "combine"]
  [:dd
   [:p "Gather the results after "
    [:a {:href "#reduce"} "reducing"]
    " two or more partitions of the input collection. Often a concatenation
 (for sequentials) or merging (for associatives), but need not be. A "
    [:em "combining function"]
    " is a function that implements this operation."]]

  [:dt#element "element"]
  [:dd
   [:p "A member of a collection. Within a "
    [:a {:href "#reduce"} [:code "reduce"]]
    "-style operation, the next \"thing\" "
    [:code "reduce"]
    " peels off the collection and sends to its "
    [:a {:href "#reducing-function"} "reducing function"]
    "."]]

  [:dt#inner "inner function/predicate"]
  [:dd
   [:p "A function used by a transducer to do its work. The "
    [:code "f"]
    " in "
    [:code "(map f)"]
    ", or the "
    [:code "pred"]
    " in "
    [:code "(filter pred)"]
    "."]]

  [:dt#keydex "keydex"]
  [:dd [:p "Short-hand for "
        [:em "key/index"]
        ". A key \"locates\" an element contained in an associative collection,
 while an integer index does so within a sequential collection."]]

  [:dt#reducing-function "reducing function"]
  [:dd
   [:p "A function that does the work in a "
    [:a {:href "#reduce"} "reduce"]
    " operation, "
    [:em "i.e., "]
    " "
    [:code "f"]
    " in "
    [:code "(reduce " [:strong "f"] " coll)"]
    ". In a transduce operation, the ultimate function at the \"bottom\" of the
 transducing "
    [:a {:href "#stack"} "stack"]
    ", i.e., the "
    [:code "f"]
    " in "
    [:code "(transduce xform " [:strong "f"] " coll)"]
    "."]

   [:p "In a "
    [:code "reduce"]
    " operation, "
    [:code "f"]
    "'s signature is "
    [:code "(fn [acc element] ...)"]
    ". In a "
    [:code "reduce-kv"]
    " operation, "
    [:code "f"]
    "'s signature is "
    [:code "(fn [acc keydex element] ...)"]
    ". In a transduce (both \"plain\" and \""
    [:code "-kv"]
    "\") operation, "
    [:code "f"]
    "'s signature is "
    [:code "(fn [acc result] ...)"]
    "."]]

  [:dt#reduce "reduce"]
  [:dd
   [:p "The process of consuming an "
    [:a {:href "#accumulator"} "accumulating value"]
    " and the next "
    [:a {:href "#element"} "element"]
    ", producing a new accumulating value. Note: A reduce operation may result
 in fewer, equal, or more elements than the original input."]]

  [:dt#stack "stack"]
  [:dd
   [:p "A composition of one or more "
    [:a {:href "#transducer"} "transducing functions"]
    ", often made with "
    [:a {:href "https://clojure.github.io/clojure/clojure.core-api.html#clojure.core/comp"}
     [:code "comp"]]
    ". The "
    [:code "xform"]
    " in "
    [:code "(transduce " [:strong "xform"] " f coll)"]
    ". When discussing the mechanical execution of a transduction, may refer to "
    [:code "(xform f)"]
    "."]]

  [:dt#transducer "transducer"]
  [:dd
   [:p "Synonym: "
    [:em "transducing function"]
    ". A function that modifies ("
    [:em "i.e., "]
    [:a {:href "#transform"} "\"transforms\""]
    ") a reducing function or another transducer. The "
    [:code "xform"]
    " in "
    [:code "(transduce " [:strong "xform"] " f coll)"]
    ". Practically, a sequence function's "
    [:code "coll"]
    "-omitted arity, e.g., "
    [:a {:href "https://clojure.github.io/clojure/clojure.core-api.html#clojure.core/map"}
     "(map f)"]
    ", "
    [:a {:href "https://clojure.github.io/clojure/clojure.core-api.html#clojure.core/filter"}
     "(filter pred)"]
    ", "
    [:a {:href "https://clojure.github.io/clojure/clojure.core-api.html#clojure.core/take"}
     "(take n)"]
    ", etc."]]

  [:dt#transduce "transduce"]
  [:dd
   [:p "To eagerly reduce over a concrete collection with a transducing stack
 serving as the reducing function. One of Clojure's off-the-shelf transducing
 contexts."]]

  [:dt#transform "transform"]
  [:dd
   [:p "To alter a "
    [:a {:href "#reducing-function"} "reducing function"]
    " by \"wrapping\" it in one or more "
    [:a {:href "#transducer"} "transducers"]
    "."]]]]

