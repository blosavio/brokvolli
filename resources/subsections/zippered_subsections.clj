(def page-abstract
  [:div
   [:h3
    [:em "What are the memory and processing trade-offs for different tactics of
 constructing a 'zippered' structure of indexes and a vector's elements?"]]

   [:p "One strategy for implementing a "
    [:code "transduce-kv"]
    " involves feeding a 'zippered' thingy, a sequential consisting of 2-tuples
 of index+elements. I considered nine tactics for generating such a zippered
 thing."]

   [:ul
    [:li [:p [:code "mapv-zip"]
          " is the naive, base case, merely globbing an index onto each element
 and stuffing it into a two-element vector."]]

    [:li [:p [:code "mapv-entry-zip"]
          " is similar, but tests if using a "
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
 provide the best performance, followed by the two mapv variants."]])


(def memory-subsection-preamble
  [:div
   [:h2 "Memory usage"]

   [:p ""]])


(def benchmark-subsection-preamble
  [:div
   [:h2 "Benchmark timings"]

   [:p ""]])

