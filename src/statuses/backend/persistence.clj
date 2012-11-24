(ns statuses.backend.persistence
  (:require [statuses.backend.core :as core]
            [statuses.backend.json :as json]
            [clj-time.format :as format]
            [clj-time.core :as time]
            [clj-time.local :as local])
  (:use [clojure.java.io :only [reader writer]])
  (:import java.util.concurrent.TimeUnit
           java.util.concurrent.Executors))


(defn get-save-time
  "Return the timestamp of the last time the DB was saved"
  [db]
  (:time db))

(defonce db (atom nil))
(defonce timer (. Executors newScheduledThreadPool 1))


(defn init-db!
  "Initializes database from path, saving it every interval minutes"
  [path interval]
  (let [persist-db (fn []
                     (swap! db assoc :time (time/now))
                     (swap! db json/write-db path))]
    (try
      (reset! db (json/read-db path))
      (catch java.io.IOException ioe
        (println "*Warning* Database " path " not found, using test data")
        (reset! db (core/add-testdata (core/empty-db) 50))))
    (.. Runtime getRuntime (addShutdownHook (Thread. persist-db)))
    (. timer (scheduleAtFixedRate persist-db
                                  (long interval)
                                  (long interval)
                                  (. TimeUnit MINUTES)))))
