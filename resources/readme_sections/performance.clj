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
  [:a {:href "https://blosavio.github.io/chlog/transductions_performance.html"}
   "more"]
  " "
  [:a {:href "https://blosavio.github.io/chlog/deep_performance.html"}
   "nuanced"]
  " "
  [:a {:href "https://blosavio.github.io/chlog/partitions_performance.html"}
   "performance"]
  " "
  [:a {:href "https://blosavio.github.io/chlog/processors_performance.html"}
   "story"]
  ". Even though Brokvolli implements them with light-weight Fork/Join
 threads, that additional machinery, plus the partitioning and combining,
 imposes non-negligible overhead. The multi-threaded variants almost always
 perform slightly worse on collections with fewer than one-thousand elements,
 and almost always slightly worse when the per-element task is small (e.g.,
 incrementing a number)."]

 [:p "However, when the input collection grows beyond ten-thousand elements, or
 the transduction involves ten or more operations per element, then the
 multi-threaded transducing functions start to exhibit performance benefits.
 Note, however, that the speedup is somewhat less than the number of
 processors, in many cases proportional to 3/4 of each additional processor. For
 example, recruiting sixteen CPUs provides a ~12× speedup over one CPU."]

 [:p "The general advice remains the same as for any performance consideration
 involving the Java Virtual Machine: Objectively "
  [:a {:href "https://github.com/hugoduncan/criterium/"}
   "measure"]
  " a statistically-significant number of samples of data that accurately
 represents the intended use case. Only switch to the multi-threaded functions
 if the performance improves consistently by solidly double-digit percent
 (i.e., >33%)."]

 [:p "In practice, consider using the multi-threaded variants when the
 collection contains more than tens of thousands of elements, when computation
 per element is substantial, and set the partition threshold, "
  [:code "n"]
  ", so that there's at least ten partitions."]

 [:p "Just because they're available, don't be overly eager to jump to the
 multi-threaded variants. Remember, the single-threaded transducing functions
 still provide performance benefits compared to the equivalent sequence
 operations because transducers operate efficiently on reducible collections and
 eliminate intermediate collections."]]

