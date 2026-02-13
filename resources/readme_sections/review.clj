[:section#review
 [:h2 "Lightning review"]

 [:h3 "Reducing"]

 [:p "Here's how "
  [:code "reduce"]
  " works for sequential collections. Let's take a vector of numbers, increment
 each, and conjoin each result onto a new vector."]

 [:pre
  [:code                "                v------------"] [:br]
  (print-form-then-eval "(reduce #(conj %1 (inc %2)) [] [11 22 33])") [:br]
  [:code                "                        ^--------"]]

 [:p [:code "reduce"]
  " grabs the initial value, "
  [:code "[]"]
  ", and puts it into the "
  [:code "%1"]
  " spot, and shoves the first element, "
  [:code "11"]
  " into the "
  [:code "%2"]
  ". Then after "
  [:code "inc"]
  " does its magic, "
  [:code "conj"]
  " mushes them together, and "
  [:code "reduce"]
  " goes after the "
  [:code "22"]
  ", and so on until it runs out of numbers."]

 [:p "How does that differ from an associative collection? Let's take a hashmap
 of numbers associated to keywords and try the same thing."]

 [:pre
  [:code "(reduce #(conj %1 (inc %2)) [] {:a 11 :b 22 :c 33})"]
  [:br]
  [:code ";; => Unhandled java.lang.ClassCastException
;;    class clojure.lang.MapEntry cannot be cast to class java.lang.Number"]]

 [:p "Oops. "
  [:code "reduce"]
  " passes in the elements as a "
  [:em "map entry"]
  ", a little 2-ple of the key and value. Let's adjust our reducing function."]

 [:pre
  [:code                "                v--------------------"] [:br]
  (->
   (print-form-then-eval "(reduce #(conj %1 (update %2 1 inc)) [] {:a 11 :b 22 :c 33})")
   (update 1 #(clojure.string/replace % #"\n" ""))) [:br]
  [:code                "                           ^-------------\\___/"]]

 [:p "As before, "
  [:code "reduce"]
  " puts the accumulating value into the "
  [:code "%1"]
  ", but this time, it puts the entire map entry, "
  [:code "[:a 11]"]
  ", into the "
  [:code "%2"]
  ". That's actually kinda nice, because it's not too rare that we want the key
 to which a value is associated."]

 [:p "But what if we're reducing over a sequential collection and we'd like to
 know an element's index while we're doing the work?"]

 [:p
  [:code "reduce-kv"]
  " does that for us. Take a look."]

 [:pre
  [:code                "                   v------------------------"] [:br]
  (->
   (print-form-then-eval "(reduce-kv #(conj %1 (vector %2 (inc %3))) [] [11 22 33])")
   (update 1 #(clojure.string/replace % #"\n" ""))) [:br]
  [:code                "                              ^       ^--------"] [:br]
  [:code                "                              ^"] [:br]
  [:code                "                              ^--------------- 0  1  2  <--- indexes"]]

 [:p [:code "reduce-kv"]
  " sends the reducing function three items: the accumulating value, the index,
 and the next value."]

 [:p [:code "transduce"]
  " closely relates to "
  [:code "reduce"]
  "; it marches through the supplied collection, doing tasks on its contents,
 building up a result along the way. The only differences are that the tasks are
 performed by a stack of function-modifying-functions, and the accumulation is
 performed by an explicitly separate function."]

 [:p "Our toy example with "
  [:code "transduce"]
  " looks like this."]

 [:pre
  (print-form-then-eval "(transduce (map inc) conj [11 22 33])")]

 [:p "Notice that because it uses "
  [:code "reduce"]
  " under the hood, "
  [:code "transduce"]
  " has no knowledge of the element's index as it marches through the sequential
 collection."]

 [:h3 "Folding"]

 [:p [:code "fold"]
  " works vaguely similarly."]

 [:pre
  (print-form-then-eval "(require '[clojure.core.reducers :as r])")
  [:br]
  [:br]
  (print-form-then-eval "(r/fold 2 (r/monoid into vector) #(conj %1 (inc %2)) [11 22 33])")]

 [:p "The result is equivalent, but as "
  [:code "fold"]
  " builds up the accumulating result, it also does something invisible: When
 the collection grows large, it sends sub-tasks to different threads."]
 ]

