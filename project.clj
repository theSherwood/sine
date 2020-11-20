(defproject sine "0.0.3"
  :description "A port of Sinuous to ClojureScript"
  :url "https://github.com/theSherwood/sine"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[org.clojure/clojure "1.10.0"]
                 [org.clojure/clojurescript "1.9.521"]]
  :repl-options {:init-ns sine.core}

  :plugins [[lein-cljsbuild "1.1.8"]]
  :cljsbuild {:builds
              [{:id           "example"
                :source-paths ["src"]
                :compiler     {:output-to "resources/public/js/compiled/lib.js"
                               :language-in :ecmascript6
                               :language-out :ecmascript5
                               :foreign-libs [{:file "resources/observable.js"
                                               :provides ["observable"]
                                               :module-type :es6}
                                              {:file "resources/utils.js"
                                               :provides ["utils"]
                                               :module-type :es6}]
                               :optimizations :simple
                               :pretty-print true}}]})
