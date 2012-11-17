(ns statuses.server
  (:require [noir.server :as server])
  (:gen-class))

(defn logger [handler]
  (fn [request]
    (println "Received request: " request)
    (handler request)))

(server/add-middleware logger)

(server/load-views-ns 'statuses.views)

(defn -main [& m]
  (let [mode (keyword (or (first m) :dev))
        port (Integer. (get (System/getenv) "PORT" "8080"))]
    (server/start port {:mode mode
                        :ns 'statuses})))

