
  <body>
    <a href="https://clojars.org/com.sagevisuals/brokvolli"><img src="https://img.shields.io/clojars/v/com.sagevisuals/brokvolli.svg"></a><br>
    <a href="#setup">Setup</a><br>
    <a href="https://blosavio.github.io/brokvolli/index.html">API</a><br>
    <a href="https://github.com/blosavio/brokvolli/blob/main/changelog.md">Changelog</a><br>
    <a href="#intro">Introduction</a><br>
    <a href="#review">Review</a><br>
    <a href="#usage">Usage</a><br>
    <a href="#quick">Quick reference</a><br>
    <a href="#performance">Performance</a><br>
    <a href="#dont">To don&apos;t</a><br>
    <a href="#alternatives">Alternatives</a><br>
    <a href="#glossary">Glossary</a><br>
    <a href="https://github.com/blosavio">Contact</a><br>
    <h1>
      Brokvolli
    </h1><em>A Clojure library exploring parallel transduce and transduce-kv</em><br>
    <section id="setup">
      <h2>
        Setup
      </h2>
      <h3>
        Leiningen/Boot
      </h3>
      <pre><code>[com.sagevisuals/brokvolli &quot;0-SNAPSHOT0&quot;]</code></pre>
      <h3>
        Clojure CLI/deps.edn
      </h3>
      <pre><code>com.sagevisuals/brokvolli {:mvn/version &quot;0-SNAPSHOT0&quot;}</code></pre>
      <h3>
        Require
      </h3>
      <pre><code>(require &apos;[brokvolli.core :refer [concatv tassoc tconj]]
&nbsp;        &apos;[brokvolli.single :refer [transduce-kv]]
&nbsp;        &apos;[brokvolli.transducers-kv :refer [map-kv filter-kv replace-kv]])</code></pre>
    </section>
    <section id="intro">
      <h2>
        Introduction
      </h2>
      <p>
        Clojure <a href="https://clojure.org/reference/transducers">transducers</a> are nifty, and <code>transduce</code> provides one of the crucial
        off-the-shelf transducing contexts. <code>transduce</code> <a href="#reduce">reduces</a> over a collection, eagerly and efficiently building a result.
        However, &nbsp;transducer-land lacks two capabilities.
      </p>
      <ol>
        <li>
          <p>
            While <code>transduce</code> will pass an associative collection&apos;s elements as a <em>map entry</em> (i.e., the key and its value), it has no
            <code>reduce-kv</code> counterpart that passes a sequential collection&apos;s index along with &nbsp;the value.
          </p>
        </li>
        <li>
          <p>
            It has no multi-threaded option, similar to <a href="https://clojure.org/reference/reducers#_reduce_and_fold">fold</a> that reduces a collection
            with parallel reduce-combine strategy.
          </p>
        </li>
      </ol>
      <p></p>
      <p>
        <strong>The Brokvolli library provides both: a <code>transduce-kv</code> counterpart to <code>transduce</code>, and multi-threaded variants of
        both.</strong>
      </p>
    </section>
    <section id="review">
      <h2>
        Lightning review
      </h2>
      <h3>
        Reducing
      </h3>
      <p>
        Here&apos;s how <code>reduce</code> works for sequential collections. Let&apos;s take a vector of numbers, increment &nbsp;each, and conjoin each
        result onto a new vector.
      </p>
      <pre><code>                v------------</code><br><code>(reduce #(conj %1 (inc %2)) [] [11 22 33]) ;; =&gt; [12 23 34]</code><br><code>                        ^--------</code></pre>
      <p>
        <code>reduce</code> grabs the initial value, <code>[]</code>, and puts it into the <code>%1</code> spot, and shoves the first element, <code>11</code>
        into the <code>%2</code>. Then after <code>inc</code> does its magic, <code>conj</code> mushes them together, and <code>reduce</code> goes after the
        <code>22</code>, and so on until it runs out of numbers.
      </p>
      <p>
        How does that differ from an associative collection? Let&apos;s take a hashmap &nbsp;of numbers associated to keywords and try the same thing.
      </p>
      <pre><code>(reduce #(conj %1 (inc %2)) [] {:a 11 :b 22 :c 33})</code><br><code>;; =&gt; Unhandled java.lang.ClassCastException
;;    class clojure.lang.MapEntry cannot be cast to class java.lang.Number</code></pre>
      <p>
        Oops. <code>reduce</code> passes in the elements as a <em>map entry</em>, a little 2-ple of the key and value. Let&apos;s adjust our reducing function.
      </p>
      <pre><code>                v--------------------</code><br><code>(reduce #(conj %1 (update %2 1 inc)) [] {:a 11, :b 22, :c 33});; =&gt; [[:a 12] [:b 23] [:c 34]]</code><br><code>                           ^-------------\___/</code></pre>
      <p>
        As before, <code>reduce</code> puts the accumulating value into the <code>%1</code>, but this time, it puts the entire map entry,
        <code>[:a&nbsp;11]</code>, into the <code>%2</code>. That&apos;s actually kinda nice, because it&apos;s not too rare that we want the key &nbsp;to
        which a value is associated.
      </p>
      <p>
        But what if we&apos;re reducing over a sequential collection and we&apos;d like to &nbsp;know an element&apos;s index while we&apos;re doing the work?
      </p>
      <p>
        <code>reduce-kv</code> does that for us. Take a look.
      </p>
      <pre><code>                   v------------------------</code><br><code>(reduce-kv #(conj %1 (vector %2 (inc %3))) [] [11 22 33]);; =&gt; [[0 12] [1 23] [2 34]]</code><br><code>                              ^       ^--------</code><br><code>                              ^</code><br><code>                              ^--------------- 0  1  2  &lt;--- indexes</code></pre>
      <p>
        <code>reduce-kv</code> sends the reducing function three items: the accumulating value, the index, &nbsp;and the next value.
      </p>
      <p>
        <code>transduce</code> closely relates to <code>reduce</code>; it marches through the supplied collection, doing tasks on its contents, &nbsp;building
        up a result along the way. The only differences are that the tasks are &nbsp;performed by a stack of function-modifying-functions, and the accumulation
        is &nbsp;performed by an explicitly separate function.
      </p>
      <p>
        Our toy example with <code>transduce</code> looks like this.
      </p>
      <pre><code>(transduce (map inc) conj [11 22 33]) ;; =&gt; [12 23 34]</code></pre>
      <p>
        Notice that because it uses <code>reduce</code> under the hood, <code>transduce</code> has no knowledge of the element&apos;s index as it marches
        through the sequential &nbsp;collection.
      </p>
      <h3>
        Folding
      </h3>
      <p>
        <code>fold</code> works vaguely similarly.
      </p>
      <pre><code>(require &apos;[clojure.core.reducers :as r])</code><br><br><code>(r/fold 2 (r/monoid into vector) #(conj %1 (inc %2)) [11 22 33])
;; =&gt; [12 23 34]</code></pre>
      <p>
        The result is equivalent, but as <code>fold</code> builds up the accumulating result, it also does something invisible: When &nbsp;the collection grows
        large, it sends sub-tasks to different threads.
      </p>
    </section>
    <section id="usage">
      <h2>
        Usage
      </h2>
      <p>
        The Brokvolli utilities live at the intersection of <code>transduce</code>, <code>reduce-kv</code>, and <code>fold</code>. As much as possible, it
        adopts their idioms, adding or changing only what &nbsp;is absolutely necessary. Therefore, using the Brokvolli library leans on &nbsp;prior
        understanding that trio. Our discussion here will also rely on basic &nbsp;<a href="https://clojure.org/reference/transducers">familiarity</a> of
        <a href="https://clojuredocs.org/clojure.core/reduce-kv">their</a> <a href="https://clojure.org/reference/reducers">workings</a>.
      </p>
      <h3 id="single-transduce-kv">
        <code>transduce-kv</code> (single-threaded)
      </h3>
      <pre><code>(require &apos;[brokvolli.single :refer [transduce-kv]]
&nbsp;        &apos;[brokvolli.transducers-kv :refer [map-kv filter-kv remove-kv]]
&nbsp;        &apos;[brokvolli.stateful-transducers-kv :refer [take-kv take-while-kv]])</code></pre>
      <p>
        The analogy to keep in mind is that <code>reduce</code> is to <code>reduce-kv</code> as <code>transduce</code> is to <code>transduce-kv</code>.
      </p>
      <p>
        Let&apos;s assign ourselves an imaginary task: <em>Given a vector of integers, return a new vector with each element &nbsp;incremented.</em> Let&apos;s
        do that with good ol&apos; <code>transduce</code>. The incrementing business can be done with <code>inc</code>. We want to do something to every
        element, so that&apos;ll use a <code>map</code> transducer. We want to build up a new collection with a one-to-one &nbsp;correspondence with the input
        collection; that&apos;s straightforwardly done with <code>conj</code>.
      </p>
      <pre><code>(transduce (map inc) conj [11 22 33]) ;; =&gt; [12 23 34]</code></pre>
      <p>
        Let&apos;s highlight a few points. We used the &apos;no-init&apos; form of <code>transduce</code>. That means <code>transduce</code> evaluated
        <code>(conj) ;; =&gt; []</code> to produce the initial value. Also, <code>conj</code>&apos;s one-argument arity provided the &apos;completing&apos;
        phase of the transduction, in &nbsp;this case, identity. Finally, <code>inc</code> is a function of one argument, the incoming number.
      </p>
      <p>
        Let&apos;s put that earlier analogy into motion by doing that same task with <code>transduce-kv</code>. It&apos;s signature is exactly the same.
      </p>
      <pre><code>(transduce-kv <em>xform f coll</em>)</code><br><code>(transduce-kv <em>xform f init coll)</em></code></pre>
      <p>
        We need to make some adjustments, though. The transducer returned by <code>(map inc)</code> only has arities for zero, one, and two arguments. However,
        <code>transduce-kv</code> passes the key/index as well, so we&apos;ll need a transducer with an additional &nbsp;three-argument arity. Brokvolli
        provides such a transducer, <code>map-kv</code>, in its <code>transducers-kv</code> namespace.
      </p>
      <p>
        Further, as we noted before, <code>inc</code> accepts only a single argument. But <code>transduce-kv</code> will be passing two arguments, the
        key/index and the element. In this &nbsp;example, we won&apos;t be using the key/index, so we can simply drop it.
      </p>
      <pre><code>(def inc-kv (fn [_ element] (inc element)))</code></pre>
      <p>
        Now we have this.
      </p>
      <pre><code>(transduce-kv (map-kv inc-kv) conj [11 22 33]) ;; =&gt; [0 12 1 23 2 34]</code></pre>
      <p>
        Oops. That&apos;s not quite correct. We can see that our incremented elements &nbsp;are interspersed with indexes. Regular <code>conj</code>&apos;s
        variadic behavior conjoins any number of trailing items onto the &nbsp;collection. For each incoming pair of index+number, we want to skip the first
        &nbsp;item and conjoin only the second item. Let&apos;s adjust <code>conj</code>.
      </p>
      <pre><code>(def new-conj (fn ([] []) ([x] x) ([x _ z] (conj x z))))</code></pre>
      <p>
        Called with zero args, returns a vector, one arg, returns the arg. Called &nbsp;with three args, conjoins the first and third, dropping the second. And
        now our &nbsp;expression works correctly.
      </p>
      <pre><code>(transduce-kv (map-kv inc-kv) new-conj [11 22 33]) ;; =&gt; [12 23 34]</code></pre>
      <p>
        That adjusted <code>conj</code> is so useful, Brokvolli provides it pre-made, named <code>tconj</code>.
      </p>
      <pre><code>(transduce-kv (map-kv inc-kv) tconj [11 22 33]) ;; =&gt; [12 23 34]</code></pre>
      <p>
        Let&apos;s extend our imaginary task: <em>Given a vector of numbers, increment each, retain only the evens, and &nbsp;stop after three.</em> With
        regular <code>transduce</code>, we&apos;d compose a transducer stack with <code>comp</code>.
      </p>
      <pre><code>(comp (map inc)
&nbsp;     (filter even?)
&nbsp;     (take 3))</code></pre>
      <p>
        As before, <code>map</code> applies <code>inc</code> to every element. Then, <code>filter</code> retains only the even results. Finally,
        <code>take</code> halts the process after the prescribed count of elements.
      </p>
      <p>
        We&apos;ll lengthen the input vector to better demonstrate the <code>take</code>-ing action.
      </p>
      <pre><code>(transduce (comp (map inc) (filter even?) (take 3))
&nbsp;          conj
&nbsp;          [11 22 33 44 55 66 77 88 99])
;; =&gt; [12 34 56]</code></pre>
      <p>
        Again, we note that <code>inc</code>, <code>filter</code>, and <code>take</code> are all functions of one argument.
      </p>
      <p>
        Now let&apos;s do the same with <code>transduce-kv</code>. Earlier, we had to use a special mapping transducer, <code>map-kv</code> because
        <code>transduce-kv</code> passed both the key/index and the element. Brokvolli provides counterparts &nbsp;to the others: <code>filter-kv</code> and
        <code>take-kv</code>. Remember that in <code>-kv</code> land, those inner functions must handle two arguments. We made a special <code>inc-kv</code>
        earlier, so let&apos;s make a special two-argument predicate for retaining evens &nbsp;that ignores the index.
      </p>
      <pre><code>(def even?-kv (fn [_ element] (even? element)))</code></pre>
      <p>
        <code>take</code>, and by extension <code>take-kv</code>, isn&apos;t interested in the actual elements from the collection, they consume &nbsp;only an
        integer argument. So we don&apos;t need to make special two-arg function &nbsp;for that.
      </p>
      <p>
        Our <code>-kv</code> transducer stack looks like this.
      </p>
      <pre><code>(comp (map-kv inc-kv)
&nbsp;     (filter-kv even?-kv)
&nbsp;     (take-kv 3))</code></pre>
      <p>
        We assemble the pieces.
      </p>
      <pre><code>(transduce-kv (comp (map-kv inc-kv) (filter-kv even?-kv) (take-kv 3))
&nbsp;             tconj
&nbsp;             [11 22 33 44 55 66 77 88 99])
;; =&gt; [12 34 56]</code></pre>
      <p>
        Equivalent result. That&apos;s a relief.
      </p>
      <p>
        But we&apos;ve been completely ignoring the index, which kinda obviates the &nbsp;entire purpose of using <code>transduce-kv</code>. So we&apos;ll
        ignore them no longer.
      </p>
      <p>
        Our new imaginary task is this: <em>Given a vector of numbers, increment each, retain the ones with odd <strong>indexes</strong>, and stop once the
        <strong>index</strong> is greater than or equal to five.</em> Let&apos;s go piece by piece.
      </p>
      <ul>
        <li>
          <p>
            Incrementing each element from the collection is identical as before.
          </p>
          <pre><code>(map-kv (fn [_ x] (inc x)))</code></pre>
        </li>
        <li>
          <p>
            Filtering is a bit different. Instead of deciding based on the value of &nbsp;the element (the second argument), we decide based on the value of
            the index &nbsp;(the first element). That looks like this:
          </p>
          <pre><code>(filter-kv (fn [idx _] (odd? idx)))</code></pre>
        </li>
        <li>
          <p>
            Instead of <code>take-kv</code>, we&apos;ll pull in its cousin, <code>take-while-kv</code>, which is the <code>-kv</code> variant of regular
            <code>take-while</code>. Again, to decide where to stop, we&apos;ll consider only the index (the first argument) and ignore the element (the second
            argument).
          </p>
          <pre><code>(take-while-kv (fn [idx _] (&lt;= idx 5)))</code></pre>
        </li>
      </ul>
      <p>
        Then we build the transducer stack with <code>comp</code>.
      </p>
      <pre><code>(comp (map-kv (fn [_ x] (inc x)))
&nbsp;     (filter-kv (fn [idx _] (odd? idx)))
&nbsp;     (take-while-kv (fn [idx _] (&lt;= idx 5))))</code></pre>
      <p>
        And jam it into our transduce expression and evaluate.
      </p>
      <pre><code>(transduce-kv (comp (map-kv (fn [_ x] (inc x)))
&nbsp;                   (filter-kv (fn [idx _] (odd? idx)))
&nbsp;                   (take-while-kv (fn [idx _] (&lt;= idx 5))))
&nbsp;             tconj
&nbsp;             [11 22 33 44 55 66 77 88 99])
;; =&gt; [23 45 67]</code></pre>
      <p>
        Let&apos;s sketch out what happened.
      </p>
      <pre><code>value  index  (inc value)  (odd? index)  (&lt;= index 5)    result
11     0      12           false         «skipped eval»  []
22     1      23           true          true            [23]
33     2      34           false         «skipped eval»  [23]
44     3      45           true          true            [23 45]
55     4      56           false         «skipped eval»  [23 45]
66     5      67           true          true            [23 45 67]
77     6      78           false         «skipped eval»  [23 45 67]
88     7      89           true          «halt»          [23 45 67]</code></pre>
      <p>
        For each element of the vector, <code>map-kv</code> increments the number. Then, <code>filter-kv</code> checks if the index is odd. If not, that step
        ends. If the index <em>is</em> odd, then <code>take-while-kv</code> checks if the index is less than five. If it is, the element is conjoined.
        &nbsp;Once the index exceeds five, the entire process is halted without considering &nbsp;any more elements.
      </p>
      <p>
        Keep in mind that if a <code>-kv</code> transducer passes an element to an inner function/predicate, such as <code>map-kv</code> or
        <code>filter-kv</code>, it always also passes the key/index. However, when the <code>-kv</code> transducer does not have an inner function, such as
        <code>take-kv</code>, that layer of the transducer stack also ignores the key/index. Regardless &nbsp;of whether or not a transducer has an inner
        function, the key/index and &nbsp;element are always passed to the next layer of the transducer stack.
      </p>
      <h3>
        Indexes carry through
      </h3>
      <p>
        Before we leave sequential input collections, let&apos;s notice one other &nbsp;behavior: Indexes always refer to the location within the original,
        input &nbsp;collection regardless of how many elements are removed or inserted. Here&apos;s &nbsp;a removal demonstration.
      </p>
      <pre><code>(transduce-kv (comp (filter-kv (fn [idx _] (even? idx)))
&nbsp;                   (map-kv (fn [idx x] {:index idx, :x x})))
&nbsp;             tconj
&nbsp;             [11 22 33 44 55 66 77 88 99])
;; =&gt; [{:index 0, :x 11}
;;     {:index 2, :x 33}
;;     {:index 4, :x 55}
;;     {:index 6, :x 77}
;;     {:index 8, :x 99}]</code></pre>
      <p>
        After filtering to retain only the even indexes, the indexes of the output &nbsp;vector represent their position in the input collection, not in the
        final &nbsp;collection.
      </p>
      <p>
        This principle holds for an expansive transduction as well. We can use <code>mapcat-kv</code> with <code>repeat</code> to construct an expanding
        transducing stack.
      </p>
      <pre><code>(require &apos;[brokvolli.transducers-kv :refer [mapcat-kv]])</code><br><br><code>(transduce-kv (comp (mapcat-kv (fn [_ x] (repeat 3 x)))
&nbsp;                   (map-kv (fn [idx x] {:index idx, :x x})))
&nbsp;             tconj
&nbsp;             [11 22 33])
;; =&gt; [{:index 0, :x 11}
;;     {:index 0, :x 11}
;;     {:index 0, :x 11}
;;     {:index 1, :x 22}
;;     {:index 1, :x 22}
;;     {:index 1, :x 22}
;;     {:index 2, :x 33}
;;     {:index 2, :x 33}
;;     {:index 2, :x 33}]</code></pre>
      <p>
        Even though the transduction expanded from three elements to nine, each &nbsp;index reflects the location within the original, input collection.
      </p>
      <h3>
        <code>transduce-kv</code> and associative collections
      </h3>
      <p>
        So far, we&apos;ve only discussed transducing sequential collections. Like <code>transduce</code>, <code>transduce-kv</code> will also handle
        associative collections.
      </p>
      <p>
        Let&apos;s invent a new task: <em>Given a hashmap of numbers associated to keywords, increment the numbers &nbsp;and retain only the keys
        <code>:b</code>, <code>:d</code>, <code>:f</code>, returning a new hashmap.</em>
      </p>
      <p>
        From before, we already know how to increment a value by assembling <code>map-kv</code> and <code>inc-kv</code>. We already pulled in
        <code>filter-kv</code>, but we need a new predicate. That predicate takes to arguments, the key and &nbsp;the element. We only care about filtering on
        the key, so we&apos;ll ignore the &nbsp;element&apos;s value. A set of <code>#{:b :d :f}</code> is a slick way to test if we should retain the
        key+value pair. We assemble &nbsp;those two transducers with ordinary <code>comp</code>.
      </p>
      <pre><code>(comp (map-kv inc-kv)
&nbsp;                    (filter-kv (fn [keyword _] (#{:b :d :f} keyword))))</code></pre>
      <p>
        Finally, we need to assemble the results. Earlier, when we were discussing &nbsp;sequential collections, <code>conj</code> (or its variants) served
        that role. Now, we&apos;re building up a hashmap, so we &nbsp;need a function that generates an empty hashmap, completes with an identity, &nbsp;and
        associates a key and a value to the accumulating hashmap. Such a function &nbsp;might look like this.
      </p>
      <pre><code>(fn
&nbsp; ([] {})
&nbsp; ([m] m)
&nbsp; ([m k v] (assoc m k v))) ;; could also be `(conj m [k v])`</code></pre>
      <p>
        That&apos;s a pretty commonly-needed utility, so perhaps you won&apos;t be surprised &nbsp;that Brokvolli provides one pre-made, named
        <code>tassoc</code>.
      </p>
      <p>
        Let&apos;s assemble all those parts and evaluate.
      </p>
      <pre><code>(transduce-kv (comp (map-kv inc-kv)
&nbsp;                   (filter-kv (fn [keyword _] (#{:b :d :f} keyword))))
&nbsp;             tassoc
&nbsp;             {:a 11, :b 22, :c 33, :d 44, :e 55, :f 66, :g 77})
;; =&gt; {:b 23, :d 45, :f 67}</code></pre>
      <p>
        <code>transduce-kv</code> incremented each number, and then retained only the three designated &nbsp;keyword+value pairs.
      </p>
      <p>
        One point to note in this example is that we threw away a lot of work. We &nbsp;first incremented all the integers, and <em>then</em> filtered. If we
        had instead filtered first, <code>transduce-kv</code> would only have had to do the increment operation three times.
      </p>
      <h3 id="generality">
        Other outputs
      </h3>
      <p>
        <code>transduce-kv</code> is completely general: it will accept any collection that <code>reduce-kv</code> accepts, and it can build up any collection
        type (not necessarily the input &nbsp;collection type) or scalar value.
      </p>
      <p>
        Observe: transducing a hashmap to construct a string.
      </p>
      <pre><code>(transduce-kv (map-kv (fn [key value] (str key &quot; cups of &quot; value)))
&nbsp;             (fn ([] &quot;&quot;) ([s] s) ([s _ value] (str s value &quot;; &quot;)))
&nbsp;             {99 &quot;soda&quot;, 98 &quot;coffee&quot;, 97 &quot;tea&quot;})
;; =&gt; &quot;99 cups of soda; 98 cups of coffee; 97 cups of tea; &quot;</code></pre>
      <h3 id="kv-ize">
        <code>kv-ize</code>: a last resort
      </h3>
      <p>
        Suppose someone handed us a transformer stack composed with no knowledge &nbsp;that <code>transduce-kv</code> existed, and for some reason we wanted to
        plug in that stack without &nbsp;changing anything else. Brokvolli&apos;s <code>kv-ize</code> utility wraps a transformer so that the key/index is
        diverted to dynamic &nbsp;var, passing only the element. That way, the stack can be used as-is.
      </p>
      <p>
        Standard transformer, incompatible with <code>transduce-kv</code>.
      </p>
      <pre><code>(transduce-kv (map inc) conj [11 22 33])</code><code>;; Unhandled clojure.lang.ArityException
&nbsp; ;; Wrong number of args (2) passed to: clojure.core/inc</code></pre>
      <p>
        Modified transformer.
      </p>
      <pre><code>(require &apos;[brokvolli.core :refer [kv-ize *keydex*]])</code><br><br><code>(transduce-kv (kv-ize (map inc)) conj [11 22 33]) ;; =&gt; [12 23 34]</code></pre>
      <p>
        So, where did the index go? We can access it as <code>*keydex*</code> inside the inner function.
      </p>
      <pre><code>(transduce-kv (kv-ize (map #(array-map :index *keydex* :value %)))
&nbsp;             conj
&nbsp;             [11 22 33])
;; =&gt; [{:index 0, :value 11}
;;     {:index 1, :value 22}
;;     {:index 2, :value 33}]</code></pre>
      <p>
        This usage is a hack, and really only should be used as a last resort when &nbsp;we for some reason can&apos;t adjust our transducing functions to
        handle &nbsp;keys/indexes.
      </p>
      <h3 id="multi-transduce">
        multi-threaded <code>transduce</code> &amp; <code>transduce-kv</code>
      </h3>
      <p>
        If we can arrange our job so that the operations of each element is &nbsp;completely independent of all the others, Brokvolli can perform a neat trick.
        &nbsp;Let&apos;s pretend we want to increment the numbers contained in a super-big &nbsp;vector, say eight elements.
      </p>
      <pre><code>[11 22 33 44 55 66 77 88]</code></pre>
      <p>
        Regular ol&apos; <code>transduce</code> can do that job just fine. However, we have available three little &nbsp;incrementing machines, <code>A</code>,
        <code>B</code>, and <code>C</code>. We could give our trio some instructions: <em>Machine&nbsp;<code>A</code>, grab the first chunk of elements,
        increment each, and stuff them into a &nbsp;vector. Machine&nbsp;<code>B</code> do the same for the next chunk of elements, and
        Machine&nbsp;<code>C</code> do the same for the remaining elements.</em>
      </p>
      <p>
        Being agreeable, they do.
      </p>
      <pre><code>[11 22 33   44 55   66 77 88]</code><br><code>\_______/   \___/   \_______/</code><br><code>    |         |         |</code><br><code>    A         B         C</code><br><code>    |         |         |</code><br><code>    v         v         v</code><br><code>[12 23 34] [45 56] [67 78 89]</code><br></pre>
      <p>
        Machine&nbsp;<code>A</code> decides to grab the first three elements, Machine&nbsp;<code>B</code> grabs the next two, and Machine&nbsp;<code>C</code>
        grabs the final three elements. They all return a vector of the appropriate &nbsp;size.
      </p>
      <p>
        Now, we have one final step: Putting the three vectors back together. We &nbsp;say <em>One of you Machines, concatenate all three vectors, reflecting
        the &nbsp;origins of the elements.</em> Machine&nbsp;<code>D</code> volunteers.
      </p>
      <pre><code>[12 23 34] [45 56] [67 78 89]</code><br><code>         \    |    /</code><br><code>          \   |   /</code><br><code>           \  |  /</code><br><code>            \ | /</code><br><code>             \|/</code><br><code>              D</code><br><code>              |</code><br><code>              v</code><br><code>  [12 23 34 45 56 67 78 89]</code></pre>
      <p>
        We&apos;ve spread our multi-piece job among three independent workers. If the &nbsp;delegation and re-assembly portions are relatively quick, our
        overall job is &nbsp;completed in a shorter amount of time.
      </p>
      <p>
        Brokvolli offers multi-threaded variants of <code>transduce</code> and <code>transduce-kv</code> to do that exact job. If we can arrange our job so
        that processing each &nbsp;element is completely independent from all the others, the multi-threaded &nbsp;transduce functions will partition the input
        collection into roughly &nbsp;equal-sized chunks, process each chunk on a light-weight thread, and &nbsp;re-assembles the processed chunks. In
        practice, Brokvolli closely follows the &nbsp;usage patterns established by <code>clojure.core.reducers/fold</code>.
      </p>
      <p>
        The signatures of the multi-threaded transduce functions look like this.
      </p>
      <pre><code>(transduce <em>          xform f coll)</em></code><br><code>(transduce <em>  combine xform f coll)</em></code><br><code>(transduce <em>n combine xform f coll)</em></code></pre>
      <p>
        The little machines we imagined correspond to the arguments. Machines&nbsp;<code>A</code>, <code>B</code>, and&nbsp;<code>C</code> are instances of
        <code>(xform f)</code>, a transformer stack, <code>xform</code>, wrapping a reducing function, <code>f</code>. Machine&nbsp;<code>D</code> is
        <em>combining function</em>, <code>combine</code>.
      </p>
      <p>
        The transformer stacks and reducing functions are pretty much those we &nbsp;use with single-threaded <code>transduce</code> and
        <code>transduce-kv</code>. The only difference is that their scope is a partition of the input &nbsp;collection, not the whole; they reduce only part
        of the collection. The &nbsp;reducing function <code>f</code> supplies the initial value for reducing the partition (zero args), the
        &nbsp;&apos;completing&apos; step (one arg), and the reducing step (two args for regular <code>transduce</code> and three args for
        <code>transduce-kv</code>.
      </p>
      <p>
        The combining function is analogous to the transformer stack. The &nbsp;zero-argument arity is invoked to create the initial value of the combining
        &nbsp;process, the one-argument arity is used for the completing step, and the two &nbsp;argument arity performs the combining the left and right
        chunks. (There is &nbsp;no analogous three-argument arity because the key/index is not used in the &nbsp;combining process.)
      </p>
      <p>
        That&apos;s a lot of words. Maybe a table is better.
      </p>
      <table>
        <tr>
          <td></td>
          <td>
            <code>(xform f)</code>
          </td>
          <td>
            <code>combine</code>
          </td>
        </tr>
        <tr>
          <td>
            zero args
          </td>
          <td>
            init for partition
          </td>
          <td>
            init for combining
          </td>
        </tr>
        <tr>
          <td>
            one arg
          </td>
          <td>
            completing for partition
          </td>
          <td>
            completing for combining
          </td>
        </tr>
        <tr>
          <td>
            two args
          </td>
          <td>
            reducing function, accumulating value + element
          </td>
          <td>
            combining function, left value + right value
          </td>
        </tr>
        <tr>
          <td>
            three args
          </td>
          <td>
            reducing function, accumulating value + key/index + element
          </td>
          <td>
            n/a
          </td>
        </tr>
      </table>
      <p>
        Practically, the transformer stacks and reducing functions are the same &nbsp;as we&apos;re used to, and the combining function is concatenation for
        sequential &nbsp;outputs or a merge for associative outputs.
      </p>
      <p>
        Let&apos;s see them in action. For simplicity, we&apos;ll start with plain &nbsp;transduce from the multi-threaded namespace.
      </p>
      <pre><code>(require &apos;[brokvolli.multi :as multi])</code></pre>
      <p>
        We&apos;ll assign ourselves the standard job, incrementing a vector of &nbsp;numbers. Normally, the multi-threaded transducing functions would
        partition &nbsp;at about 512 elements, but for demonstration purposes, we&apos;ll specify three.
      </p>
      <p>
        We want to peel off an element and increment it. That&apos;s a job for a <code>map</code> transducer. Since we&apos;re using the non<code>-kv</code>
        variant, we may use Clojure&apos;s off-the-shelf transducer.
      </p>
      <pre><code>(map inc)</code></pre>
      <p>
        After incrementing, we want to conjoin the result onto the accumulating &nbsp;partition, a new vector. We can use <code>conj</code> to provide the
        initial value (an empty vector), the completing function &nbsp;(identity), and the reducing function (conjoining the element).
      </p>
      <pre><code>(conj) ;; =&gt; []</code><br><code>(conj [:foo :bar]) ;; =&gt; [:foo :bar]</code><br><code>(conj [:foo :bar] :baz) ;; =&gt; [:foo :bar :baz]</code></pre>
      <p>
        Let&apos;s break apart the input vector and calculate the chunks manually.
      </p>
      <pre><code>(transduce (map inc) conj [11 22 33]) ;; =&gt; [12 23 34]</code><br><code>(transduce (map inc) conj [44 55]) ;; =&gt; [45 56]</code><br><code>(transduce (map inc) conj [66 77 88]) ;; =&gt; [67 78 89]</code></pre>
      <p>
        It&apos;s as if we could peek inside and observe what Machines&nbsp;<code>A</code>, <code>B</code>, and <code>C</code> were up to.
      </p>
      <p>
        Now we&apos;ve got three chunks: <code>[12 23 34]</code>, <code>[45 56]</code>, and <code>[67 78 89]</code>. To assemble, we run another reduction
        across those three items, this time &nbsp;using <code>combine</code>. We need an initial value and a step function. Fortunately, <code>concat</code>
        has multiple arities that suit our purposes.
      </p>
      <pre><code>(concat) ;; =&gt; ()</code><br><code>(concat [:foo :bar]) ;; =&gt; (:foo :bar)</code><br><code>(concat [:foo :bar] [:baz]) ;; =&gt; (:foo :bar :baz)</code></pre>
      <p>
        Hmm. <code>concat</code> returns a lazy sequence, which is kinda antithetical to <code>transduce</code>, which is eager. Whaddya know? Brokvolli
        provides an eager concatenating &nbsp;utility that returns vectors.
      </p>
      <pre><code>(concatv) ;; =&gt; []</code><br><code>(concatv [:foo :bar]) ;; =&gt; [:foo :bar]</code><br><code>(concatv [:foo :bar] [:baz]) ;; =&gt; [:foo :bar :baz]</code></pre>
      <p>
        We&apos;ve got all the pieces, let&apos;s put them together and transduce, &nbsp;multi-threaded.
      </p>
      <pre><code>(multi/transduce 3 concatv (map inc) conj [11 22 33 44 55 66 77 88])
;; =&gt; [12 23 34 45 56 67 78 89]</code></pre>
      <p>
        On the surface, not too impressive: we did all the same work as regular, &nbsp;single-threaded transduce, but with extra arguments. Going to that
        trouble &nbsp;becomes <a href="#performance">worth it</a> when the input collection grows large.
      </p>
      <p>
        What does it look like to perform our more-involved job, <em>Increment each number, filter to retain even numbers, halt the process &nbsp;after three
        elements</em>? We know how to increment and filter.
      </p>
      <p>
        That last task, <em>Halt after three elements</em> is a show-stopper. <code>take</code> and friends belong to a class of transducers that are stateful.
        <a href="#stateful-transducers">Stateful transducers</a> are unsafe to use in a multi-threaded transduction. There is currently no mechanism to check
        and prevent us from using a stateful transducer here, but it&apos;s the Brokvolli library&apos;s strong recommendation to avoid it.
      </p>
      <p>
        But it&apos;d be kinda nice to have a third step for our demonstration, so &nbsp;let&apos;s pick a third from the safe set of stateless transducers.
        Let&apos;s &nbsp;stipulate <em>Remove elements if they&apos;re greater than seventy</em>.
      </p>
      <p>
        Our transformer stack will look like this.
      </p>
      <pre><code>(comp (map inc)
&nbsp; (filter even?)
&nbsp; (remove #(&lt; 70 %)))</code></pre>
      <p>
        Let&apos;s try out that transduction.
      </p>
      <pre><code>(multi/transduce 3
&nbsp;                concatv
&nbsp;                (comp (map inc) (filter even?) (remove #(&lt; 70 %)))
&nbsp;                conj
&nbsp;                [11 22 33 44 55 66 77 88])
;; =&gt; [12 34 56]</code></pre>
      <p>
        All the elements were incremented, <code>23</code>, <code>45</code>, <code>67</code>, and <code>89</code> were not retained by <code>filter</code>
        because they&apos;re odd, and <code>78</code> was removed by <code>remove</code> because it&apos;s greater than seventy. Behind the scenes, these tasks
        were done &nbsp;by three light-weight threads, each having only a chunk of the input &nbsp;collection. Once the chunks returned,
        <code>multi/transduce</code> stitched them together into the final output collection. We might visualize &nbsp;the process like this.
      </p>
      <pre><code>input     [11 22 33     44 55     66 77 88]</code><br><code>partition [11 22 33]   [44 55]   [66 77 88]</code><br><code>increment  12 23 34     45 56     67 78 89</code><br><code>filter     12    34        56        78</code><br><code>remove     12    34        56</code><br><code>init    [] 12    34     [] 56</code><br><code>conj      [12]   34       [56]</code><br><code>conj      [12 34]         [56]</code><br><code>init   [] [12 34]         [56]</code><br><code>concatv   [12 34]         [56]</code><br><code>concatv   [12 34 56]</code><br></pre>
      <p>
        We can rest easy that the multi-threaded transducing functions preserve &nbsp;the ordering for sequential input collections.
      </p>
      <p>
        What about multi-threaded <code>transduce-kv</code>? It&apos;s the amalgam of single-threaded <code>transduce-kv</code> and multi-threaded
        <code>transduce</code>. The reducing function, <code>f</code> and transformer stack <code>xform</code> are the same as <a href=
        "#single-transduce-kv"><span>single-threaded <code>transduce-kv</code></span></a>. The combining function is the same as <a href=
        "#multi-transduce"><span>multi-threaded <code>transduce</code></span></a>.
      </p>
      <p>
        Let&apos;s run through a quick demonstration. <em>Given a vector of numbers, increment each element, retain elements &nbsp;whose index is even, and
        remove elements whose index is greater than six.</em>. We&apos;ll break down our job by assembling the argument list from right to &nbsp;left. The
        sub-reductions need to build up a sequential collection, so we&apos;ll &nbsp;rely on <code>tconj</code>. Our transformer stack performs three tasks on
        each element, a mapping, a &nbsp;filtering, and a removing. Each needs to handle <code>-kv</code> inputs. Here&apos;s our stack.
      </p>
      <pre><code>(def xform-7
&nbsp; (comp (map-kv (fn [_ x] (inc x)))
&nbsp;       (filter-kv (fn [idx _] (even? idx)))
&nbsp;       (remove-kv (fn [idx _] (&lt; 6 idx)))))</code></pre>
      <p>
        Finally, we need to supply a combining function to assemble the results of &nbsp;the sub-reductions, which are vectors themselves. <code>concatv</code>
        will do nicely.
      </p>
      <p>
        Altogether, we have this.
      </p>
      <pre><code>(multi/transduce-kv 3 concatv xform-7 tconj [11 22 33 44 55 66 77 88 99])
;; =&gt; [12 34 56 78]</code></pre>
      <p>
        <code>multi/transduce-kv</code> ripped the elements like this.
      </p>
      <pre><code>index   element   inc   result</code><br><strong><code>0       11        12    retained</code></strong><br><code>1       22        23    filtered</code><br><strong><code>2       33        34    retained</code></strong><br><code>3       44        45    filtered</code><br><strong><code>4       55        56    retained</code></strong><br><code>5       66        67    filtered</code><br><strong><code>6       77        78    retained</code></strong><br><code>7       88        89    removed</code><br><code>8       99        100   removed</code></pre>
      <p>
        Behind the scenes, <code>multi/transduce-kv</code> partitioned the input vector into lengths of three or fewer, sent those &nbsp;pieces to a pool where
        light-weight threads performed the sub-transductions, &nbsp;then, once those results were finished, concatenated the results from the
        &nbsp;sub-reductions. The final result is exactly as if we had used single-threaded <code>transduce-kv</code>.
      </p>
      <pre><code>(require &apos;[brokvolli.single :as single])</code><br><br><code>(single/transduce-kv xform-7 tconj [11 22 33 44 55 66 77 88 99])
;; =&gt; [12 34 56 78]</code><br><br><code>(let [test (fn [f] (f xform-7 tconj [11 22 33 44 55 66 77 88 99]))]
&nbsp; (= (test single/transduce-kv) (test multi/transduce-kv)))
;; =&gt; true</code></pre>
      <p>
        Don&apos;t make any assumptions about where the multi-threaded transduce &nbsp;functions partition the input collection. That&apos;s an implementation
        detail and &nbsp;subject to change without notice.
      </p>
      <h4 id="stateful-transducers">
        Multi-threaded transduction and stateful &nbsp;transducers
      </h4>
      <p>
        Some transducers are stateful, and therefore will most likely give &nbsp;incorrect results when using the multi-threaded transduce functions. For
        &nbsp;example, <code>take</code> (and its <code>-kv</code> variant) is stateful. Here&apos;s what happens when used with multi-threaded
        <code>transduce</code>.
      </p>
      <pre><code>(multi/transduce 3 concatv (take 1) tconj [11 22 33 44 55 66 77 88 99])
;; =&gt; [11 33 55 77]</code></pre>
      <p>
        We intended to halt the transduction after collecting the first element. &nbsp;However, multiple threads took the first element of their individual
        &nbsp;partitions, ignorant that the same thing was happening on sibling threads. &nbsp;Then, <code>transduce</code> dutifully concatenated all the
        results, returning an unintended result: a &nbsp;vector with more than one element.
      </p>
      <p>
        There is no mechanism to check or stop us from using stateful transducers &nbsp;with the multi-threaded transduce functions. We must rely on policy and
        &nbsp;discipline.
      </p>
      <p>
        The following chart categorizes Clojure&apos;s off-the-shelf transducers.
      </p>
      <table>
        <tr>
          <th>
            stateless
          </th>
          <th>
            stateful
          </th>
        </tr>
        <tr>
          <td>
            cat
          </td>
          <td>
            dedupe
          </td>
        </tr>
        <tr>
          <td>
            filter
          </td>
          <td>
            distinct
          </td>
        </tr>
        <tr>
          <td>
            keep
          </td>
          <td>
            drop
          </td>
        </tr>
        <tr>
          <td>
            map
          </td>
          <td>
            drop-while
          </td>
        </tr>
        <tr>
          <td>
            mapcat
          </td>
          <td>
            interpose
          </td>
        </tr>
        <tr>
          <td>
            random-sample
          </td>
          <td>
            partition-all
          </td>
        </tr>
        <tr>
          <td>
            remove
          </td>
          <td>
            partition-by
          </td>
        </tr>
        <tr>
          <td>
            replace
          </td>
          <td>
            take
          </td>
        </tr>
        <tr>
          <td></td>
          <td>
            take-nth
          </td>
        </tr>
        <tr>
          <td></td>
          <td>
            take-while-kv
          </td>
        </tr>
      </table>
      <p>
        Brokvolli&apos;s <code>-kv</code> transducer variants also include stateful transducers. Those that are not &nbsp;unconditionally safe to use with
        multi-threaded transduce functions are &nbsp;separated into the <code>stateful-transducers-kv</code> namespace to remind us.
      </p>
      <h4>
        Halting headaches
      </h4>
      <p>
        The multi-threaded transduce functions ultimately delegate their &nbsp;partitions to <code>reduce</code> and <code>reduce-kv</code>. There are two
        mechanisms for breaking out of the transduction process: &nbsp;wrapping the return value in <code>reduced</code> is mostly for implementing a
        transducing function, and the <code>halt-when</code> transducer, mostly for the user. The multi-threaded Brokvolli functions are &nbsp;not currently
        able respect either early termination mechanism when the input &nbsp;collection is partitioned into multiple pieces.
      </p>
      <h3 id="avoid">
        Avoids
      </h3>
      <ul>
        <li>
          <p>
            Don&apos;t use multi-threaded variants as an ersatz async scheduler. Use &nbsp;instead futures, promises, or proper threads, etc.
          </p>
        </li>
        <li>
          <p>
            Don&apos;t do any I/O or other side-effects. Not what it&apos;s for. Only do &nbsp;pure computation.
          </p>
        </li>
      </ul>
    </section>
    <section id="quick">
      <h2>
        Quick reference
      </h2>
      <table>
        <tr>
          <th>
            prototype
          </th>
          <th>
            single-threaded
          </th>
          <th>
            multi-threaded
          </th>
        </tr>
        <tr>
          <td>
            <code>reduce</code>
          </td>
          <td>
            <code>clojure.core/transduce</code>
          </td>
          <td>
            <code>brokvolli.multi/transduce</code>
          </td>
        </tr>
        <tr>
          <td>
            <code>reduce-kv</code>
          </td>
          <td>
            <code>brokvolli.single/transduce-kv</code>
          </td>
          <td>
            <code>brokvolli.multi/transduce-kv</code>
          </td>
        </tr>
      </table>
      <h3>
        Signatures
      </h3>
      <table>
        <tr>
          <td>
            <pre><code>clojure.core/transduce &amp;</code><br><code>brokvolli.single/transduce-kv</code></pre>
          </td>
          <td>
            <pre><code>[xform f      coll]</code><br><code>[xform f init coll]</code></pre>
          </td>
          <td>
            <p>
              <em>Like <code>reduce</code>, but with <code>xform</code> at front.</em>
            </p>
          </td>
        </tr>
        <tr>
          <td>
            <pre><code>brokvolli.multi/transduce &amp;</code><br><code>brokvolli.multi/transduce-kv</code></pre>
          </td>
          <td>
            <code>[&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;xform&nbsp;f&nbsp;coll]</code><br>
            <code>[&nbsp;&nbsp;combine&nbsp;xform&nbsp;f&nbsp;coll]</code><br>
            <code>[n&nbsp;combine&nbsp;xform&nbsp;f&nbsp;coll]</code>
          </td>
          <td>
            <p>
              <em>Expands from right-to-left, roughly in order of importance.</em>
            </p>
          </td>
        </tr>
      </table>
    </section>
    <section id="performance">
      <h2>
        Performance
      </h2>
      <p>
        ...
      </p>
    </section>
    <section id="dont">
      <h2>
        To don&apos;t
      </h2>
      <p>
        Non-goals, etc.
      </p>
      <ul>
        <li>
          <p>
            Won&apos;t stop you from using stateful transducers in a multi-threaded context. User must know enough to avoid. Or stick to single-threaded
            variants.
          </p>
        </li>
        <li>
          <p>
            multiple collections a la `map`
          </p>
        </li>
        <li>
          <p>
            All possible permutations of function signatures.
          </p>
        </li>
        <li>
          <p>
            non-sequential steps: sliding window, first+last, second+second-last, etc.
          </p>
        </li>
      </ul>
    </section>
    <section id="alternatives">
      <h2>
        Alternatives
      </h2>
      <ul>
        <li>
          <p>
            Clojure&apos;s <a href="https://clojure.github.io/clojure/clojure.core-api.html#clojure.core/reduce"><code>reduce</code></a>, <a href=
            "https://clojure.github.io/clojure/clojure.core-api.html#clojure.core/reduce-kv"><code>reduce-kv</code></a>, <a href=
            "https://clojure.github.io/clojure/clojure.core-api.html#clojure.core/transduce"><code>transduce</code></a>, and <a href=
            "https://clojure.github.io/clojure/clojure.core-api.html#clojure.core.reducers/fold"><code>clojure.core.reducers/fold</code></a>
          </p>
          <p>
            No additional dependencies, proven in-the-wild.
          </p><br>
        </li>
        <li>
          <p>
            Sebastian Fedrau&apos;s <a href="https://github.com/20centaurifux/pold">pold</a>
          </p>
          <p>
            A Clojure library for efficiently dividing data into any number of partitions and accumulating them into a result.
          </p><br>
        </li>
        <li>
          <p>
            Kyle Kingsbury&apos;s <a href="https://github.com/aphyr/tesser">Tesser</a>
          </p>
          <p>
            A Clojure library for concurrent &amp; commutative folds.
          </p><br>
        </li>
        <li>
          <p>
            Christophe Grand&apos;s <a href="https://github.com/cgrand/xforms">xforms</a>
          </p>
          <p>
            More transducers and reducing functions for Clojure(script).
          </p><br>
        </li>
      </ul>
    </section>
    <section id="glossary">
      <h2>
        Glossary
      </h2>
      <dl>
        <dt id="accumulator">
          accumulator
        </dt>
        <dd>
          <p>
            Synonym: <em>accumulating value</em>. The on-going value that is produced by evaluating the <a href="#reducing-function">reducing function</a> with
            all the previous elements. The <code>acc</code> in a reducing function&apos;s signatures,
            <code>(fn&nbsp;[<strong>acc</strong>&nbsp;element]&nbsp;...)</code>.
          </p>
        </dd>
        <dt id="combine">
          combine
        </dt>
        <dd>
          <p>
            Gather the results after <a href="#reduce">reducing</a> two or more partitions of the input collection. Often a concatenation (for sequentials) or
            merging (for associatives), but need not be. A <em>combining function</em> is a function that implements this operation.
          </p>
        </dd>
        <dt id="element">
          element
        </dt>
        <dd>
          <p>
            A member of a collection. Within a <a href="#reduce"><code>reduce</code></a>-style operation, the next &quot;thing&quot; <code>reduce</code> peels
            off the collection and sends to its <a href="#reducing-function">reducing function</a>.
          </p>
        </dd>
        <dt id="inner">
          inner function/predicate
        </dt>
        <dd>
          <p>
            A function used by a transducer to do its work. The&nbsp;<code>f</code> in <code>(map&nbsp;f)</code>, or the <code>pred</code> in
            <code>(filter&nbsp;pred)</code>.
          </p>
        </dd>
        <dt id="keydex">
          keydex
        </dt>
        <dd>
          <p>
            Short-hand for <em>key/index</em>. A key &quot;locates&quot; an element contained in an associative collection, while an integer index do so within
            a sequential collection.
          </p>
        </dd>
        <dt id="reducing-function">
          reducing function
        </dt>
        <dd>
          <p>
            A function that does the work in a <a href="#reduce">reduce</a> operation, <em>i.e.,</em> <code>f</code> in
            <code>(reduce&nbsp;<strong>f</strong>&nbsp;coll)</code>. In a transduce operation, the ultimate function at the &quot;bottom&quot; of the
            transducing <a href="#stack">stack</a>, i.e., the <code>f</code> in <code>(transduce&nbsp;xform&nbsp;<strong>f</strong>&nbsp;coll)</code>.
          </p>
          <p>
            In a <code>reduce</code> operation, <code>f</code>&apos;s signature is <code>(fn&nbsp;[acc&nbsp;element]&nbsp;...)</code>. In a
            <code>reduce-kv</code> operation, <code>f</code>&apos;s signature is <code>(fn&nbsp;[acc&nbsp;keydex&nbsp;element]&nbsp;...)</code>. In a transduce
            (both &quot;plain&quot; and &quot;<code>-kv</code>&quot;) operation, <code>f</code>&apos;s signature is
            <code>(fn&nbsp;[acc&nbsp;result]&nbsp;...)</code>.
          </p>
        </dd>
        <dt id="reduce">
          reduce
        </dt>
        <dd>
          <p>
            The process of consuming an <a href="#accumulator">accumulating value</a> and the next <a href="#element">element</a>, producing a new accumulating
            value. Note: A reduce operation may result in fewer, equal, or more elements than the original input.
          </p>
        </dd>
        <dt id="stack">
          stack
        </dt>
        <dd>
          <p>
            A composition of one or more <a href="#transducer">transducing functions</a>, often made with <a href=
            "https://clojure.github.io/clojure/clojure.core-api.html#clojure.core/comp"><code>comp</code></a>. The <code>xform</code> in
            <code>(transduce&nbsp;<strong>xform</strong>&nbsp;f&nbsp;coll)</code>. When discussing the mechanical execution of a transduction, may refer to
            <code>(xform&nbsp;f)</code>.
          </p>
        </dd>
        <dt id="transducer">
          transducer
        </dt>
        <dd>
          <p>
            Synonym: <em>transducing function</em>. A function that modifies (<em>i.e.,</em> <a href="#transform">&quot;transforms&quot;</a>) a reducing
            function or another transducer. The <code>xform</code> in <code>(transduce&nbsp;<strong>xform</strong>&nbsp;f&nbsp;coll)</code>. Practically, a
            sequence function&apos;s <code>coll</code>-omitted arity, e.g., <a href=
            "https://clojure.github.io/clojure/clojure.core-api.html#clojure.core/map">(map&nbsp;f)</a>, <a href=
            "https://clojure.github.io/clojure/clojure.core-api.html#clojure.core/filter">(filter&nbsp;pred)</a>, <a href=
            "https://clojure.github.io/clojure/clojure.core-api.html#clojure.core/take">(take&nbsp;n)</a>, etc.
          </p>
        </dd>
        <dt id="transduce">
          transduce
        </dt>
        <dd>
          <p>
            To eagerly reduce over a concrete collection with a transducing stack serving as the reducing function. One of Clojure&apos;s off-the-shelf
            transducing contexts.
          </p>
        </dd>
        <dt id="transform">
          transform
        </dt>
        <dd>
          <p>
            To alter a <a href="#reducing-function">reducing function</a> by &quot;wrapping&quot; it in one or more <a href="#transducer">transducers</a>.
          </p>
        </dd>
      </dl>
    </section><br>
    <h2>
      License
    </h2>
    <p></p>
    <p>
      This program and the accompanying materials are made available under the terms of the <a href="https://opensource.org/license/MIT">MIT License</a>.
    </p>
    <p></p>
    <p id="page-footer">
      Copyright © 2024–2026 Brad Losavio.<br>
      Compiled by <a href="https://github.com/blosavio/readmoi">ReadMoi</a> on 2026 February 13.<span id="uuid"><br>
      294419b4-984b-4fdb-bd80-9737707339c6</span>
    </p>
  </body>
</html>
