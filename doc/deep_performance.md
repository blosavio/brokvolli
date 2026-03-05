
  <body>
    <h1>
      Addendum 1: Benchmarking a single, deep transduction
    </h1>
    <div>
      <a href="#group-0">Construct hashmap of eighteen mathematical ops, per element</a>
    </div>
    <div>
      <h2>
        Do Brokvolli&apos;s <code>transduce</code> benchmarks change with a heavier per-element task?
      </h2>
      <p>
        <em>Observations: The multi-threaded variants&apos; performance improvements increase when the tasks are heavier.</em>
      </p>
      <p>
        See also:
      </p>
      <ul>
        <li>
          <a href="https://blosavio.github.io/brokvolli/transductions.html">Main benchmarks</a>
        </li>
        <li>
          <a href="https://blosavio.github.io/brokvolli/partitions.html">Addendum 2: Effect of partition size</a>
        </li>
        <li>
          <a href="https://blosavio.github.io/brokvolli/processors.html">Addendum 3: Performance scaling with processors</a>
        </li>
      </ul>
      <p></p>
      <p>
        We&apos;ll define our benchmarks <a href="https://github.com/blosavio/brokvolli/blob/main/test/brokvolli/performance/deep.clj">here</a>. For each of
        these functions,
      </p>
      <ul>
        <li>
          <code>clojure.core/reduce</code>
        </li>
        <li>
          <code>clojure.core/reduce-kv</code>
        </li>
        <li>
          <code>clojure.core.reducers/fold</code> (multi-threaded)
        </li>
        <li>
          <code>clojure.core/transduce</code>
        </li>
        <li>
          <code>brokvolli.single/transduce-kv</code>
        </li>
        <li>
          <code>brokvolli.multi/transduce</code> (multi-threaded)
        </li>
        <li>
          <code>brokvolli.multi/transduce-kv</code> (multi-threaded)
        </li>
      </ul>We&apos;ll test vectors increasing in length from one element to one-hundred-thousand elements, by powers of ten. For each element, a pre-generated
      random floating point number, we&apos;ll construct a hashmap of eighteen mathematical operations on that number (trig ops, logarithms, etc.) that the JVM
      compiler oughtn&apos;t be able to optimize. We&apos;ll use the <a href="https://github.com/hugoduncan/criterium/">Criterium benchmarking library</a> to
      measure the execution times of sixty repetitions of each condition. Benchmarks were run on three explicitly-pinned cores of my geriatric desktop
      computer.
      <p></p>
      <p>
        Overall, we observe that the execution times increase with increasing vector lengths. The results are indistinguishable when the vector contains
        one-hundred or fewer elements. When the vectors grow longer, the three multi-threaded variants (<code>fold</code>, <code>multi/transduce</code>, and
        <code>multi/transduce‑kv</code>), offer improvements that scale roughly with the number of processors. The single-threaded functions all perform very
        similarly. Not surprising, since they all ultimately delegate to the same underlying implementation, <code>reduce/reduce-kv</code>. Unfortunately, my
        computer was not capable of handling one-million element vectors and I was compelled to stop measuring at one-hundred-thousand elements.
      </p>
      <p>
        As with the previous benchmarks, the multi-threaded functions appear to offer performance benefits over their single-threaded counterparts for large
        collection sizes and heavy per-element operations. For smaller collections or shallow transformer stacks, the single-threaded variants perform better,
        and present a simpler interface.
      </p>
    </div>
    <section>
      <h3 id="group-0">
        Construct hashmap of eighteen mathematical ops, per element
      </h3>
      <div>
        <p>
          This test performs multiple mathematical operations per element, <em>sine</em>, <em>square-root</em>, <em>logarithm</em>, <a href=
          "https://github.com/blosavio/brokvolli/blob/accd4a2f4fec60092beb6cff27a2d2414ed67033/test/brokvolli/performance/deep.clj#L37-L57">etc</a>.
        </p>
        <p>
          Execution times increase with vector length. The single- and multi-threaded functions are indistinguishable for one-hundred elements or fewer. When
          the vectors grow larger than that, the multi-threaded variants demonstrate faster execution (i.e., lower times), roughly by a factor of two.
        </p>
      </div>
      <div>
        <h4 id="group-0-fexpr-0">
          (fn [n] ((tactics-1 (project-version-lein)) (vecs n)))
        </h4><img alt=
        "Benchmark measurements for expression `(fn [n] ((tactics-1 (project-version-lein)) (vecs n)))`, time versus &apos;n&apos; arguments, comparing different versions."
        src="deep_img/group-0-fexpr-0.svg"><button class="collapser" type="button">Show details</button>
        <div class="collapsable">
          <table>
            <caption>
              times in seconds, <em>mean±std</em>
            </caption>
            <thead>
              <tr>
                <td></td>
                <th colspan="6">
                  arg, n
                </th>
              </tr>
              <tr>
                <th>
                  version
                </th>
                <th>
                  1
                </th>
                <th>
                  10
                </th>
                <th>
                  100
                </th>
                <th>
                  1000
                </th>
                <th>
                  10000
                </th>
                <th>
                  100000
                </th>
              </tr>
            </thead>
            <tr>
              <td>
                core-transduce
              </td>
              <td>
                <a href="https://github.com/blosavio/brokvolli/blob/main/resources/deep_performance/version core-transduce/test-0.edn">1.4e-04±5.8e-06</a>
              </td>
              <td>
                <a href="https://github.com/blosavio/brokvolli/blob/main/resources/deep_performance/version core-transduce/test-1.edn">1.5e-04±7.5e-06</a>
              </td>
              <td>
                <a href="https://github.com/blosavio/brokvolli/blob/main/resources/deep_performance/version core-transduce/test-2.edn">2.9e-04±1.4e-05</a>
              </td>
              <td>
                <a href="https://github.com/blosavio/brokvolli/blob/main/resources/deep_performance/version core-transduce/test-3.edn">1.8e-03±1.3e-04</a>
              </td>
              <td>
                <a href="https://github.com/blosavio/brokvolli/blob/main/resources/deep_performance/version core-transduce/test-4.edn">2.0e-02±2.1e-03</a>
              </td>
              <td>
                <a href="https://github.com/blosavio/brokvolli/blob/main/resources/deep_performance/version core-transduce/test-5.edn">2.4e-01±4.0e-02</a>
              </td>
            </tr>
            <tr>
              <td>
                fold
              </td>
              <td>
                <a href="https://github.com/blosavio/brokvolli/blob/main/resources/deep_performance/version fold/test-0.edn">1.3e-04±8.4e-06</a>
              </td>
              <td>
                <a href="https://github.com/blosavio/brokvolli/blob/main/resources/deep_performance/version fold/test-1.edn">1.4e-04±7.4e-06</a>
              </td>
              <td>
                <a href="https://github.com/blosavio/brokvolli/blob/main/resources/deep_performance/version fold/test-2.edn">2.9e-04±1.9e-05</a>
              </td>
              <td>
                <a href="https://github.com/blosavio/brokvolli/blob/main/resources/deep_performance/version fold/test-3.edn">1.2e-03±9.1e-05</a>
              </td>
              <td>
                <a href="https://github.com/blosavio/brokvolli/blob/main/resources/deep_performance/version fold/test-4.edn">8.0e-03±1.4e-03</a>
              </td>
              <td>
                <a href="https://github.com/blosavio/brokvolli/blob/main/resources/deep_performance/version fold/test-5.edn">1.2e-01±2.5e-02</a>
              </td>
            </tr>
            <tr>
              <td>
                multi-transduce
              </td>
              <td>
                <a href="https://github.com/blosavio/brokvolli/blob/main/resources/deep_performance/version multi-transduce/test-0.edn">1.4e-04±1.2e-05</a>
              </td>
              <td>
                <a href="https://github.com/blosavio/brokvolli/blob/main/resources/deep_performance/version multi-transduce/test-1.edn">1.4e-04±8.2e-06</a>
              </td>
              <td>
                <a href="https://github.com/blosavio/brokvolli/blob/main/resources/deep_performance/version multi-transduce/test-2.edn">2.9e-04±1.7e-05</a>
              </td>
              <td>
                <a href="https://github.com/blosavio/brokvolli/blob/main/resources/deep_performance/version multi-transduce/test-3.edn">1.2e-03±1.1e-04</a>
              </td>
              <td>
                <a href="https://github.com/blosavio/brokvolli/blob/main/resources/deep_performance/version multi-transduce/test-4.edn">8.2e-03±1.5e-03</a>
              </td>
              <td>
                <a href="https://github.com/blosavio/brokvolli/blob/main/resources/deep_performance/version multi-transduce/test-5.edn">1.2e-01±2.6e-02</a>
              </td>
            </tr>
            <tr>
              <td>
                multi-transduce-kv
              </td>
              <td>
                <a href="https://github.com/blosavio/brokvolli/blob/main/resources/deep_performance/version multi-transduce-kv/test-0.edn">1.3e-04±6.3e-06</a>
              </td>
              <td>
                <a href="https://github.com/blosavio/brokvolli/blob/main/resources/deep_performance/version multi-transduce-kv/test-1.edn">1.5e-04±7.1e-06</a>
              </td>
              <td>
                <a href="https://github.com/blosavio/brokvolli/blob/main/resources/deep_performance/version multi-transduce-kv/test-2.edn">2.9e-04±1.8e-05</a>
              </td>
              <td>
                <a href="https://github.com/blosavio/brokvolli/blob/main/resources/deep_performance/version multi-transduce-kv/test-3.edn">1.2e-03±6.9e-05</a>
              </td>
              <td>
                <a href="https://github.com/blosavio/brokvolli/blob/main/resources/deep_performance/version multi-transduce-kv/test-4.edn">1.0e-02±1.9e-03</a>
              </td>
              <td>
                <a href="https://github.com/blosavio/brokvolli/blob/main/resources/deep_performance/version multi-transduce-kv/test-5.edn">1.4e-01±3.5e-02</a>
              </td>
            </tr>
            <tr>
              <td>
                reduce
              </td>
              <td>
                <a href="https://github.com/blosavio/brokvolli/blob/main/resources/deep_performance/version reduce/test-0.edn">1.3e-04±9.9e-06</a>
              </td>
              <td>
                <a href="https://github.com/blosavio/brokvolli/blob/main/resources/deep_performance/version reduce/test-1.edn">1.5e-04±1.5e-05</a>
              </td>
              <td>
                <a href="https://github.com/blosavio/brokvolli/blob/main/resources/deep_performance/version reduce/test-2.edn">2.9e-04±1.9e-05</a>
              </td>
              <td>
                <a href="https://github.com/blosavio/brokvolli/blob/main/resources/deep_performance/version reduce/test-3.edn">1.8e-03±1.9e-04</a>
              </td>
              <td>
                <a href="https://github.com/blosavio/brokvolli/blob/main/resources/deep_performance/version reduce/test-4.edn">1.9e-02±2.6e-03</a>
              </td>
              <td>
                <a href="https://github.com/blosavio/brokvolli/blob/main/resources/deep_performance/version reduce/test-5.edn">2.3e-01±3.8e-02</a>
              </td>
            </tr>
            <tr>
              <td>
                reduce-kv
              </td>
              <td>
                <a href="https://github.com/blosavio/brokvolli/blob/main/resources/deep_performance/version reduce-kv/test-0.edn">1.3e-04±6.7e-06</a>
              </td>
              <td>
                <a href="https://github.com/blosavio/brokvolli/blob/main/resources/deep_performance/version reduce-kv/test-1.edn">1.5e-04±8.2e-06</a>
              </td>
              <td>
                <a href="https://github.com/blosavio/brokvolli/blob/main/resources/deep_performance/version reduce-kv/test-2.edn">3.1e-04±4.0e-05</a>
              </td>
              <td>
                <a href="https://github.com/blosavio/brokvolli/blob/main/resources/deep_performance/version reduce-kv/test-3.edn">1.8e-03±1.3e-04</a>
              </td>
              <td>
                <a href="https://github.com/blosavio/brokvolli/blob/main/resources/deep_performance/version reduce-kv/test-4.edn">1.9e-02±2.8e-03</a>
              </td>
              <td>
                <a href="https://github.com/blosavio/brokvolli/blob/main/resources/deep_performance/version reduce-kv/test-5.edn">2.3e-01±3.4e-02</a>
              </td>
            </tr>
            <tr>
              <td>
                single-transduce-kv
              </td>
              <td>
                <a href="https://github.com/blosavio/brokvolli/blob/main/resources/deep_performance/version single-transduce-kv/test-0.edn">1.3e-04±8.4e-06</a>
              </td>
              <td>
                <a href="https://github.com/blosavio/brokvolli/blob/main/resources/deep_performance/version single-transduce-kv/test-1.edn">1.6e-04±1.8e-05</a>
              </td>
              <td>
                <a href="https://github.com/blosavio/brokvolli/blob/main/resources/deep_performance/version single-transduce-kv/test-2.edn">3.1e-04±1.5e-05</a>
              </td>
              <td>
                <a href="https://github.com/blosavio/brokvolli/blob/main/resources/deep_performance/version single-transduce-kv/test-3.edn">1.8e-03±1.5e-04</a>
              </td>
              <td>
                <a href="https://github.com/blosavio/brokvolli/blob/main/resources/deep_performance/version single-transduce-kv/test-4.edn">1.8e-02±3.1e-03</a>
              </td>
              <td>
                <a href="https://github.com/blosavio/brokvolli/blob/main/resources/deep_performance/version single-transduce-kv/test-5.edn">2.3e-01±3.6e-02</a>
              </td>
            </tr>
          </table>
        </div>
      </div>
      <hr>
    </section>
    <p id="page-footer">
      Copyright © 2024–2026 Brad Losavio.<br>
      Compiled by <a href="https://github.com/blosavio/Fastester">Fastester</a> on 2026 March 05.<span id="uuid"><br>
      1ff123be-d29c-42fc-8dbe-2bd794272c59</span>
    </p>
  </body>
</html>
