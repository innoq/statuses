(ns statuses.server
  (:require [statuses.views.main :as main]
            [statuses.backend.persistence :as persistence])
  (:use [compojure.core]
        [ring.middleware file file-info stacktrace reload params]
        [ring.adapter.jetty :only [run-jetty]])
  (:gen-class))

(defn app
  []
  (-> main/app-routes
      (wrap-params)
      (wrap-file "public")
      (wrap-file-info)
      (wrap-stacktrace)))

(persistence/init-db! "data/db.json" 1)

(defn -main [& m]
  (run-jetty (app) {:port 8080 :join? false}))
