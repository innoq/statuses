(ns statuses.backend.time
  (:require [clj-time.format :as format]
            [clj-time.local :as local]))

(defn time-to-utc [time]
  (local/format-local-time time :rfc822))

(defn time-to-human [time]
  (str (local/format-local-time time :date) " "
       (local/format-local-time time :hour-minute-second)))

(defn utc-to-time [s]
  (.toDateTime (format/parse-local (format/formatters :rfc822) s)))

