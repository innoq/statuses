(ns statuses.server
  (:require [statuses.routing             :as main]
            [statuses.backend.persistence :as persistence]
            [statuses.configuration       :as cfg :refer [config]]
            [ring.middleware [params       :refer [wrap-params]]
                             [file         :refer [wrap-file]]
                             [content-type :refer [wrap-content-type]]
                             [not-modified :refer [wrap-not-modified]]
                             [stacktrace   :refer [wrap-stacktrace]]
                             [reload       :refer [wrap-reload]]]
            [ring.adapter.jetty :refer [run-jetty]])
  (:gen-class))

(def app
  (-> main/app-routes
      wrap-params
      (wrap-file "public")
      wrap-content-type
      wrap-not-modified
      wrap-stacktrace))

(defn -main [& m]
  (cfg/init! (or (first m) "config.clj"))
  (println "Configuration: " (config))
  (persistence/init! (config :database-path) (config :save-interval))
  (println "Starting server on host"  (config :host)
           "port" (config :http-port)
           "in mode" (config :run-mode))
  (run-jetty
   (if (= (config :run-mode) :dev) (wrap-reload app) app)
   {:port (config :http-port) :join? false :host (config :host)}))
