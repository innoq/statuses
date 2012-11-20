(ns statuses.backend.persistence
  (:require [statuses.backend.core :as core]
            [clojure.data.json :as json]
            [clj-time.format :as format]
            [clj-time.core :as time]
            [clj-time.local :as local])
  (:use [clojure.java.io :only [reader writer]])
  (:import java.util.concurrent.TimeUnit
           java.util.concurrent.Executors))


(defn write-db
  "Writes out db to path"
  [db path]
  (with-open [file (writer path)]
    (json/write db file :value-fn core/time-to-json)
    (assoc db :time (time/now))))

(defn get-save-time [db] (:time db))

(defn- keywordify [n]
  (let [parsed (read-string n)]
    (if (number? parsed)
      parsed
      (keyword parsed))))

(defn read-db
  "Reads db from path"
  [path]
  (with-open [file (reader path)]
    (json/read file
               :value-fn core/json-to-time
               :key-fn keywordify)))

(defonce db (atom nil))
(defonce timer (. Executors newScheduledThreadPool 1))


(defn init-db!
  "Initializes database from path, saving it every interval minutes"
  [path interval]
  (let [persist-db (fn []
                     (println "Saving db to" path)
                     (swap! db write-db path))]
    (try
      (reset! db (read-db path))
      (catch java.io.IOException ioe
        (println "*Warning* Database " path " not found, using test data")
        (reset! db (core/add-testdata (core/empty-db) 50))))
    (.. Runtime getRuntime (addShutdownHook (Thread. persist-db)))
    (. timer (scheduleAtFixedRate persist-db
                                  (long interval)
                                  (long interval)
                                  (. TimeUnit MINUTES)))))
