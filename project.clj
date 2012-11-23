(defproject statuses "0.1.0-SNAPSHOT"
            :description "Statuses app for innoQ"
            :dependencies [
                           [org.clojure/clojure "1.4.0"]
                           [compojure "1.1.3"]
                           [clj-time "0.4.4"]
                           [org.clojure/data.json "0.2.0"]
                           ]
            :plugins [[lein-ring "0.7.5"]]
            :ring {:handler statuses.server/app}
            :profiles
            {:dev {:dependencies [[ring-mock "0.1.3"]]}})


