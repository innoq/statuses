(ns statuses.server
  (:require [statuses.routing :as main]
            [statuses.backend.persistence :as persistence]
            [statuses.configuration :as cfg])
  (:use [compojure.core]
        [statuses.configuration :only [config]]
        [ring.middleware file file-info stacktrace reload params]
        [ring.adapter.jetty :only [run-jetty]])
  (:gen-class))

(def app
  (-> main/app-routes
      wrap-params
      (wrap-file "public")
      wrap-file-info
      wrap-stacktrace))

(defn -main [& m]
  (cfg/init! (or (first m) "config.clj"))
  (println "Configuration: " (config))
  (persistence/init! (config :database-path) (config :save-interval))
  (println "Starting server on port"  "in mode" (config :run-mode))
  (run-jetty
   (if (= (config :run-mode) :dev) (wrap-reload app) app)
   {:port (config :http-port) :join? false :host (config :host)}))
