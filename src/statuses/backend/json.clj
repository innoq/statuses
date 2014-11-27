(ns statuses.backend.json
  (:refer-clojure :exclude [read])
  (:require [clj-time.format :refer [formatters parse-local]]
            [clj-time.local :refer [format-local-time]]
            [clojure.data.json :refer [read write write-str]])
  (:use [clojure.java.io :only [reader writer]]))

(defn time-to-json [k v]
  (if (= :time k)
    (format-local-time v :date-time)
    v))

(defn write-db [db path]
  (with-open [file (writer path)]
    (write db file :value-fn time-to-json)
    db))

(defn- keywordify
  "turn string s into keywords, skipping numbers"
  [s]
  (let [parsed (read-string s)]
    (if (number? parsed)
      parsed
      (keyword parsed))))

(defn json-to-time [k v]
  (if (= :time k)
    (letfn [(parse [fmt s] (.toDateTime (parse-local (formatters fmt) s)))]
      (try (parse :date-time v)
        (catch Exception _
          (parse :rfc822 v))))
    v))

(defn read-db [path]
  (with-open [file (reader path)]
    (read file
          :key-fn keywordify
          :value-fn json-to-time)))

(defn as-json
  "Return appropriate JSON rendering for content (db or parts thereof)"
  [content]
  (write-str content :value-fn time-to-json))

