(defproject statuses "0.1.0-SNAPSHOT"
            :description "Statuses app for innoQ"
            :namespaces [statuses]
            :dependencies [
                           [org.clojure/clojure "1.4.0"]
                           [compojure "1.1.3"]
                           [clj-time "0.4.4"]
                           [ring "1.1.6"]
                           [org.clojure/data.json "0.2.0"]
                           ]
            :plugins [[lein-ring "0.7.5"]]
            :ring {:handler statuses.views.main/app-routes}
            :main statuses.server
            :profiles
            {:dev {:dependencies [[ring-mock "0.1.3"]]}})


