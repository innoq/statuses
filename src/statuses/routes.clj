(ns statuses.routes
  (:require [statuses.configuration :refer [config]]))

(def base-template "/statuses")
(defn base-path [] base-template)

(def updates-template (str base-template "/updates"))
(defn updates-path
  ([] (updates-path nil))
  ([response-format]
   (str updates-template
        (if response-format (str "?format=" (name response-format)) ""))))

(def update-template (str updates-template "/:id"))
(defn update-path [id] (str (updates-path) "/" id))

(def update-replyform-template (str update-template "/replyform"))
(defn update-replyform-path [id] (str (update-path id) "/replyform"))

(def conversation-template (str base-template "/conversations/:id"))
(defn conversation-path [id] (str (base-path) "/conversations/" id))

(def info-template (str base-template "/info"))
(defn info-path [] (str (base-path) "/info"))

(def too-long-template (str base-template "/too-long/:length"))
(defn too-long-path [length] (str (base-path) "/too-long/" length))

(defn mention-path
  ([username] (mention-path username nil))
  ([username response-format]
   (str (updates-path) "?query=@" username
        (if response-format (str "&format=" (name response-format)) ""))))

(defn issue-path [] "https://github.com/innoq/statuses/issues"); TODO: read from configuration

(defn avatar-path [username]
  (clojure.string/replace (config :avatar-url) "{username}" username))

