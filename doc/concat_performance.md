
  <body>
    <h1>
      Vector concatenation benchmarks
    </h1>
    <div>
      <a href="#group-0">Concatenation tactics</a>
    </div>
    <div>
      <p>
        <a href="https://clojuredocs.org/clojure.core/concat"><code>clojure.core/concat</code></a> is lazy, so it&apos;s perhaps not the best suited for
        <code>transduce</code> tasks, which are eager. There are a handful of tactics for concatenating two vectors, so let&apos;s run some benchmarks to see
        if any particular tactic performs objectively faster than the others. Benchmarks are defined <a href=
        "https://github.com/blosavio/brokvolli/blob/main/test/brokvolli/performance/concatenating.vlj">here</a>.
      </p>
      <p>
        Overall, we observe that the <em>into</em> and <em>transducer</em> tactics (closely related to each other), perform the best. Since they perform the
        best, and their implementation is straightforward, those are used in the <a href=
        "https://example.com/https://github.com/blosavio/brokvolli">concatv</a> core utility.
      </p>
    </div>
    <section>
      <h3 id="group-0">
        Concatenation tactics
      </h3>
      <div>
        <p>
          We&apos;ll consider five concatenation tactics.
        </p>
        <table>
          <tr>
            <td>
              realized concat
            </td>
            <td>
              <code>(doall (concat v1 v2))</code>
            </td>
          </tr>
          <tr>
            <td>
              vectored concat
            </td>
            <td>
              <code>(vec (concat v1 v2))</code>
            </td>
          </tr>
          <tr>
            <td>
              basic into
            </td>
            <td>
              <code>(into v1 v2)</code>
            </td>
          </tr>
          <tr>
            <td>
              transient cat
            </td>
            <td>
              <a href="https://example.com/https://github.com/blosavio/brokvolli">implementation here</a>
            </td>
          </tr>
          <tr>
            <td>
              transducer cat
            </td>
            <td>
              <code>(into v1 conj v2)</code>
            </td>
          </tr>
        </table>
        <p>
          For each of those five tactics, we&apos;ll feed a pair of vectors, <code>v1</code> and <code>v2</code>, with lengths, <code>n</code>, ranging from
          one to a million, increasing by decade. We&apos;ll use <a href="https://github.com/hugoduncan/criterium">Criterium</a> to measure the execution
          times. Each data point is the mean±std of sixty measurements.
        </p>
        <p></p>
        <h3>
          Observations
        </h3>
        <p>
          Execution times increase with vector length. The looping transient tactic is substantially slower (i.e., higher times in seconds). The two
          <code>into</code> tactics performed the best for all lengths of vectors, with the transducer tactic slightly better.
        </p>
      </div>
      <div>
        <h4 id="group-0-fexpr-0">
          (fn [n] ((tactics (project-version-lein)) (get-in vecs [n :left]) (get-in vecs [n :right])))
        </h4><img alt=
        "Benchmark measurements for expression `(fn [n] ((tactics (project-version-lein)) (get-in vecs [n :left]) (get-in vecs [n :right])))`, time versus &apos;n&apos; arguments, comparing different versions."
        src="concat_img/group-0-fexpr-0.svg"><button class="collapser" type="button">Show details</button>
        <div class="collapsable">
          <table>
            <caption>
              times in seconds, <em>mean±std</em>
            </caption>
            <thead>
              <tr>
                <td></td>
                <th colspan="7">
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
                <th>
                  1000000
                </th>
              </tr>
            </thead>
            <tr>
              <td>
                basic-into
              </td>
              <td>
                <a href="https://github.com/blosavio/brokvolli/blob/main/resources/concat_performance/version basic-into/test-0.edn">1.2e-04±1.7e-06</a>
              </td>
              <td>
                <a href="https://github.com/blosavio/brokvolli/blob/main/resources/concat_performance/version basic-into/test-1.edn">1.2e-04±1.6e-06</a>
              </td>
              <td>
                <a href="https://github.com/blosavio/brokvolli/blob/main/resources/concat_performance/version basic-into/test-2.edn">1.2e-04±1.7e-06</a>
              </td>
              <td>
                <a href="https://github.com/blosavio/brokvolli/blob/main/resources/concat_performance/version basic-into/test-3.edn">1.4e-04±1.6e-06</a>
              </td>
              <td>
                <a href="https://github.com/blosavio/brokvolli/blob/main/resources/concat_performance/version basic-into/test-4.edn">3.2e-04±3.3e-06</a>
              </td>
              <td>
                <a href="https://github.com/blosavio/brokvolli/blob/main/resources/concat_performance/version basic-into/test-5.edn">2.1e-03±1.9e-05</a>
              </td>
              <td>
                <a href="https://github.com/blosavio/brokvolli/blob/main/resources/concat_performance/version basic-into/test-6.edn">2.2e-02±6.7e-04</a>
              </td>
            </tr>
            <tr>
              <td>
                realized-concat
              </td>
              <td>
                <a href="https://github.com/blosavio/brokvolli/blob/main/resources/concat_performance/version realized-concat/test-0.edn">1.2e-04±2.1e-06</a>
              </td>
              <td>
                <a href="https://github.com/blosavio/brokvolli/blob/main/resources/concat_performance/version realized-concat/test-1.edn">1.3e-04±2.8e-06</a>
              </td>
              <td>
                <a href="https://github.com/blosavio/brokvolli/blob/main/resources/concat_performance/version realized-concat/test-2.edn">1.3e-04±3.2e-06</a>
              </td>
              <td>
                <a href="https://github.com/blosavio/brokvolli/blob/main/resources/concat_performance/version realized-concat/test-3.edn">1.6e-04±4.5e-06</a>
              </td>
              <td>
                <a href="https://github.com/blosavio/brokvolli/blob/main/resources/concat_performance/version realized-concat/test-4.edn">5.1e-04±1.6e-05</a>
              </td>
              <td>
                <a href="https://github.com/blosavio/brokvolli/blob/main/resources/concat_performance/version realized-concat/test-5.edn">4.4e-03±1.7e-04</a>
              </td>
              <td>
                <a href="https://github.com/blosavio/brokvolli/blob/main/resources/concat_performance/version realized-concat/test-6.edn">4.2e-02±1.9e-03</a>
              </td>
            </tr>
            <tr>
              <td>
                transducer-cat
              </td>
              <td>
                <a href="https://github.com/blosavio/brokvolli/blob/main/resources/concat_performance/version transducer-cat/test-0.edn">1.2e-04±1.9e-06</a>
              </td>
              <td>
                <a href="https://github.com/blosavio/brokvolli/blob/main/resources/concat_performance/version transducer-cat/test-1.edn">1.2e-04±1.6e-06</a>
              </td>
              <td>
                <a href="https://github.com/blosavio/brokvolli/blob/main/resources/concat_performance/version transducer-cat/test-2.edn">1.2e-04±1.7e-06</a>
              </td>
              <td>
                <a href="https://github.com/blosavio/brokvolli/blob/main/resources/concat_performance/version transducer-cat/test-3.edn">1.4e-04±1.5e-06</a>
              </td>
              <td>
                <a href="https://github.com/blosavio/brokvolli/blob/main/resources/concat_performance/version transducer-cat/test-4.edn">3.0e-04±3.9e-06</a>
              </td>
              <td>
                <a href="https://github.com/blosavio/brokvolli/blob/main/resources/concat_performance/version transducer-cat/test-5.edn">1.9e-03±2.4e-05</a>
              </td>
              <td>
                <a href="https://github.com/blosavio/brokvolli/blob/main/resources/concat_performance/version transducer-cat/test-6.edn">1.9e-02±3.4e-04</a>
              </td>
            </tr>
            <tr>
              <td>
                transient-cat
              </td>
              <td>
                <a href="https://github.com/blosavio/brokvolli/blob/main/resources/concat_performance/version transient-cat/test-0.edn">1.3e-04±2.1e-06</a>
              </td>
              <td>
                <a href="https://github.com/blosavio/brokvolli/blob/main/resources/concat_performance/version transient-cat/test-1.edn">1.6e-04±2.8e-06</a>
              </td>
              <td>
                <a href="https://github.com/blosavio/brokvolli/blob/main/resources/concat_performance/version transient-cat/test-2.edn">4.3e-04±9.7e-06</a>
              </td>
              <td>
                <a href="https://github.com/blosavio/brokvolli/blob/main/resources/concat_performance/version transient-cat/test-3.edn">3.2e-03±1.0e-04</a>
              </td>
              <td>
                <a href="https://github.com/blosavio/brokvolli/blob/main/resources/concat_performance/version transient-cat/test-4.edn">3.1e-02±1.2e-03</a>
              </td>
              <td>
                <a href="https://github.com/blosavio/brokvolli/blob/main/resources/concat_performance/version transient-cat/test-5.edn">3.1e-01±3.2e-02</a>
              </td>
              <td>
                <a href="https://github.com/blosavio/brokvolli/blob/main/resources/concat_performance/version transient-cat/test-6.edn">3.0e+00±4.1e-02</a>
              </td>
            </tr>
            <tr>
              <td>
                vectored-concat
              </td>
              <td>
                <a href="https://github.com/blosavio/brokvolli/blob/main/resources/concat_performance/version vectored-concat/test-0.edn">1.2e-04±3.9e-06</a>
              </td>
              <td>
                <a href="https://github.com/blosavio/brokvolli/blob/main/resources/concat_performance/version vectored-concat/test-1.edn">1.3e-04±2.0e-06</a>
              </td>
              <td>
                <a href="https://github.com/blosavio/brokvolli/blob/main/resources/concat_performance/version vectored-concat/test-2.edn">1.4e-04±1.7e-06</a>
              </td>
              <td>
                <a href="https://github.com/blosavio/brokvolli/blob/main/resources/concat_performance/version vectored-concat/test-3.edn">2.1e-04±2.6e-06</a>
              </td>
              <td>
                <a href="https://github.com/blosavio/brokvolli/blob/main/resources/concat_performance/version vectored-concat/test-4.edn">8.9e-04±2.4e-05</a>
              </td>
              <td>
                <a href="https://github.com/blosavio/brokvolli/blob/main/resources/concat_performance/version vectored-concat/test-5.edn">7.9e-03±1.3e-04</a>
              </td>
              <td>
                <a href="https://github.com/blosavio/brokvolli/blob/main/resources/concat_performance/version vectored-concat/test-6.edn">7.9e-02±2.8e-03</a>
              </td>
            </tr>
          </table>
        </div>
      </div>
      <hr>
    </section>
    <p id="page-footer">
      Copyright © 2024–2026 Brad Losavio.<br>
      Compiled by <a href="https://github.com/blosavio/Fastester">Fastester</a> on 2026 January 28.<span id="uuid"><br>
      1b5101d1-6b60-4868-830b-09d396f19bf3</span>
    </p>
  </body>
</html>
