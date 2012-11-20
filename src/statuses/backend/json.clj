(ns statuses.backend.json
  (:require [clojure.data.json :as json]
            [clj-time.format :as format]
            [statuses.backend.time :as time]
            [clj-time.local :as local])
  (:use [clojure.java.io :only [reader writer]]))


(defn time-to-json [key value]
  (if (= :time key)
    (time/time-to-utc value)
    value))

(defn as-json
  "Return appropriate JSON rendering for content (db or parts thereof)"
  [content]
  (json/write-str content :value-fn time-to-json))

(defn write-db
  "Writes out db to path"
  [db path]
  (with-open [file (writer path)]
    (json/write db file :value-fn time-to-json)
    db))

(defn- keywordify
  "turn string s into keywords, skipping numbers"
  [s]
  (let [parsed (read-string s)]
    (if (number? parsed)
      parsed
      (keyword parsed))))

(defn json-to-time [key value]
  (if (= :time key)
    (time/utc-to-time value)
    value))

(defn read-db
  "Reads db from path"
  [path]
  (with-open [file (reader path)]
    (json/read file
               :value-fn json-to-time
               :key-fn keywordify)))

