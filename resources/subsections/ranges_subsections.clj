(def page-abstract
  [:div
   [:h3
    [:em "What are the memory and processing trade-offs for different tactics of
 constructing a sequential of "
     [:code "1-n"] "?"]]

   [:p "I can only guess, and the unpredictability of the JVM's dynamic
 optimizations and the non-determinism of the OS environment make it even more
 uncertain. So let's make some quick measurements."]

   [:p "The utility tests the memory size and processing times for constructing
 a sequential of integers of lengths one, ten, ..., one million. Eight "
    [:a {:href "https://github.com/blosavio/brokvolli/blob/main/test/brokvolli/performance/create_range.clj"}
     "tactics"]
    " were considered."]

   [:ul
    [:li [:p [:code "long-range"]
          " fully realizes a "
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
          ", a transducer variant of "
          [:code "into"]
          ", analogous to "
          [:code "vector-range-1"]
          "."]]

    [:li [:p [:code "transient-range"]
          " "
          [:cond "conj"]
          "-es onto a transient vector."]]

    [:li [:p [:code "long-array-range"]
          " returns a Java array of longs constructed with "
          [:code "amap"]
          "."]]

    [:li [:p [:code "vec-range"]
          " returns a primitive vector, i.e., an instance of "
          [:code "clojure.core.Vec"]
          "."]]]

   [:p "Don't read too much into the data, just get a sense for the general
 trends. Overall, the Java arrays of longs and vectors-of-primitives are the
 most memory-efficient, while the transducer, transient, and range tactics
 are the most time efficient."]

   [:p "The Clojure vector-of-primitives may offer the best combination of
 memory efficiency and performance."]])


(def memory-subsection-preamble
  [:div
   [:h2 "Memory usage"]

   [:p "The Java long array and the Clojure primitive vector are the most
 space-efficient by about half an order of magnitude. All the variants that
 produce a vector are indistinguishable, as we should expect."]

   [:p "Note: The memory profiler failed to descend below the root of
 `long-range`. Those memory measurements are therefore spurious."]])


(def benchmark-subsection-preamble
  [:div
   [:h2 "Benchmark timings"]

   [:p "Realizing a "
    [:code "LongRange"]
    " appears to be the consistently fastest variant, closely followed by the
 transducer and transient variants. The Java array of longs is nearly two
 orders of magnitude slower."]])

