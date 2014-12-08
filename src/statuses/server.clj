(ns statuses.server
  (:require [ring.adapter.jetty :refer [run-jetty]]
            [ring.middleware.content-type :refer [wrap-content-type]]
            [ring.middleware.not-modified :refer [wrap-not-modified]]
            [ring.middleware.params :refer [wrap-params]]
            [ring.middleware.reload :refer [wrap-reload]]
            [ring.middleware.resource :refer [wrap-resource]]
            [ring.middleware.stacktrace :refer [wrap-stacktrace]]
            [statuses.backend.persistence :as persistence]
            [statuses.configuration :as cfg :refer [config]]
            [statuses.routing :as main])
  (:gen-class))

(def app
  (-> main/app-routes
      wrap-params
      (wrap-resource "public")
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
    (if (= (config :run-mode) :dev)
      (wrap-reload app)
      app)
    {:host (config :host)
     :port (config :http-port)
     :join? false}))

