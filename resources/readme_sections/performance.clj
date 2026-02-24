[:section#performance
 [:h2 "Performance"]

 [:p "Under the hood, single-threaded "
  [:code "transduce-kv"]
  " delegates to "
  [:code "reduce-kv"]
  ", so there's only an "
  [:em "O(1)"]
  " sprinkling of additional overhead."]

 [:p "The multi-threaded transducing functions present a "
  [:a {:href ""} "more"]
  " "
  [:a {:href ""} "nuanced"]
  " "
  [:a {:href ""} "performance"]
  " "
  [:a {:href ""} "story"]
  ". Even though Brokvolli implements them with light-weight Fork/Join
 threads, that additional machinery imposes non-negligible overhead. The
 multi-threaded variants almost always perform slightly worse on collections
 with fewer than one-thousand elements, and almost always slightly worse when
 the per-element task is small (e.g., incrementing a number)."]

 [:p "However, when the input collection grows beyond ten-thousand elements, or
 the transduction involves ten or more tasks per element, then the
 multi-threaded transducing functions start to exhibit performance benefits.
 Note, however, that the speed-up is somewhat less than the number of
 processors."]

 [:p "The general advice remains the same as for any performance considerations
 on the Java Virtual Machine. Objectively "
  [:a {:href "https://github.com/hugoduncan/criterium/"}
   "measure"]
  " a statistically-significant number of samples of data that accurately
 represents the intended use cases. Only switch to the multi-threaded functions
 if the performance improves consistently by solidly double-digit percent
 (i.e., >33%)."]

 [:p "Just because they're available, don't be overly eager to jump to the
 multi-threaded variants. Remember, the single-threaded transducing functions
 still provide performance benefits compared to the equivalent sequence
 operations because transducers operate efficiently on reducible collections and
 eliminate intermediate collections."]]

