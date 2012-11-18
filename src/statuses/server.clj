(ns statuses.server
  (:require [statuses.backend.persistence :as persistence]
            [noir.server :as server]
            [noir.options :as options]
            [clojure.string :as string]
            [hiccup.util :as hiccup])
  (:gen-class))

(defn logger [handler]
  (fn [request]
    (println (get-in request [:headers "remote_user"])
             (:request-method request)
             (:uri request))
    (handler request)))

(server/add-middleware logger)
(server/load-views-ns 'statuses.views)


(defn -main [& m]
  (persistence/init-db! "data/db.json" 1)
  (let [mode (keyword (or (first m) :dev))
        port (Integer. (get (System/getenv) "PORT" "8080"))]
    (server/start port {:mode mode
                        :ns 'statuses})))

