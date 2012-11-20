(ns statuses.server
  (:require [statuses.backend.persistence :as persistence]
            [noir.server :as server])
  (:gen-class))

(defn logger [handler]
  (fn [request]
    (println request)
    (handler request)))

;(server/add-middleware logger)
(server/load-views-ns 'statuses.views)


(defn -main [& m]
  (persistence/init-db! "data/db.json" 1)
  (let [mode (keyword (or (first m) :dev))
        port (Integer. (get (System/getenv) "PORT" "8080"))]
    (server/start port {:mode mode
                        :ns 'statuses})))

