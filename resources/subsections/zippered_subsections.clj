(def page-abstract
  [:div
   [:h3
    [:em "What are the memory and processing trade-offs for different tactics of
 constructing a 'zippered' structure of indexes and a vector's elements?"]]

   [:p "One strategy for implementing "
    [:code "transduce-kv"]
    " involves feeding a 'zippered' thingy, a sequential consisting of 2-tuples
 of index+elements. For example, a vector like this..."]

   [:pre [:code "[97 98 99]"]]

   [:p "...gets 'zippered' into this."]

   [:pre [:code "[[0 97] [1 98] [2 99]"]]

   [:p
    "Let's consider nine "
    [:a {:href "https://github.com/blosavio/brokvolli/blob/main/test/brokvolli/performance/create_zipped_sequential.clj"}
     "tactics"]
    " for generating such a zippered thing."]

   [:ul
    [:li [:p [:code "mapv-zip"]
          " is the naive, base case, merely globbing an index onto each element
 and stuffing it into a two-element vector."]]

    [:li [:p [:code "mapv-entry-zip"]
          " is similar, but investigates if using a "
          [:code "MapEntry"]
          " causes measurably different performance."]]

    [:li [:p [:code "pmap-zip"]
          " is similar to the base case, but uses "
          [:code "pmap"]
          "."]]

    [:li [:p [:code "long-range-zip"]
          " uses a primitive vector of "
          [:code "long"]
          "s to supply the indexes."]]

    [:li [:p [:code "long-array-zip"]
          " uses a Java array of "
          [:code "long"]
          "s."]]

    [:li [:p [:code "map-indexed-zip"]
          " uses the index supplied by "
          [:code "map-indexed"]
          "."]]

    [:li [:p [:code "transduce-zip"]
          " transduces with "
          [:code "map-indexed"]
          "."]]

    [:li [:p [:code "transient-loop-zip"]
          " conjoins onto a transient vector in a loop, avoiding creation of a
 secondary sequential to supply the indexes."]]

    [:li [:p [:code "transient-first-next-zip"]
          " conjoins onto a transient vector using a recursive "
          [:code "first/next"]
          " idiom, avoiding creation of a secondary sequential."]]]

   [:p "From a high-level view, all tactics use a similar amount of memory.
 Performance-wise, the transducer, first/next transient, and map-indexed tactics
 provide the best performance."]])


(def memory-subsection-preamble
  [:div
   [:h2 "Memory usage"]

   [:p "All nine tactics consume memory within a factor of two or three.
 long-range zippers, mapv-entry zippers, transient and transducer zippers, and
 mapv-indexed zippers form a cluster of lower-tier memory usage, while pmap
 zippers, transient-first/next and mapv zippers comprise a higher-tier."]])


(def benchmark-subsection-preamble
  [:div
   [:h2 "Benchmark timings"]

   [:p "The performance measurements could be grouped into roughly three tiers.
 The fastest tier contains transduce zippers, transient first/next zippers, and
 map-indexed zippers. pmap zippers perform consistently worse, and transient
 loop zippers show supra-exponential slow-downs on these tests.."]])


(def commentary-subsection
  [:div
   [:h2 "Commentary"]

   [:p "Let's assemble a summary table."]

   [:table
    [:tr
     [:th "tier"]
     [:th "memory"]
     [:th "performance"]]

    [:tr
     [:td "1"]
     [:td [:div
           "long-range-zippered"
           [:br]
           "mapv-entry-zippered"
           [:br]
           "transient-loop-zippered"
           [:br]
           [:strong "transduce-zippered"]
           [:br]
           [:strong "map-indexed-zippered"]
           [:br]
           "long-array-zippered"]]
     [:td [:div
           [:strong "transduce-zippered"]
           [:br]
           [:strong "map-indexed-zippered"]
           [:br]
           [:strong "transient-first-next-zippered"]
           [:br]]]]

    [:tr
     [:td "2"]
     [:td [:div
           "pmap-zippered"
           [:br]
           "mapv-zippered"
           [:br]
           [:strong "transient-first-next-zippered"]]]
     [:td [:div
           "mapv-entry-zippered"
           [:br]
           "mapv-zippered"
           [:br]
           "long-array-zippered"
           [:br]
           "long-range-zippered"]]]

    [:tr
     [:td "3"]
     [:td ""]
     [:td [:div
           "transient-loop-zippered"
           [:br]
           "pmap-zippered"]]]]

   [:p "The transduce, transient-first/next, and map-indexed zipper variants
 demonstrate the fastest performance on these tests. The transduce variant and
 map-indexed provide a bit more memory efficiency than transient-first/next."]

   [:p "The map-indexed tactic has the strong benefit that it requires no
 implementation cleverness; it's built-in and idiomatic."]])

