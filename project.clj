(defproject statuses "1.0.0-SNAPSHOT"
  :description "Statuses app for innoQ"
  :url "https://github.com/innoq/statuses"
  :license {:name "Apache License, Version 2.0"
            :url "http://www.apache.org/licenses/LICENSE-2.0"
            :distribution :repo
            :comments "A business-friendly OSS license"}
  :dependencies [[org.clojure/clojure "1.7.0"]
                 [ring "1.4.0"]
                 [compojure "1.3.3"]
                 [clj-time "0.9.0"]
                 [org.clojure/data.json "0.2.6"]
                 [org.clojure/tools.cli "0.3.3"]]
  :pedantic? :abort
  :plugins [[jonase/eastwood "0.2.0"]]
  :profiles {:dev {:dependencies [[ring-mock "0.1.5"]]}
             :uberjar {:aot [statuses.server]}}
  :main statuses.server
  :aliases {"lint" "eastwood"}
  :eastwood {:exclude-linters [:constant-test]})

