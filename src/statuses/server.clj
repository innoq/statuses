(ns statuses.server
  (:require [statuses.views.main :as main]
            [statuses.backend.persistence :as persistence])
  (:use [compojure.core]
        [ring.middleware file file-info stacktrace reload params]
        [ring.adapter.jetty :only [run-jetty]])
  (:gen-class))

(def app
  (-> main/app-routes
      (wrap-params)
      (wrap-file "public")
      (wrap-file-info)
      (wrap-stacktrace)))

(defn -main [& m]
  (let [mode (keyword (or (first m) :dev))
        port (Integer. (get (System/getenv) "PORT" "8080"))
        hdl  (wrap-reload app)]
    (persistence/init-db! "data/db.json" 1)
    (println "Starting server on port" port "in mode" mode)
    (run-jetty hdl {:port port :join? false})))
