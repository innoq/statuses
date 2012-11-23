(ns statuses.server
  (:require [statuses.views.main :as main]
            [statuses.backend.persistence :as persistence]
            [compojure.handler :as handler])
  (:use compojure.core)
  (:gen-class))

(defn logger [handler]
  (fn [request]
    (println request)
    (handler request)))

;(server/add-middleware logger)
;(server/load-views-ns 'statuses.views)


;; (defn -main [& m]
;;   (persistence/init-db! "data/db.json" 1)
;;   (let [mode (keyword (or (first m) :dev))
;;         port (Integer. (get (System/getenv) "PORT" "8080"))]
;;     (println "Starting server in" mode "mode")
;;     (server/start port {:mode mode
;;                         :ns 'statuses})))

(persistence/init-db! "data/db.json" 1)

(def app
  (handler/site main/app-routes))