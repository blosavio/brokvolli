[:section#dont
 [:h2 "To don't"]

 [:p "Non-goals, etc."]

 [:ul
  [:li [:p "Brokvolli won't stop us from using stateful transducers in a
 multi-threaded context. We must be mindful of using only the state-less
 transducers, or stick to single-threaded transducing functions."]]

  [:li [:p "Brokvolli won't stop us from consuming all the machine's memory. If
 we have data that's larger, we ought to look into lazy methods."]]

  [:li [:p "Brokvolli doesn't support simultaneously transducing over multiple
 collections, à la variadic "
        [:code "map"]
        "."]]

  [:li [:p "Brokvolli doesn't offer all possible permutations of function
 signatures. If we want to specify the multi-threaded partitioning size, we must
 explicitly provide the combining function, even if it is identical to the
 reducing function."]]

  [:li [:p "Brokvolli aspires to cover only the one-element-at-a-time type of
 jobs done by "
        [:code "transduce"]
        ". It will not handle non-sequential, non-single step jobs, i.e. sliding
 window, first+last pairs, etc. See "
        [:a {:href "#alternatives"}
         " Christophe Grand's xforms"]
        " for transducing utilities offering more sophisticated options."]]]]

