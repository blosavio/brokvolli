[:section#dont
 [:h2 "To don't"]

 [:p "Non-goals, etc."]

 [:ul
  [:li [:p "Won't stop you from using stateful transducers in a multi-threaded
 context. User must know enough to avoid. Or stick to single-threaded variants."]]
  [:li [:p "multiple collections a la `map`"]]
  [:li [:p "All possible permutations of function signatures."]]
  [:li [:p "non-sequential steps: sliding window, first+last, second+second-last, etc."]]]]

