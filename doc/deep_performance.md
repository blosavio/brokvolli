
  <body>
    <h1>
      Addendum 1: Benchmarking a single, deep transduction
    </h1>
    <div>
      <a href="#group-0">Construct hashmap of eighteen mathematical ops, per element</a>
    </div>
    <div>
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
        We&apos;ll define our benchmarks <a href="https://github.com/blosavio/brokvolli/blob/main/test/brokvolli/performance/deep.clj">here</a>. For each
        function below, we&apos;ll test vectors of random floating point numbers, increasing in length from one to one-hundred-thousand, by powers of ten. For
        each number, We&apos;ll construct a hashmap of eighteen mathematical operations on that number (trig ops, logarithms, etc.) that the JVM compiler
        oughtn&apos;t be able to optimize. We&apos;ll use the <a href="https://github.com/hugoduncan/criterium/">Criterium benchmarking library</a> to measure
        the execution times of sixty repetitions of each condition. Benchmarks were run on three pinned cores of my old, rusty computer.
      </p>
      <p>
        The functions under examination are as follows:
      </p>
      <ul>
        <li>
          <p>
            <code>clojure.core/reduce</code>
          </p>
        </li>
        <li>
          <p>
            <code>clojure.core/reduce-kv</code>
          </p>
        </li>
        <li>
          <p>
            <code>clojure.core/transduce</code>
          </p>
        </li>
        <li>
          <p>
            <code>brokvolli.single/transduce-kv</code>
          </p>
        </li>
        <li>
          <p>
            <code>brokvolli.multi/transduce</code>
          </p>
        </li>
        <li>
          <p>
            <code>brokvolli.multi/transduce-kv</code>
          </p>
        </li>
      </ul>
      <p></p>
      <p>
        Overall, we observe that the execution times increase with increasing vector lengths. The results are indistinguishable when the vector contains
        one-hundred or fewer elements. When the vectors grow longer the three multi-threaded variants (<code>fold</code>, <code>multi/transduce</code>, and
        <code>multi/transduce-kv</code>), offer improvements that scale roughly with the number of processors. The other functions all perform very similarly.
        Not surprising, since they all ultimately delegate to the same underlying implementation, <code>reduce/reduce-kv</code>. Unfortunately, my computer was
        not capable of handling one-million element vectors.
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
          This test performs multiple mathematical operations per element, <em>sine</em>, <em>square-root</em>, <em>logarithm</em>, etc.
        </p>
        <p>
          Execution times increase with vector length. The single- and multi-threaded functions are indistinguishable for one-hundred elements or fewer. When
          the vectors grow larger than that, the multi-threaded variants demonstrate faster execution (i.e., lower times).
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
                <a href="https://github.com/blosavio/brokvolli/blob/main/resources/deep_performance/version core-transduce/test-0.edn">1.1e-04±1.5e-06</a>
              </td>
              <td>
                <a href="https://github.com/blosavio/brokvolli/blob/main/resources/deep_performance/version core-transduce/test-1.edn">1.3e-04±2.5e-06</a>
              </td>
              <td>
                <a href="https://github.com/blosavio/brokvolli/blob/main/resources/deep_performance/version core-transduce/test-2.edn">2.7e-04±7.5e-06</a>
              </td>
              <td>
                <a href="https://github.com/blosavio/brokvolli/blob/main/resources/deep_performance/version core-transduce/test-3.edn">2.0e-03±1.0e-04</a>
              </td>
              <td>
                <a href="https://github.com/blosavio/brokvolli/blob/main/resources/deep_performance/version core-transduce/test-4.edn">2.2e-02±2.2e-03</a>
              </td>
              <td>
                <a href="https://github.com/blosavio/brokvolli/blob/main/resources/deep_performance/version core-transduce/test-5.edn">2.3e-01±3.9e-02</a>
              </td>
            </tr>
            <tr>
              <td>
                fold
              </td>
              <td>
                <a href="https://github.com/blosavio/brokvolli/blob/main/resources/deep_performance/version fold/test-0.edn">1.1e-04±5.1e-07</a>
              </td>
              <td>
                <a href="https://github.com/blosavio/brokvolli/blob/main/resources/deep_performance/version fold/test-1.edn">1.4e-04±9.8e-06</a>
              </td>
              <td>
                <a href="https://github.com/blosavio/brokvolli/blob/main/resources/deep_performance/version fold/test-2.edn">2.7e-04±9.3e-06</a>
              </td>
              <td>
                <a href="https://github.com/blosavio/brokvolli/blob/main/resources/deep_performance/version fold/test-3.edn">1.1e-03±7.3e-05</a>
              </td>
              <td>
                <a href="https://github.com/blosavio/brokvolli/blob/main/resources/deep_performance/version fold/test-4.edn">9.7e-03±2.5e-03</a>
              </td>
              <td>
                <a href="https://github.com/blosavio/brokvolli/blob/main/resources/deep_performance/version fold/test-5.edn">1.5e-01±4.2e-02</a>
              </td>
            </tr>
            <tr>
              <td>
                multi-transduce
              </td>
              <td>
                <a href="https://github.com/blosavio/brokvolli/blob/main/resources/deep_performance/version multi-transduce/test-0.edn">1.2e-04±4.3e-06</a>
              </td>
              <td>
                <a href="https://github.com/blosavio/brokvolli/blob/main/resources/deep_performance/version multi-transduce/test-1.edn">1.3e-04±1.0e-06</a>
              </td>
              <td>
                <a href="https://github.com/blosavio/brokvolli/blob/main/resources/deep_performance/version multi-transduce/test-2.edn">2.7e-04±1.0e-05</a>
              </td>
              <td>
                <a href="https://github.com/blosavio/brokvolli/blob/main/resources/deep_performance/version multi-transduce/test-3.edn">1.2e-03±1.1e-04</a>
              </td>
              <td>
                <a href="https://github.com/blosavio/brokvolli/blob/main/resources/deep_performance/version multi-transduce/test-4.edn">9.9e-03±3.8e-03</a>
              </td>
              <td>
                <a href="https://github.com/blosavio/brokvolli/blob/main/resources/deep_performance/version multi-transduce/test-5.edn">1.5e-01±2.9e-02</a>
              </td>
            </tr>
            <tr>
              <td>
                multi-transduce-kv
              </td>
              <td>
                <a href="https://github.com/blosavio/brokvolli/blob/main/resources/deep_performance/version multi-transduce-kv/test-0.edn">1.2e-04±8.3e-06</a>
              </td>
              <td>
                <a href="https://github.com/blosavio/brokvolli/blob/main/resources/deep_performance/version multi-transduce-kv/test-1.edn">1.3e-04±3.6e-06</a>
              </td>
              <td>
                <a href="https://github.com/blosavio/brokvolli/blob/main/resources/deep_performance/version multi-transduce-kv/test-2.edn">2.8e-04±8.9e-06</a>
              </td>
              <td>
                <a href="https://github.com/blosavio/brokvolli/blob/main/resources/deep_performance/version multi-transduce-kv/test-3.edn">1.2e-03±1.4e-04</a>
              </td>
              <td>
                <a href="https://github.com/blosavio/brokvolli/blob/main/resources/deep_performance/version multi-transduce-kv/test-4.edn">9.6e-03±3.4e-03</a>
              </td>
              <td>
                <a href="https://github.com/blosavio/brokvolli/blob/main/resources/deep_performance/version multi-transduce-kv/test-5.edn">1.5e-01±3.3e-02</a>
              </td>
            </tr>
            <tr>
              <td>
                reduce
              </td>
              <td>
                <a href="https://github.com/blosavio/brokvolli/blob/main/resources/deep_performance/version reduce/test-0.edn">1.1e-04±3.5e-06</a>
              </td>
              <td>
                <a href="https://github.com/blosavio/brokvolli/blob/main/resources/deep_performance/version reduce/test-1.edn">1.2e-04±2.9e-06</a>
              </td>
              <td>
                <a href="https://github.com/blosavio/brokvolli/blob/main/resources/deep_performance/version reduce/test-2.edn">2.7e-04±8.4e-06</a>
              </td>
              <td>
                <a href="https://github.com/blosavio/brokvolli/blob/main/resources/deep_performance/version reduce/test-3.edn">1.8e-03±1.7e-04</a>
              </td>
              <td>
                <a href="https://github.com/blosavio/brokvolli/blob/main/resources/deep_performance/version reduce/test-4.edn">2.1e-02±3.6e-03</a>
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
                <a href="https://github.com/blosavio/brokvolli/blob/main/resources/deep_performance/version reduce-kv/test-0.edn">1.2e-04±9.7e-06</a>
              </td>
              <td>
                <a href="https://github.com/blosavio/brokvolli/blob/main/resources/deep_performance/version reduce-kv/test-1.edn">1.3e-04±1.1e-06</a>
              </td>
              <td>
                <a href="https://github.com/blosavio/brokvolli/blob/main/resources/deep_performance/version reduce-kv/test-2.edn">2.8e-04±1.2e-05</a>
              </td>
              <td>
                <a href="https://github.com/blosavio/brokvolli/blob/main/resources/deep_performance/version reduce-kv/test-3.edn">1.9e-03±1.7e-04</a>
              </td>
              <td>
                <a href="https://github.com/blosavio/brokvolli/blob/main/resources/deep_performance/version reduce-kv/test-4.edn">2.3e-02±3.8e-03</a>
              </td>
              <td>
                <a href="https://github.com/blosavio/brokvolli/blob/main/resources/deep_performance/version reduce-kv/test-5.edn">2.4e-01±3.5e-02</a>
              </td>
            </tr>
            <tr>
              <td>
                single-transduce-kv
              </td>
              <td>
                <a href="https://github.com/blosavio/brokvolli/blob/main/resources/deep_performance/version single-transduce-kv/test-0.edn">1.2e-04±2.9e-06</a>
              </td>
              <td>
                <a href="https://github.com/blosavio/brokvolli/blob/main/resources/deep_performance/version single-transduce-kv/test-1.edn">1.3e-04±4.7e-06</a>
              </td>
              <td>
                <a href="https://github.com/blosavio/brokvolli/blob/main/resources/deep_performance/version single-transduce-kv/test-2.edn">2.7e-04±8.8e-06</a>
              </td>
              <td>
                <a href="https://github.com/blosavio/brokvolli/blob/main/resources/deep_performance/version single-transduce-kv/test-3.edn">1.8e-03±1.6e-04</a>
              </td>
              <td>
                <a href="https://github.com/blosavio/brokvolli/blob/main/resources/deep_performance/version single-transduce-kv/test-4.edn">2.1e-02±4.0e-03</a>
              </td>
              <td>
                <a href="https://github.com/blosavio/brokvolli/blob/main/resources/deep_performance/version single-transduce-kv/test-5.edn">2.5e-01±4.8e-02</a>
              </td>
            </tr>
          </table>
        </div>
      </div>
      <hr>
    </section>
    <p id="page-footer">
      Copyright © 2024–2026 Brad Losavio.<br>
      Compiled by <a href="https://github.com/blosavio/Fastester">Fastester</a> on 2026 February 24.<span id="uuid"><br>
      1ff123be-d29c-42fc-8dbe-2bd794272c59</span>
    </p>
  </body>
</html>
