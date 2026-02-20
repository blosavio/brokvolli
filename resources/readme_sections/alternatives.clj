[:section#alternatives
 [:h2 "Alternatives"]
 [:ul
  [:li
   [:p "Clojure's "
    [:a {:href "https://clojure.github.io/clojure/clojure.core-api.html#clojure.core/reduce"}
     [:code "reduce"]]
    ", "
    [:a {:href "https://clojure.github.io/clojure/clojure.core-api.html#clojure.core/reduce-kv"}
     [:code "reduce-kv"]]
    ", "
    [:a {:href "https://clojure.github.io/clojure/clojure.core-api.html#clojure.core/transduce"}
     [:code "transduce"]]
    ", and "
    [:a {:href "https://clojure.github.io/clojure/clojure.core-api.html#clojure.core.reducers/fold"}
     [:code "clojure.core.reducers/fold"]]]
   [:p  "No additional dependencies, proven in-the-wild."]
   [:br]]

  [:li
   [:p "clj-commons "
    [:a {:href "https://github.com/clj-commons/claypoole"}
     "claypoole"]]
   [:p "Threadpool-based parallel versions of Clojure functions."]
   [:br]]

  [:li
   [:p "Sebastian Fedrau's "
    [:a {:href "https://github.com/20centaurifux/pold"}
     "pold"]]
   [:p  "A Clojure library for efficiently dividing data into any number of
 partitions and accumulating them into a result."]
   [:br]]

  [:li
   [:p "Kyle Kingsbury's "
    [:a {:href "https://github.com/aphyr/tesser"}
     "Tesser"]]
   [:p "A Clojure library for concurrent & commutative folds."]
   [:br]]

  [:li
   [:p "Christophe Grand's "
    [:a {:href "https://github.com/cgrand/xforms"}
     "xforms"]]
   [:p "More transducers and reducing functions for Clojure(script)."]
   [:br]]]]

