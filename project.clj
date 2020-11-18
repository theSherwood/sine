(defproject sine "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[org.clojure/clojure "1.10.0"]
                 [org.clojure/clojurescript "1.9.521"]]
  :repl-options {:init-ns sine.core}
  :foreign-libs [{:file "src/sine/h.js"
                  :provides ["biff"]
                  :module-type :es6}]

  :plugins [[lein-cljsbuild "1.1.8"]]
  :cljsbuild {:builds
              [{:id           "example"
                :source-paths ["src"]
                :compiler     {:output-to "resources/public/js/compiled/lib.js"
                               :language-in :ecmascript6
                               :language-out :ecmascript5
                               :foreign-libs [{:file "src/sine/hyper.js"
                                               :provides ["hyper"]
                                               :module-type :es6}
                                              {:file "src/sine/observable.js"
                                               :provides ["observable"]
                                               :module-type :es6}
                                              {:file "src/sine/api.js"
                                               :provides ["api"]
                                               :module-type :es6}
                                              {:file "src/sine/utils.js"
                                               :provides ["utils"]
                                               :module-type :es6}]
                               :optimizations :simple
                               :pretty-print true}}]})
