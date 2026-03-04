
  <body>
    <h1>
      Addendum 3: Benchmarking with different processor counts
    </h1>
    <div>
      <a href="#group-0">Ninety mathematical operations per element</a>
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
          <a href="https://blosavio.github.io/brokvolli/deep.html">Addendum 1: Increased computations per element</a>
        </li>
        <li>
          <a href="https://blosavio.github.io/brokvolli/partitions.html">Addendum 3: Performance scaling with processors</a>
        </li>
      </ul>
      <p></p>
      <p>
        Benchmarks defined <a href="https://github.com/blosavio/brokvolli/blob/main/test/brokvolli/performance/processors.clj">here</a>, similar to <a href=
        "">before</a>. For exploring the affects of partiton size, we&apos;ll consider only multi-threaded <code>transduce</code>. We&apos;ll test vectors of
        random floating point numbers, increasing in length from one to one-hundred-thousand, by powers of ten. For each number in the vectors, we&apos;ll
        construct a hashmap of eighteen mathematical operations on that number (trig ops, logarithms, etc.) that the JVM compiler oughtn&apos;t be able to
        optimize. To amplify the computation requirements, we&apos;ll make that hashmap five times for each element. We&apos;ll use the <a href=
        "https://github.com/hugoduncan/criterium/">Criterium benchmarking library</a> to measure the execution times of sixty repetitions of each condition.
        Benchmarks were run on three pinned cores of my feeble desktop computer.
      </p>
    </div>
    <section>
      <h3 id="group-0">
        Ninety mathematical operations per element
      </h3>
      <div>
        <h4 id="group-0-fexpr-0">
          (fn [n] (multi/transduce partition-at concatv xform-1 tconj (vecs n)))
        </h4><img alt=
        "Benchmark measurements for expression `(fn [n] (multi/transduce partition-at concatv xform-1 tconj (vecs n)))`, time versus &apos;n&apos; arguments, comparing different versions."
        src="processors_img/group-0-fexpr-0.svg"><button class="collapser" type="button">Show details</button>
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
                0-0
              </td>
              <td>
                <a href="https://github.com/blosavio/brokvolli/blob/main/resources/processors_performance/version 0-0/test-0.edn">1.4e-05±4.6e-07</a>
              </td>
              <td>
                <a href="https://github.com/blosavio/brokvolli/blob/main/resources/processors_performance/version 0-0/test-1.edn">1.4e-04±1.6e-06</a>
              </td>
              <td>
                <a href="https://github.com/blosavio/brokvolli/blob/main/resources/processors_performance/version 0-0/test-2.edn">1.4e-03±3.5e-05</a>
              </td>
              <td>
                <a href="https://github.com/blosavio/brokvolli/blob/main/resources/processors_performance/version 0-0/test-3.edn">1.5e-02±2.3e-03</a>
              </td>
              <td>
                <a href="https://github.com/blosavio/brokvolli/blob/main/resources/processors_performance/version 0-0/test-4.edn">2.4e-01±1.2e-01</a>
              </td>
              <td>
                <a href="https://github.com/blosavio/brokvolli/blob/main/resources/processors_performance/version 0-0/test-5.edn">2.6e+00±4.7e-01</a>
              </td>
            </tr>
            <tr>
              <td>
                0-1
              </td>
              <td>
                <a href="https://github.com/blosavio/brokvolli/blob/main/resources/processors_performance/version 0-1/test-0.edn">1.6e-05±2.4e-06</a>
              </td>
              <td>
                <a href="https://github.com/blosavio/brokvolli/blob/main/resources/processors_performance/version 0-1/test-1.edn">1.6e-04±2.2e-05</a>
              </td>
              <td>
                <a href="https://github.com/blosavio/brokvolli/blob/main/resources/processors_performance/version 0-1/test-2.edn">1.6e-03±2.2e-04</a>
              </td>
              <td>
                <a href="https://github.com/blosavio/brokvolli/blob/main/resources/processors_performance/version 0-1/test-3.edn">8.4e-03±7.5e-04</a>
              </td>
              <td>
                <a href="https://github.com/blosavio/brokvolli/blob/main/resources/processors_performance/version 0-1/test-4.edn">1.8e-01±2.6e-02</a>
              </td>
              <td>
                <a href="https://github.com/blosavio/brokvolli/blob/main/resources/processors_performance/version 0-1/test-5.edn">1.6e+00±2.2e-01</a>
              </td>
            </tr>
            <tr>
              <td>
                0-3
              </td>
              <td>
                <a href="https://github.com/blosavio/brokvolli/blob/main/resources/processors_performance/version 0-3/test-0.edn">1.7e-05±2.4e-06</a>
              </td>
              <td>
                <a href="https://github.com/blosavio/brokvolli/blob/main/resources/processors_performance/version 0-3/test-1.edn">1.7e-04±1.9e-05</a>
              </td>
              <td>
                <a href="https://github.com/blosavio/brokvolli/blob/main/resources/processors_performance/version 0-3/test-2.edn">1.6e-03±2.0e-04</a>
              </td>
              <td>
                <a href="https://github.com/blosavio/brokvolli/blob/main/resources/processors_performance/version 0-3/test-3.edn">8.1e-03±6.2e-04</a>
              </td>
              <td>
                <a href="https://github.com/blosavio/brokvolli/blob/main/resources/processors_performance/version 0-3/test-4.edn">5.6e-02±1.1e-02</a>
              </td>
              <td>
                <a href="https://github.com/blosavio/brokvolli/blob/main/resources/processors_performance/version 0-3/test-5.edn">7.1e-01±1.0e-01</a>
              </td>
            </tr>
            <tr>
              <td>
                0-7
              </td>
              <td>
                <a href="https://github.com/blosavio/brokvolli/blob/main/resources/processors_performance/version 0-7/test-0.edn">1.7e-05±2.4e-06</a>
              </td>
              <td>
                <a href="https://github.com/blosavio/brokvolli/blob/main/resources/processors_performance/version 0-7/test-1.edn">1.8e-04±2.5e-05</a>
              </td>
              <td>
                <a href="https://github.com/blosavio/brokvolli/blob/main/resources/processors_performance/version 0-7/test-2.edn">1.8e-03±2.8e-04</a>
              </td>
              <td>
                <a href="https://github.com/blosavio/brokvolli/blob/main/resources/processors_performance/version 0-7/test-3.edn">8.0e-03±4.2e-04</a>
              </td>
              <td>
                <a href="https://github.com/blosavio/brokvolli/blob/main/resources/processors_performance/version 0-7/test-4.edn">3.5e-02±1.1e-02</a>
              </td>
              <td>
                <a href="https://github.com/blosavio/brokvolli/blob/main/resources/processors_performance/version 0-7/test-5.edn">4.0e-01±9.6e-02</a>
              </td>
            </tr>
            <tr>
              <td>
                0-15
              </td>
              <td>
                <a href="https://github.com/blosavio/brokvolli/blob/main/resources/processors_performance/version 0-15/test-0.edn">1.7e-05±2.6e-06</a>
              </td>
              <td>
                <a href="https://github.com/blosavio/brokvolli/blob/main/resources/processors_performance/version 0-15/test-1.edn">1.5e-04±2.0e-05</a>
              </td>
              <td>
                <a href="https://github.com/blosavio/brokvolli/blob/main/resources/processors_performance/version 0-15/test-2.edn">1.7e-03±2.6e-04</a>
              </td>
              <td>
                <a href="https://github.com/blosavio/brokvolli/blob/main/resources/processors_performance/version 0-15/test-3.edn">7.7e-03±8.9e-04</a>
              </td>
              <td>
                <a href="https://github.com/blosavio/brokvolli/blob/main/resources/processors_performance/version 0-15/test-4.edn">1.8e-02±4.0e-03</a>
              </td>
              <td>
                <a href="https://github.com/blosavio/brokvolli/blob/main/resources/processors_performance/version 0-15/test-5.edn">2.3e-01±6.8e-02</a>
              </td>
            </tr>
            <tr>
              <td>
                0-31
              </td>
              <td>
                <a href="https://github.com/blosavio/brokvolli/blob/main/resources/processors_performance/version 0-31/test-0.edn">1.7e-05±2.3e-06</a>
              </td>
              <td>
                <a href="https://github.com/blosavio/brokvolli/blob/main/resources/processors_performance/version 0-31/test-1.edn">1.8e-04±2.7e-05</a>
              </td>
              <td>
                <a href="https://github.com/blosavio/brokvolli/blob/main/resources/processors_performance/version 0-31/test-2.edn">1.7e-03±2.5e-04</a>
              </td>
              <td>
                <a href="https://github.com/blosavio/brokvolli/blob/main/resources/processors_performance/version 0-31/test-3.edn">8.2e-03±9.7e-04</a>
              </td>
              <td>
                <a href="https://github.com/blosavio/brokvolli/blob/main/resources/processors_performance/version 0-31/test-4.edn">1.0e-02±4.4e-03</a>
              </td>
              <td>
                <a href="https://github.com/blosavio/brokvolli/blob/main/resources/processors_performance/version 0-31/test-5.edn">1.2e-01±3.5e-02</a>
              </td>
            </tr>
            <tr>
              <td>
                0-63
              </td>
              <td>
                <a href="https://github.com/blosavio/brokvolli/blob/main/resources/processors_performance/version 0-63/test-0.edn">1.6e-05±8.5e-07</a>
              </td>
              <td>
                <a href="https://github.com/blosavio/brokvolli/blob/main/resources/processors_performance/version 0-63/test-1.edn">1.8e-04±2.5e-05</a>
              </td>
              <td>
                <a href="https://github.com/blosavio/brokvolli/blob/main/resources/processors_performance/version 0-63/test-2.edn">1.7e-03±2.0e-04</a>
              </td>
              <td>
                <a href="https://github.com/blosavio/brokvolli/blob/main/resources/processors_performance/version 0-63/test-3.edn">8.2e-03±8.2e-04</a>
              </td>
              <td>
                <a href="https://github.com/blosavio/brokvolli/blob/main/resources/processors_performance/version 0-63/test-4.edn">1.4e-02±4.5e-03</a>
              </td>
              <td>
                <a href="https://github.com/blosavio/brokvolli/blob/main/resources/processors_performance/version 0-63/test-5.edn">1.1e-01±2.7e-02</a>
              </td>
            </tr>
          </table>
        </div>
      </div>
      <hr>
    </section>
    <p id="page-footer">
      Copyright © 2024–2026 Brad Losavio.<br>
      Compiled by <a href="https://github.com/blosavio/Fastester">Fastester</a> on 2026 March 04.<span id="uuid"><br>
      280ceca6-1559-4e0c-b032-a92d85dc411e</span>
    </p>
  </body>
</html>
