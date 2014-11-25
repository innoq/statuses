(ns statuses.routes
  (:require [statuses.configuration :refer [config]]))

(def base "/statuses/updates")

(defn info-path [] "/statuses/info")

(defn mention-path
  ([username] (mention-path username nil))
  ([username response-format] (str base "?query=@" username
    (if response-format (str "&format=" (name response-format)) ""))))

(defn issue-path [] "https://github.com/innoq/statuses/issues"); TODO: read from configuration

(defn avatar-path [username]
  (clojure.string/replace (config :avatar-url) "{username}" username))

