(ns statuses.views.layout
  (:require [hiccup.element :refer [link-to]]
            [hiccup.form :refer [check-box]]
            [statuses.routes :refer [info-path issue-path mention-path]]
            [statuses.views.common :refer [icon]]))

(defn- preference [id title icon-name]
  [:li [:a {:name id}
    (icon icon-name)
    [:label {:for (str "pref-" id)} title]
    (check-box {:class "pref" :disabled "disabled"} (str "pref-" id))]])

(defn- nav-link [url title icon-name]
  [:li (link-to url (icon icon-name) title)])

(defn nav-links [username]
  (list (nav-link (mention-path username)       "Mentions"        "at")
        (nav-link (mention-path username :atom) "Feed (mentions)" "rss")
        (nav-link (info-path)                   "Info"            "info")
        (nav-link (issue-path)                  "Issues"          "github")
        (preference "inline-images"             "Inline images?"  "cogs")))

