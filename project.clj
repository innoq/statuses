(defproject statuses "0.1.0-SNAPSHOT"
            :description "Statuses app for innoQ"
            :dependencies [[org.clojure/clojure "1.4.0"]
                           [noir "1.2.1" :exclusions [clj-stacktrace]]
                           [clj-stacktrace "0.2.4"]
                           [clj-time "0.4.4"]]
            :main statuses.server)


