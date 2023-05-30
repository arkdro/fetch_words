(defproject fetch_words "0.0.1"
  :description "Fetch audio for the given list of words."
  :url "http://example.com/FIXME"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[org.clojure/clojure "1.10.1"]
                 [org.clojure/tools.cli "0.3.5"]
                 [org.clojure/tools.logging "1.2.4"]
                 [org.clojure/tools.trace "0.7.11"]
                 [org.clojure/data.csv "1.0.1"]
                 [org.slf4j/slf4j-api "2.0.7"]
                 [org.slf4j/slf4j-simple "2.0.7"]
                 [clj-http "3.12.3"]]
  :main ^:skip-aot fetch-words.core
  :repl-options {:init-ns fetch-words.core}
  :jvm-opts ["-Dclojure.tools.logging.factory=clojure.tools.logging.impl/slf4j-factory"]
  :profiles {:uberjar {:aot :all}
             :dev {:resource-paths ["test/resources"]
                   :dependencies []}})
