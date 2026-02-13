[:section#usage

 [:h2 "Usage"]

 [:p "The Brokvolli utilities live at the intersection of "
  [:code "transduce"]
  ", "
  [:code "reduce-kv"]
  ", and "
  [:code "fold"]
  ". As much as possible, it adopts their idioms, adding or changing only what
 is absolutely necessary. Therefore, using the Brokvolli library leans on
 prior understanding that trio. Our discussion here will also rely on basic
 "
  [:a {:href "https://clojure.org/reference/transducers"} "familiarity"]
  " of "
  [:a {:href "https://clojuredocs.org/clojure.core/reduce-kv"} "their"]
  " "
  [:a {:href "https://clojure.org/reference/reducers"} "workings"]
  "."]

 [:h3#single-transduce-kv [:code "transduce-kv"] " (single-threaded)"]

 [:pre (print-form-then-eval "(require '[brokvolli.single :refer [transduce-kv]]
                                      '[brokvolli.transducers-kv :refer [map-kv filter-kv remove-kv]]
                                      '[brokvolli.stateful-transducers-kv :refer [take-kv take-while-kv]])")]

 [:p "The analogy to keep in mind is that "
  [:code "reduce"]
  " is to "
  [:code "reduce-kv"]
  " as "
  [:code "transduce"]
  " is to "
  [:code "transduce-kv"]
  "."]

 [:p "Let's assign ourselves an imaginary task: "
  [:em "Given a vector of integers, return a new vector with each element
 incremented."]
  " Let's do that with good ol' "
  [:code "transduce"]
  ". The incrementing business can be done with "
  [:code "inc"]
  ". We want to do something to every element, so that'll use a "
  [:code "map"]
  " transducer. We want to build up a new collection with a one-to-one
 correspondence with the input collection; that's straightforwardly done with "
  [:code "conj"]
  "."]

 [:pre (print-form-then-eval "(transduce (map inc) conj [11 22 33])")]

 [:p "Let's highlight a few points. We used the 'no-init' form of "
  [:code "transduce"]
  ". That means "
  [:code "transduce"]
  " evaluated "
  (print-form-then-eval "(conj)")
  " to produce the initial value. Also, "
  [:code "conj"]
  "'s one-argument arity provided the 'completing' phase of the transduction, in
 this case, identity. Finally, "
  [:code "inc"]
  " is a function of one argument, the incoming number."]

 [:p "Let's put that earlier analogy into motion by doing that same task with "
  [:code "transduce-kv"]
  ". It's signature is exactly the same."]

 [:pre
  [:code "(transduce-kv " [:em "xform f coll"] ")"]
  [:br]
  [:code "(transduce-kv " [:em "xform f init coll" ")"]]]

 [:p
  "We need to make some adjustments, though. The transducer returned by "
  [:code "(map inc)"]
  " only has arities for zero, one, and two arguments. However, "
  [:code "transduce-kv"]
  " passes the key/index as well, so we'll need a transducer with an additional
 three-argument arity. Brokvolli provides such a transducer, "
  [:code "map-kv"]
  ", in its "
  [:code "transducers-kv"]
  " namespace."]

 [:p "Further, as we noted before, "
  [:code "inc"]
  " accepts only a single argument. But "
  [:code "transduce-kv"]
  " will be passing two arguments, the key/index and the element. In this
 example, we won't be using the key/index, so we can simply drop it."]

 [:pre (print-form-then-eval "(def inc-kv (fn [_ element] (inc element)))")]

 [:p "Now we have this."]

 [:pre (print-form-then-eval "(transduce-kv (map-kv inc-kv) conj [11 22 33])")]

 [:p "Oops. That's not quite correct. We can see that our incremented elements
 are interspersed with indexes. Regular "
  [:code "conj"]
  "'s variadic behavior conjoins any number of trailing items onto the
 collection. For each incoming pair of index+number, we want to skip the first
 item and conjoin only the second item. Let's adjust "
  [:code "conj"]
  "."]

 [:pre (print-form-then-eval "(def new-conj (fn ([] []) ([x] x) ([x _ z] (conj x z))))")]

 [:p "Called with zero args, returns a vector, one arg, returns the arg. Called
 with three args, conjoins the first and third, dropping the second. And now our
 expression works correctly."]

 [:pre (print-form-then-eval "(transduce-kv (map-kv inc-kv) new-conj [11 22 33])")]

 [:p "That adjusted "
  [:code "conj"]
  " is so useful, Brokvolli provides it pre-made, named "
  [:code "tconj"]
  "."]

 [:pre (print-form-then-eval "(transduce-kv (map-kv inc-kv) tconj [11 22 33])")]

 [:p "Let's extend our imaginary task: "
  [:em "Given a vector of numbers, increment each, retain only the evens, and
 stop after three."]
  " With regular "
  [:code "transduce"]
  ", we'd compose a transducer stack with "
  [:code "comp"]
  "."]

 [:pre
  [:code
   "(comp (map inc)
      (filter even?)
      (take 3))"]]

 [:p "As before, "
  [:code "map"]
  " applies "
  [:code "inc"]
  " to every element. Then, "
  [:code "filter"]
  " retains only the even results. Finally, "
  [:code "take"]
  " halts the process after the prescribed count of elements."]

 [:p "We'll lengthen the input vector to better demonstrate the "
  [:code "take"]
  "-ing action."]

 [:pre (print-form-then-eval "(transduce (comp (map inc)
                                                (filter even?)
                                                (take 3))
                                          conj
                                          [11 22 33 44 55 66 77 88 99])")]

 [:p "Again, we note that "
  [:code "inc"]
  ", "
  [:code "filter"]
  ", and "
  [:code "take"]
  " are all functions of one argument."]

 [:p "Now let's do the same with "
  [:code "transduce-kv"]
  ". Earlier, we had to use a special mapping transducer, "
  [:code "map-kv"]
  " because "
  [:code "transduce-kv"]
  " passed both the key/index and the element. Brokvolli provides counterparts
 to the others: "
  [:code "filter-kv"]
  " and "
  [:code "take-kv"]
  ". Remember that in "
  [:code "-kv"]
  " land, those inner functions must handle two arguments. We made a special "
  [:code "inc-kv"]
  " earlier, so let's make a special two-argument predicate for retaining evens
 that ignores the index."]

 [:pre (print-form-then-eval "(def even?-kv (fn [_ element] (even? element)))")]

 [:p
  [:code "take"]
  ", and by extension "
  [:code "take-kv"]
  ", isn't interested in the actual elements from the collection, they consume
 only an integer argument. So we don't need to make special two-arg function
 for that."]

 [:p "Our "
  [:code "-kv"]
  " transducer stack looks like this."]

 [:pre [:code
        "(comp (map-kv inc-kv)
      (filter-kv even?-kv)
      (take-kv 3))"]]

 [:p "We assemble the pieces."]

 [:pre (print-form-then-eval
        "(transduce-kv (comp (map-kv inc-kv)
                            (filter-kv even?-kv)
                            (take-kv 3))
                      tconj
                      [11 22 33 44 55 66 77 88 99])")]

 [:p "Equivalent result. That's a relief."]

 [:p "But we've been completely ignoring the index, which kinda obviates the
 entire purpose of using "
  [:code "transduce-kv"]
  ". So we'll ignore them no longer."]

 [:p "Our new imaginary task is this: "
  [:em "Given a vector of numbers, increment each, retain the ones with odd "
   [:strong "indexes"]
   ", and stop once the "
   [:strong "index"]
   " is greater than or equal to five."]
  " Let's go piece by piece."]

 [:ul
  [:li
   [:p "Incrementing each element from the collection is identical as before."]
   [:pre [:code "(map-kv (fn [_ x] (inc x)))"]]]
  [:li
   [:p "Filtering is a bit different. Instead of deciding based on the value of
 the element (the second argument), we decide based on the value of the index
 (the first element). That looks like this: "]
   [:pre [:code "(filter-kv (fn [idx _] (odd? idx)))"]]]
  [:li
   [:p "Instead of "
    [:code "take-kv"]
    ", we'll pull in its cousin, "
    [:code "take-while-kv"]
    ", which is the "
    [:code "-kv"]
    " variant of regular "
    [:code "take-while"]
    ". Again, to decide where to stop, we'll consider only the index (the first argument) and ignore the element (the second argument)."]
   [:pre [:code "(take-while-kv (fn [idx _] (<= idx 5)))"]]]]

 [:p "Then we build the transducer stack with "
  [:code "comp"]
  "."]

 [:pre [:code
        "(comp (map-kv (fn [_ x] (inc x)))
      (filter-kv (fn [idx _] (odd? idx)))
      (take-while-kv (fn [idx _] (<= idx 5))))"]]

 [:p "And jam it into our transduce expression and evaluate."]

 [:pre (print-form-then-eval
        "(transduce-kv (comp (map-kv (fn [_ x] (inc x)))
                                                 (filter-kv (fn [idx _] (odd? idx)))
                                                 (take-while-kv (fn [idx _] (<= idx 5))))
                                           tconj
                                           [11 22 33 44 55 66 77 88 99])")]

 [:p "Let's sketch out what happened."]

 [:pre
  [:code
"value  index  (inc value)  (odd? index)  (<= index 5)    result
11     0      12           false         «skipped eval»  []
22     1      23           true          true            [23]
33     2      34           false         «skipped eval»  [23]
44     3      45           true          true            [23 45]
55     4      56           false         «skipped eval»  [23 45]
66     5      67           true          true            [23 45 67]
77     6      78           false         «skipped eval»  [23 45 67]
88     7      89           true          «halt»          [23 45 67]"]]

 [:p "For each element of the vector, "
  [:code "map-kv"]
  " increments the number. Then, "
  [:code "filter-kv"]
  " checks if the index is odd. If not, that step ends. If the index "
  [:em "is"]
  " odd, then "
  [:code "take-while-kv"]
  " checks if the index is less than five. If it is, the element is conjoined.
 Once the index exceeds five, the entire process is halted without considering
 any more elements."]

 [:p "Keep in mind that if a "
  [:code "-kv"]
  " transducer passes an element to an inner function/predicate, such as "
  [:code "map-kv"]
  " or "
  [:code "filter-kv"]
  ", it always also passes the key/index. However, when the "
  [:code "-kv"]
  " transducer does not have an inner function, such as "
  [:code "take-kv"]
  ", that layer of the transducer stack also ignores the key/index. Regardless
 of whether or not a transducer has an inner function, the key/index and
 element are always passed to the next layer of the transducer stack."]

 [:h3 "Indexes carry through"]

 [:p "Before we leave sequential input collections, let's notice one other
 behavior: Indexes always refer to the location within the original, input
 collection regardless of how many elements are removed or inserted. Here's
 a removal demonstration."]

 [:pre (print-form-then-eval "(transduce-kv (comp (filter-kv (fn [idx _] (even? idx)))
                                                   (map-kv (fn [idx x] {:index idx :x x}))) tconj [11 22 33 44 55 66 77 88 99])" 75 35)]

 [:p "After filtering to retain only the even indexes, the indexes of the output
 vector represent their position in the input collection, not in the final
 collection."]

 [:p "This principle holds for an expansive transduction as well. We can use "
  [:code "mapcat-kv"]
  " with "
  [:code "repeat"]
  " to construct an expanding transducing stack."]

 [:pre
  (print-form-then-eval "(require '[brokvolli.transducers-kv :refer [mapcat-kv]])")
  [:br]
  [:br]
  (print-form-then-eval "(transduce-kv (comp (mapcat-kv (fn [_ x] (repeat 3 x)))
                                             (map-kv (fn [idx x] {:index idx :x x}))) tconj [11 22 33])" 75 35)]

 [:p "Even though the transduction expanded from three elements to nine, each
 index reflects the location within the original, input collection."]

 [:h3 [:code "transduce-kv"] " and associative collections"]

 [:p "So far, we've only discussed transducing sequential collections. Like "
  [:code "transduce"]
  ", "
  [:code "transduce-kv"]
  " will also handle associative collections."]

 [:p "Let's invent a new task: "
  [:em "Given a hashmap of numbers associated to keywords, increment the numbers
 and retain only the keys "
   [:code ":b"]
   ", "
   [:code ":d"]
   ", "
   [:code ":f"]
   ", returning a new hashmap."]]

 [:p "From before, we already know how to increment a value by assembling "
  [:code "map-kv"]
  " and "
  [:code "inc-kv"]
  ". We already pulled in "
  [:code "filter-kv"]
  ", but we need a new predicate. That predicate takes to arguments, the key and
 the element. We only care about filtering on the key, so we'll ignore the
 element's value. A set of "
  [:code "#{:b :d :f}"]
  " is a slick way to test if we should retain the key+value pair. We assemble
 those two transducers with ordinary "
  [:code "comp"]
  "."]

 [:pre [:code "(comp (map-kv inc-kv)
                     (filter-kv (fn [keyword _] (#{:b :d :f} keyword))))"]]

 [:p "Finally, we need to assemble the results. Earlier, when we were discussing
 sequential collections, "
  [:code "conj"]
  " (or its variants) served that role. Now, we're building up a hashmap, so we
 need a function that generates an empty hashmap, completes with an identity,
 and associates a key and a value to the accumulating hashmap. Such a function
 might look like this."]

 [:pre [:code
        "(fn
  ([] {})
  ([m] m)
  ([m k v] (assoc m k v))) ;; could also be `(conj m [k v])`"]]

 [:p "That's a pretty commonly-needed utility, so perhaps you won't be surprised
 that Brokvolli provides one pre-made, named "
  [:code "tassoc"]
  "."]

 [:p "Let's assemble all those parts and evaluate."]

 [:pre (print-form-then-eval "(transduce-kv (comp (map-kv inc-kv)
                                                 (filter-kv (fn [keyword _] (#{:b :d :f} keyword))))
                                           tassoc
                                           {:a 11 :b 22 :c 33 :d 44 :e 55 :f 66 :g 77})")]

 [:p [:code "transduce-kv"]
  " incremented each number, and then retained only the three designated
 keyword+value pairs."]

 [:p "One point to note in this example is that we threw away a lot of work. We
 first incremented all the integers, and "
  [:em "then"]
  " filtered. If we had instead filtered first, "
  [:code "transduce-kv"]
  " would only have had to do the increment operation three times."]

 [:h3#generality "Other outputs"]

 [:p [:code "transduce-kv"]
  " is completely general: it will accept any collection that "
  [:code "reduce-kv"]
  " accepts, and it can build up any collection type (not necessarily the input
 collection type) or scalar value."]

 [:p "Observe: transducing a hashmap to construct a string."]

 [:pre (print-form-then-eval "(transduce-kv (map-kv (fn [key value] (str key \" cups of \" value)))
                                             (fn
                                               ([] \"\")
                                               ([s] s)
                                               ([s _ value] (str s value \"; \")))
                                             {99 \"soda\" 98 \"coffee\" 97 \"tea\"})")]

 [:h3#kv-ize [:code "kv-ize"] ": a last resort"]

 [:p "Suppose someone handed us a transformer stack composed with no knowledge
 that "
  [:code "transduce-kv"]
  " existed, and for some reason we wanted to plug in that stack without
 changing anything else. Brokvolli's "
  [:code "kv-ize"]
  "  utility wraps a transformer so that the key/index is diverted to dynamic
 var, passing only the element. That way, the stack can be used as-is."]

 [:p "Standard transformer, incompatible with "
  [:code "transduce-kv"]
  "."]

 [:pre
  [:code "(transduce-kv (map inc) conj [11 22 33])"]
  [:code
   ";; Unhandled clojure.lang.ArityException
  ;; Wrong number of args (2) passed to: clojure.core/inc"]]

 [:p "Modified transformer."]

 [:pre
  (print-form-then-eval "(require '[brokvolli.core :refer [kv-ize *keydex*]])")
  [:br]
  [:br]
  (print-form-then-eval "(transduce-kv (kv-ize (map inc)) conj [11 22 33])")]

 [:p "So, where did the index go? We can access it as "
  [:code "*keydex*"]
  " inside the inner function."]

 [:pre
  (print-form-then-eval "(transduce-kv (kv-ize (map #(array-map :index *keydex* :value %))) conj [11 22 33])")]

 [:p "This usage is a hack, and really only should be used as a last resort when
 we for some reason can't adjust our transducing functions to handle
 keys/indexes."]

 [:h3#multi-transduce "multi-threaded "[:code "transduce"] " & " [:code "transduce-kv"]]

 [:p "If we can arrange our job so that the operations of each element is
 completely independent of all the others, Brokvolli can perform a neat trick.
 Let's pretend we want to increment the numbers contained in a super-big
 vector, say eight elements."]

 [:pre [:code "[11 22 33 44 55 66 77 88]"]]

 [:p "Regular ol' "
  [:code "transduce"]
  " can do that job just fine. However, we have available three little
 incrementing machines, "
  [:code "A"]
  ", "
  [:code "B"]
  ", and "
  [:code "C"]
  ". We could give our trio some instructions: "
  [:em "Machine "
   [:code "A"]
   ", grab the first chunk of elements, increment each, and stuff them into a
 vector. Machine "
   [:code "B"]
   " do the same for the next chunk of elements, and Machine "
   [:code "C"]
   " do the same for the remaining elements."]]

 [:p "Being agreeable, they do."]

 [:pre
  [:code "[11 22 33   44 55   66 77 88]"] [:br]
  [:code "\\_______/   \\___/   \\_______/"] [:br]
  [:code "    |         |         |"] [:br]
  [:code "    A         B         C"] [:br]
  [:code "    |         |         |"] [:br]
  [:code "    v         v         v"] [:br]
  [:code "[12 23 34] [45 56] [67 78 89]"] [:br]]

 [:p "Machine "
  [:code "A"]
  " decides to grab the first three elements, Machine "
  [:code "B"]
  " grabs the next two, and Machine "
  [:code "C"]
  " grabs the final three elements. They all return a vector of the appropriate
 size."]

 [:p "Now, we have one final step: Putting the three vectors back together. We
 say "
  [:em "One of you Machines, concatenate all three vectors, reflecting the
 origins of the elements. "]
  " Machine "
  [:code "D"]
  " volunteers."]

 [:pre
  [:code "[12 23 34] [45 56] [67 78 89]"] [:br]
  [:code "         \\    |    /"] [:br]
  [:code "          \\   |   /"] [:br]
  [:code "           \\  |  /"] [:br]
  [:code "            \\ | /"] [:br]
  [:code "             \\|/"] [:br]
  [:code "              D"] [:br]
  [:code "              |"] [:br]
  [:code "              v"] [:br]
  [:code "  [12 23 34 45 56 67 78 89]"]]

 [:p "We've spread our multi-piece job among three independent workers. If the
 delegation and re-assembly portions are relatively quick, our overall job is
 completed in a shorter amount of time."]

 [:p "Brokvolli offers multi-threaded variants of "
  [:code "transduce"]
  " and "
  [:code "transduce-kv"]
  " to do that exact job. If we can arrange our job so that processing each
 element is completely independent from all the others, the multi-threaded
 transduce functions will partition the input collection into roughly
 equal-sized chunks, process each chunk on a light-weight thread, and
 re-assembles the processed chunks. In practice, Brokvolli closely follows the
 usage patterns established by "
  [:code "clojure.core.reducers/fold"]
  "."]

 [:p "The signatures of the multi-threaded transduce functions look like this."]

 [:pre
  [:code "(transduce " [:em "          xform f coll" ")"]] [:br]
  [:code "(transduce " [:em "  combine xform f coll" ")"]] [:br]
  [:code "(transduce " [:em "n combine xform f coll" ")"]]]

 [:p "The little machines we imagined correspond to the arguments. Machines "
  [:code "A"]
  ", "
  [:code "B"]
  ", and "
  [:code "C"]
  " are instances of "
  [:code "(xform f)"]
  ", a transformer stack, "
  [:code "xform"]
  ", wrapping a reducing function, "
  [:code "f"]
  ". Machine "
  [:code "D"]
  " is "
  [:em "combining function"]
  ", "
  [:code "combine"]
  "."]

 [:p "The transformer stacks and reducing functions are pretty much those we
 use with single-threaded "
  [:code "transduce"]
  " and "
  [:code "transduce-kv"]
  ". The only difference is that their scope is a partition of the input
 collection, not the whole; they reduce only part of the collection. The
 reducing function "
  [:code "f"]
  " supplies the initial value for reducing the partition (zero args), the
 'completing' step (one arg), and the reducing step (two args for regular "
  [:code "transduce"]
  " and three args for "
  [:code "transduce-kv"]
  "."]

 [:p "The combining function is analogous to the transformer stack. The
 zero-argument arity is invoked to create the initial value of the combining
 process, the one-argument arity is used for the completing step, and the two
 argument arity performs the combining the left and right chunks. (There is
 no analogous three-argument arity because the key/index is not used in the
 combining process.)"]

 [:p "That's a lot of words. Maybe a table is better."]

 [:table
  [:tr
   [:td ""]
   [:td [:code "(xform f)"]]
   [:td [:code "combine"]]]
  [:tr
   [:td "zero args"]
   [:td "init for partition"]
   [:td "init for combining"]]
  [:tr
   [:td "one arg"]
   [:td "completing for partition"]
   [:td "completing for combining"]]
  [:tr
   [:td "two args"]
   [:td "reducing function, accumulating value + element"]
   [:td "combining function, left value + right value"]]
  [:tr
   [:td "three args"]
   [:td "reducing function, accumulating value + key/index + element"]
   [:td "n/a"]]]

 [:p "Practically, the transformer stacks and reducing functions are the same
 as we're used to, and the combining function is concatenation for sequential
 outputs or a merge for associative outputs."]

 [:p "Let's see them in action. For simplicity, we'll start with plain
 transduce from the multi-threaded namespace."]

 [:pre (print-form-then-eval "(require '[brokvolli.multi :as multi])")]

 [:p "We'll assign ourselves the standard job, incrementing a vector of
 numbers. Normally, the multi-threaded transducing functions would partition
 at about 512 elements, but for demonstration purposes, we'll specify three."]

 [:p "We want to peel off an element and increment it. That's a job for a "
  [:code "map"]
  " transducer. Since we're using the non"
  [:code "-kv"]
  " variant, we may use Clojure's off-the-shelf transducer."]

 [:pre [:code "(map inc)"]]

 [:p "After incrementing, we want to conjoin the result onto the accumulating
 partition, a new vector. We can use  "
  [:code "conj"]
  " to provide the initial value (an empty vector), the completing function
 (identity), and the reducing function (conjoining the element)."]

 [:pre
  (print-form-then-eval "(conj)") [:br]
  (print-form-then-eval "(conj [:foo :bar])") [:br]
  (print-form-then-eval "(conj [:foo :bar] :baz)")]

 [:p "Let's break apart the input vector and calculate the chunks manually."]

 [:pre
  (print-form-then-eval "(transduce (map inc) conj [11 22 33])") [:br]
  (print-form-then-eval "(transduce (map inc) conj [44 55])") [:br]
  (print-form-then-eval "(transduce (map inc) conj [66 77 88])")]

 [:p "It's as if we could peek inside and observe what Machines "
  [:code "A"]
  ", "
  [:code "B"]
  ", and "
  [:code "C"]
  " were up to."]

 [:p "Now we've got three chunks: "
  [:code "[12 23 34]"]
  ", "
  [:code "[45 56]"]
  ", and "
  [:code "[67 78 89]"]
  ". To assemble, we run another reduction across those three items, this time
 using "
  [:code "combine"]
  ". We need an initial value and a step function. Fortunately, "
  [:code "concat"]
  " has multiple arities that suit our purposes."]

 [:pre
  (print-form-then-eval "(concat)") [:br]
  (print-form-then-eval "(concat [:foo :bar])") [:br]
  (print-form-then-eval "(concat [:foo :bar] [:baz])")]

 [:p "Hmm. "
  [:code "concat"]
  " returns a lazy sequence, which is kinda antithetical to "
  [:code "transduce"]
  ", which is eager. Whaddya know? Brokvolli provides an eager concatenating
 utility that returns vectors."]

 [:pre
  (print-form-then-eval "(concatv)") [:br]
  (print-form-then-eval "(concatv [:foo :bar])") [:br]
  (print-form-then-eval "(concatv [:foo :bar] [:baz])")]

 [:p "We've got all the pieces, let's put them together and transduce,
 multi-threaded."]

 [:pre (print-form-then-eval "(multi/transduce 3 concatv (map inc) conj [11 22 33 44 55 66 77 88])")]

 [:p "On the surface, not too impressive: we did all the same work as regular,
 single-threaded transduce, but with extra arguments. Going to that trouble
 becomes "
  [:a {:href "#performance"} "worth it"]
  " when the input collection grows large."]

 [:p "What does it look like to perform our more-involved job, "
  [:em "Increment each number, filter to retain even numbers, halt the process
 after three elements"]
  "? We know how to increment and filter."]

 [:p "That last task, "
  [:em "Halt after three elements"]
  " is a show-stopper. "
  [:code "take"]
  " and friends belong to a class of transducers that are stateful. "
  [:a {:href "#stateful-transducers"}
   "Stateful transducers"]
  " are unsafe to use in a multi-threaded transduction. There is currently no mechanism to check and prevent us from using a stateful transducer here, but it's the Brokvolli library's strong recommendation to avoid it."]

 [:p "But it'd be kinda nice to have a third step for our demonstration, so
 let's pick a third from the safe set of stateless transducers. Let's
 stipulate "
  [:em "Remove elements if they're greater than seventy"]
  "."]

 [:p "Our transformer stack will look like this."]

 [:pre
  [:code
   "(comp (map inc)
  (filter even?)
  (remove #(< 70 %)))"]]

 [:p "Let's try out that transduction."]

 [:pre (print-form-then-eval "(multi/transduce 3 concatv (comp (map inc)
  (filter even?)
  (remove #(< 70 %))) conj [11 22 33 44 55 66 77 88])")]

 [:p "All the elements were incremented, "
  [:code "23"]
  ", "
  [:code "45"]
  ", "
  [:code "67"]
  ", and "
  [:code "89"]
  " were not retained by "
  [:code "filter"]
  " because they're odd, and "
  [:code "78"]
  " was removed by "
  [:code "remove"]
  " because it's greater than seventy. Behind the scenes, these tasks were done
 by three light-weight threads, each having only a chunk of the input
 collection. Once the chunks returned, "
  [:code "multi/transduce"]
  " stitched them together into the final output collection. We might visualize
 the process like this."]

 [:pre
  [:code "input     [11 22 33     44 55     66 77 88]"] [:br]
  [:code "partition [11 22 33]   [44 55]   [66 77 88]"] [:br]
  [:code "increment  12 23 34     45 56     67 78 89"] [:br]
  [:code "filter     12    34        56        78"] [:br]
  [:code "remove     12    34        56"] [:br]
  [:code "init    [] 12    34     [] 56"] [:br]
  [:code "conj      [12]   34       [56]"] [:br]
  [:code "conj      [12 34]         [56]"] [:br]
  [:code "init   [] [12 34]         [56]"] [:br]
  [:code "concatv   [12 34]         [56]"] [:br]
  [:code "concatv   [12 34 56]"] [:br]]

 [:p "We can rest easy that the multi-threaded transducing functions preserve
 the ordering for sequential input collections."]

 [:p "What about multi-threaded "
  [:code "transduce-kv"]
  "? It's the amalgam of single-threaded "
  [:code "transduce-kv"]
  " and multi-threaded "
  [:code "transduce"]
  ". The reducing function, "
  [:code "f"]
  " and transformer stack "
  [:code "xform"]
  " are the same as"
  [:a {:href "#single-transduce-kv"}
   [:span " single-threaded " [:code "transduce-kv"]]]
  ". The combining function is the same as "
  [:a {:href "#multi-transduce"}
   [:span "multi-threaded " [:code "transduce"]]]
  "."]

 [:p "Let's run through a quick demonstration. "
  [:em "Given a vector of numbers, increment each element, retain elements
 whose index is even, and remove elements whose index is greater than six."]
  ". We'll break down our job by assembling the argument list from right to
 left. The sub-reductions need to build up a sequential collection, so we'll
 rely on "
  [:code "tconj"]
  ". Our transformer stack performs three tasks on each element, a mapping, a
 filtering, and a removing. Each needs to handle "
  [:code "-kv"]
  " inputs. Here's our stack."]

 [:pre
  (print-form-then-eval "(def xform-7 (comp (map-kv (fn [_ x] (inc x)))
                                           (filter-kv (fn [idx _] (even? idx)))
                                           (remove-kv (fn [idx _] (< 6 idx)))))")]

 [:p "Finally, we need to supply a combining function to assemble the results of
 the sub-reductions, which are vectors themselves. "
  [:code "concatv"]
  " will do nicely."]

 [:p "Altogether, we have this."]

 [:pre
  (print-form-then-eval "(multi/transduce-kv 3 concatv xform-7 tconj [11 22 33 44 55 66 77 88 99])")]

 [:p [:code "multi/transduce-kv"]
  " ripped the elements like this."]

 [:pre
           [:code "index   element   inc   result"] [:br]
  [:strong [:code "0       11        12    retained"]] [:br]
           [:code "1       22        23    filtered"] [:br]
  [:strong [:code "2       33        34    retained"]] [:br]
           [:code "3       44        45    filtered"] [:br]
  [:strong [:code "4       55        56    retained"]] [:br]
           [:code "5       66        67    filtered"] [:br]
  [:strong [:code "6       77        78    retained"]] [:br]
           [:code "7       88        89    removed"] [:br]
           [:code "8       99        100   removed"]]

 [:p "Behind the scenes, "
  [:code "multi/transduce-kv"]
  " partitioned the input vector into lengths of three or fewer, sent those
 pieces to a pool where light-weight threads performed the sub-transductions,
 then, once those results were finished, concatenated the results from the
 sub-reductions. The final result is exactly as if we had used single-threaded "
  [:code "transduce-kv"]
  "."]

 [:pre
  (print-form-then-eval "(require '[brokvolli.single :as single])")
  [:br]
  [:br]
  (print-form-then-eval "(single/transduce-kv xform-7 tconj [11 22 33 44 55 66 77 88 99])")
  [:br]
  [:br]
  (print-form-then-eval
   "(let [test (fn [f] (f xform-7 tconj [11 22 33 44 55 66 77 88 99]))]
   (= (test single/transduce-kv)
      (test multi/transduce-kv)))")]

 [:p "Don't make any assumptions about where the multi-threaded transduce
 functions partition the input collection. That's an implementation detail and
 subject to change without notice."]

 [:h4#stateful-transducers "Multi-threaded transduction and stateful
 transducers"]

 [:p "Some transducers are stateful, and therefore will most likely give
 incorrect results when using the multi-threaded transduce functions. For
 example, "
  [:code "take"]
  " (and its "
  [:code "-kv"]
  " variant) is stateful. Here's what happens when used with multi-threaded "
  [:code "transduce"]
  "."]

 [:pre (print-form-then-eval "(multi/transduce 3 concatv (take 1) tconj [11 22 33 44 55 66 77 88 99])")]

 [:p "We intended to halt the transduction after collecting the first element.
 However, multiple threads took the first element of their individual
 partitions, ignorant that the same thing was happening on sibling threads.
 Then, "
  [:code "transduce"]
  " dutifully concatenated all the results, returning an unintended result: a
 vector with more than one element."]

 [:p "There is no mechanism to check or stop us from using stateful transducers
 with the multi-threaded transduce functions. We must rely on policy and
 discipline."]

 [:p "The following chart categorizes Clojure's off-the-shelf transducers."]

 [:table
  [:tr
   [:th "stateless"]
   [:th "stateful"]]
  [:tr
   [:td "cat"]
   [:td "dedupe"]]
  [:tr
   [:td "filter"]
   [:td "distinct"]]
  [:tr
   [:td "keep"]
   [:td "drop"]]
  [:tr
   [:td "map"]
   [:td "drop-while"]]
  [:tr
   [:td "mapcat"]
   [:td "interpose"]]
  [:tr
   [:td "random-sample"]
   [:td "partition-all"]]
  [:tr
   [:td "remove"]
   [:td "partition-by"]]
  [:tr
   [:td "replace"]
   [:td "take"]]
  [:tr
   [:td ""]
   [:td "take-nth"]]
  [:tr
   [:td ""]
   [:td "take-while-kv"]]]

 [:p "Brokvolli's "
  [:code "-kv"]
  " transducer variants also include stateful transducers. Those that are not
 unconditionally safe to use with multi-threaded transduce functions are
 separated into the "
  [:code "stateful-transducers-kv"]
  " namespace to remind us."]

 [:h4 "Halting headaches"]

 [:p "The multi-threaded transduce functions ultimately delegate their
 partitions to "
  [:code "reduce"]
  " and "
  [:code "reduce-kv"]
  ". There are two mechanisms for breaking out of the transduction process:
 wrapping the return value in "
  [:code "reduced"]
  " is mostly for implementing a transducing function, and the "
  [:code "halt-when"]
  " transducer, mostly for the user. The multi-threaded Brokvolli functions are
 not currently able respect either early termination mechanism when the input
 collection is partitioned into multiple pieces."]

 [:h3#avoid "Avoids"]

 [:ul
  [:li [:p "Don't use multi-threaded variants as an ersatz async scheduler. Use
 instead futures, promises, or proper threads, etc."]]
  [:li [:p "Don't do any I/O or other side-effects. Not what it's for. Only do
 pure computation."]]]]

