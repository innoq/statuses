(defproject statuses "2.0.0-SNAPSHOT"
  :url "https://github.com/innoq/statuses"
  :description "An experimental, extremely simple-minded microblogging
                infrastructure for internal use."
  :license {:name "Apache License, Version 2.0"
            :url "http://www.apache.org/licenses/LICENSE-2.0"
            :distribution :repo
            :comments "A business-friendly OSS license"}
  :min-lein-version "2.8.1"
  :dependencies [[org.clojure/clojure "1.9.0"]]
  :main statuses.main
  :uberjar-name "statuses.jar"
  :profiles {:uberjar {:aot [statuses.main]}})
