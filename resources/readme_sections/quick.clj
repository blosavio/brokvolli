[:section#quick
 [:h2 "Quick reference"]

 [:table
  [:tr
   [:th "prototype"]
   [:th "single-threaded"]
   [:th "multi-threaded"]]

  [:tr
   [:td [:code "reduce"]]
   [:td [:code "clojure.core/transduce"]]
   [:td [:code "brokvolli.multi/transduce"]]]

  [:tr
   [:td [:code "reduce-kv"]]
   [:td [:code "brokvolli.single/transduce-kv"]]
   [:td [:code "brokvolli.multi/transduce-kv"]]]]

 [:h3 "Signatures"]
 [:table
  [:tr
   [:th "functions"]
   [:th "signatures"]
   [:th "mnemonic"]]
  [:tr
   [:td
    [:pre
     [:code "clojure.core/transduce &"]
     [:br]
     [:code "brokvolli.single/transduce-kv"]]]
   [:td
    [:pre
     [:code "[xform f      coll]"]
     [:br]
     [:code "[xform f init coll]"]]]
   [:td
    [:p
     [:em"Like "
      [:code "reduce"]
      ", but with "
      [:code "xform"]
      " at front."]]]]

  [:tr
   [:td [:pre
         [:code "brokvolli.multi/transduce &"]
         [:br]
         [:code "brokvolli.multi/transduce-kv"]]]
   [:td
    [:code "[          xform f coll]"]
    [:br]
    [:code "[  combine xform f coll]"]
    [:br]
    [:code "[n combine xform f coll]"]]
   [:td
    [:p [:em "Expands from right-to-left, roughly in order of required-ness."]]]]]]

