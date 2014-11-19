(defproject statuses "0.1.0-SNAPSHOT"
  :description "Statuses app for innoQ"
  :namespaces [statuses]
  :dependencies [
                 [org.clojure/clojure "1.6.0"]
                 [compojure "1.2.1"]
                 [clj-time "0.8.0"]
                 [ring "1.3.1"]
                 [org.clojure/data.json "0.2.5"]]
  :plugins [[jonase/eastwood "0.1.5"]
            [lein-ring "0.8.13"]]
  :ring {:handler statuses.views.main/app-routes}
  :main statuses.server
  :aliases {"lint"  ["eastwood"]}
  :profiles {:dev {:dependencies [[ring-mock "0.1.5"]]}})

