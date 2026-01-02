(def page-abstract
  [:div
   [:h3
    [:em "What are the memory and processing trade-offs for different tactics of
 constructing a sequential of "
     [:code "1-n"] "?"]]

   [:p "We could merely guess, and the unpredictability of the JVM's dynamic
 optimizations and the non-determinism of the OS environment make it even more
 uncertain. So let's make some quick measurements."]

   [:p "We will measure the memory size and processing times for constructing a
 sequential of integers of lengths one, ten, ..., one million. Let's consider
 seven "
    [:a {:href "https://github.com/blosavio/brokvolli/blob/main/test/brokvolli/performance/create_range.clj"}
     "tactics"]
    "."]

   [:ul
    [:li [:p [:code "long-range"]
          " creates a "
          [:code "clojure.lang.LongRange"]
          ". The naive, base case."]]

    [:li [:p [:code "vector-range-1"]
          " stuffs a "
          [:code "clojure.lang.LongRange"]
          " into a vector via "
          [:code "into"]
          "."]]

    [:li [:p [:code "vector-range-2"]
          " calls "
          [:code "vec"]
          " on a "
          [:code "clojure.lang.LongRange"]
          "."]]

    [:li [:p [:code "transducer-range"]
          " is a transducer variant of "
          [:code "into"]
          ", analogous to "
          [:code "vector-range-1"]
          "."]]

    [:li [:p [:code "transient-range"]
          " conjoins onto a transient vector."]]

    [:li [:p [:code "long-array-range"]
          " returns a Java array of longs constructed with "
          [:code "amap"]
          "."]]

    [:li [:p [:code "vec-range"]
          " returns a primitive vector, i.e., an instance of "
          [:code "clojure.core.Vec"]
          "."]]]

   [:p "Don't read too much into the data, just get a sense for the general
 trends. Overall, long ranges, Java arrays of longs, and vectors-of-primitives
 are the most memory-efficient, while the transducer, transient, and range
 tactics are the most time efficient."]

   [:p "A long-range seems to offer the best combination of memory efficiency
 and performance."]])


(def memory-subsection-preamble
  [:div
   [:h2 "Memory usage"]

   [:p "An instance of "
    [:code "clojure.lang.LongRange"]
    " is an "
    [:a {:href "https://github.com/clojure/clojure/blob/master/src/jvm/clojure/lang/LongRange.java"}
     "efficient, special case"]
    ", which allows it consume a fixed, "
    [:a {:href "https://github.com/clojure-goes-fast/clj-memory-meter/issues/13"}
     "relatively-small"]
    " amount of memory."]

   [:p "Beyond that, the Java long array and the Clojure primitive vector are
 the most space-efficient by about half an order of magnitude. All the variants
 that produce a vector are indistinguishable, as we should expect."]])


(def benchmark-subsection-preamble
  [:div
   [:h2 "Benchmark timings"]

   [:p "Creating a "
    [:code "LongRange"]
    " appears to consistently be the fastest variant, closely followed by the
 transducer and transient variants. The Java array of longs is nearly two
 orders of magnitude slower."]])


(def commentary-subsection
  [:div
   [:h2 "Commentary"]

   [:p "Let's eye-ball the observations into coarse tiers."]

   [:table
    [:tr
     [:th "tier"]
     [:th "memory"]
     [:th "performance"]]

    [:tr
     [:td "0"]
     [:td [:div
           [:strong "long-range"]]]
     [:td ""]]

    [:tr
     [:td "1"]
     [:td [:div
           "long-array-range"
           [:br]
           "vec-range"]]
     [:td [:div
           [:strong "long-range"]
           [:br]
           [:strong "vector-range-1"]
           [:br]
           [:strong "transducer-range"]
           [:br]
           [:strong "transient-range"]
           [:br]]]]

    [:tr
     [:td "2"]
     [:td [:div
           [:strong "vector-range-1"] " & 2"
           [:br]
           [:strong "transducer-range"]
           [:br]
           [:strong "transient-range"]]]
     [:td [:div
           "vector-range-2"
           [:br]
           "vec-range"]]]

    [:tr
     [:td "3"]
     [:td ""]
     [:td "long-array-range"]]]

   [:p "Instances of Clojure "
    [:code "LongRange"]
    ", vector ranges, and the transducer and transient variants demonstrate
 the best performance on these tests, while offering tolerable memory
 consumption."]

   [:p "Opinion for this application: Consume additional memory (within reason)
 to gain speed."]

   [:p "Additionally, an instance of "
    [:code "LongRange"]
    " has the happy trait that it requires no additional cleverness to
 implement; it's built-in and idiomatic."]])

