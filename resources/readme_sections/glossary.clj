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
    [:code "(fn [acc element] ...)"]
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

  [:dt#keydex "keydex"]
  [:dd [:p "Short-hand for "
        [:em "key/index"]
        ". A key \"locates\" an element contained in an associative collection,
 while an integer index do so within a sequential collection."]]

  [:dt#reducing-function "reducing function"]
  [:dd
   [:p "A function that does the work in a "
    [:a {:href "#reduce"} "reduce"]
    " operation, "
    [:em "i.e., "]
    " "
    [:code "f"]
    " in "
    [:code "(reduce f coll)"]
    ". The ultimate function at the \"bottom\" of the transducing "
    [:a {:href "#stack"} "stack"]
    "."]

   [:p "In a "
    [:code "reduce"]
    "-style operation, "
    [:code "f"]
    "'s signature is "
    [:code "(fn [acc element] ...)"]
    ". In a "
    [:code "reduce-kv"]
    "-style operation, "
    [:code "f"]
    "'s signature is "
    [:code "(fn [acc keydex element] ...)"]
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
    [:code "(transduce xform f coll)"]
    ". When discussing the mechanical execution of a transduction, may refer to "
    [:code "(xform f)"]
    "."]]

  [:dt#transducer "transducer"]
  [:dd
   [:p "Synonym: "
    [:em "transducing function"]
    ". A function that modifies ("
    [:em "i.e., "]
    [:a {:href "#transform"} "\"transforms\""]
    ") a reducing function or another transducer. Practically, a sequence
 function's "
    [:code "coll"]
    "-omitted arity, e.g., "
    [:a {:href "https://clojure.github.io/clojure/clojure.core-api.html#clojure.core/map"}
     "(map f)"]
    ", "
    [:a {:href "https://clojure.github.io/clojure/clojure.core-api.html#clojure.core/filter"}
     "(filter pred)"]
    ", "
    [:a {:href "https://clojure.github.io/clojure/clojure.core-api.html#clojure.core/take"}
     "(take n)"]
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

