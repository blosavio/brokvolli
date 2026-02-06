[:section#quick
 [:h2 "Quick reference"]

 [:table
  [:tr
   [:td ""]
   [:td ""]
   [:th {:colspan "2"} "execution model"]]

  [:tr
   [:th ""]
   [:td ""]
   [:th "single-threaded"]
   [:th "multi-threaded"]]

  [:tr
   [:th {:rowspan "2"} "style"]
   [:td [:code "reduce"]]
   [:td [:code "clojure.core/transduce"]]
   [:td [:code "brokvolli.multi/transduce"]]]

  [:tr
   [:td [:code "reduce-kv"]]
   [:td [:code "brokvolli.single/transduce-kv"]]
   [:td [:code "brokvolli.multi/transduce-kv"]]]]

 [:h3 "Function signatures"]
 [:table
  [:tr
   [:td [:pre [:code "clojure.core/transduce"]]]
   [:td [:code "[xform f coll]"]]
   [:td [:code "[xform f init coll]"]]
   [:td [:code ""]]]

  [:tr
   [:td [:pre [:code "brokvolli.single/transduce-kv"]]]
   [:td [:code "[xform f coll]"]]
   [:td [:code "[xform f init coll]"]]
   [:td [:code ""]]]

  [:tr
   [:td [:pre [:code "brokvolli.multi/transduce"]]]
   [:td [:code "[xform f coll]"]]
   [:td [:code "[xform f combine coll]"]]
   [:td [:code "[n xform f combine coll]"]] ]

  [:tr
   [:td [:pre [:code "brokvolli.multi/transduce-kv"]]]
   [:td [:code "[xform f coll]"]]
   [:td [:code "[xform f combine coll]"]]
   [:td [:code "[n xform f combine coll]"]]]]

 [:p "TODO: consider changing signature to match `r/fold`."]
 [:p "Mnemonic: sig grows from right to left, roughly in order of importance."]
 [:pre
  [:code "[          xform f coll]"] [:br]
  [:code "[  combine xform f coll]"] [:br]
  [:code "[n combine xform f coll]"]]]

