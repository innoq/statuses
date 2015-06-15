(ns statuses.server
  (:require [ring.adapter.jetty :refer [run-jetty]]
            [ring.middleware.content-type :refer [wrap-content-type]]
            [ring.middleware.not-modified :refer [wrap-not-modified]]
            [ring.middleware.params :refer [wrap-params]]
            [ring.middleware.reload :refer [wrap-reload]]
            [ring.middleware.resource :refer [wrap-resource]]
            [clojure.tools.cli :refer [parse-opts]]
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


(def cli-options
  [["-p" "--port PORT" "Port number" :parse-fn #(Integer/parseInt %)]
  ["-h" "--host HOST" "Hostname"]
  ["-c" "--conf ConfigurationFile" "The Location of the Configuration File"]])

(defn -main [& args]
  (let [{:keys [options arguments errors summary]} (parse-opts args cli-options)]
    (cfg/init! (or (:conf options) "config.clj"))
    (println "Configuration: " (config))
    (println "CLI Options: " (str options))
    (persistence/init! (config :database-path) (config :save-interval))
    
    (println "Starting server on host" (or (:host options) (config :host))
             "port" (or (:port options) (config :http-port))
             "in mode" (config :run-mode))
    (run-jetty
      (if (= (config :run-mode) :dev)
        (wrap-reload app)
        app)
      {:host (or (:host options) (config :host))
       :port (or (:port options) (config :http-port))
       :join? false}))
  )


