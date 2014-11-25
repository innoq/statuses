(ns statuses.backend.persistence
  (:require [clj-time.core :refer [now]]
            [statuses.backend.core :refer [add-testdata empty-db]]
            [statuses.backend.json :refer [read-db write-db]]))

(defn get-save-time
  "Return the timestamp of the last time the DB was saved"
  [db]
  (:time db))

(defonce db (atom nil))

(defn init!
  "Initializes database from path, saving it every interval minutes"
  [path interval]
  (letfn [(persist-db []
            (swap! db assoc :time (now))
            (swap! db write-db path))]
    (try
      (reset! db (read-db path))
      (catch java.io.IOException ioe
        (println "*Warning* Database " path " not found, using test data")
        (reset! db (add-testdata (empty-db) 50))))
    (.. Runtime getRuntime (addShutdownHook (Thread. persist-db)))
    (.scheduleAtFixedRate
      (java.util.concurrent.Executors/newScheduledThreadPool 1)
      persist-db
      (long interval)
      (long interval)
      java.util.concurrent.TimeUnit/MINUTES)))

