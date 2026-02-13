[:section#intro
 [:h2 "Introduction"]

 [:p "Clojure "
  [:a {:href "https://clojure.org/reference/transducers"}
   "transducers"]
  " are nifty, and "
  [:code "transduce"]
  " provides one of the crucial off-the-shelf transducing contexts. "
  [:code "transduce"]
  " "
  [:a {:href "#reduce"}
   "reduces"]
  " over a collection, eagerly and efficiently building a result. However,
 transducer-land lacks two capabilities."
  [:ol
   [:li
    [:p "While "
     [:code "transduce"]
     " will pass an associative collection's elements as a "
     [:em "map entry"]
     #_(transduce (map identity) (completing conj) {:a 11 :b 22 :c 33})
     " (i.e., the key and its value), it has no "
     [:code "reduce-kv"]
     " counterpart that passes a sequential collection's index along with
 the value."]]
   [:li
    [:p "It has no multi-threaded option, similar to "
     [:a {:href "https://clojure.org/reference/reducers#_reduce_and_fold"}
      "fold"]
     " that reduces a collection with parallel reduce-combine strategy."]]]]

 [:p [:strong "The Brokvolli library provides both: a "
      [:code "transduce-kv"]
      " counterpart to "
      [:code "transduce"]
      ", and multi-threaded variants of both."]]]

