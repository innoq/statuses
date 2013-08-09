(ns statuses.views.main
  (:require [statuses.views.common :as common]
            [statuses.views.atom :as atom]
            [statuses.backend.time :as time])
  (:use [hiccup.core :only [html]]
        [hiccup.element :only [link-to]]
        [hiccup.form :only [form-to text-field hidden-field submit-button]]
        [statuses.configuration :only [config]]
        ))

(def base "/statuses/updates")

(defn user [request]
  (or (get-in request [:headers "remote_user"]) "guest"))

(defn avatar-uri [username]
  (clojure.string/replace (config :avatar-url) "{username}" username))


(defn nav-links [request]
  (let [links ["Statuses" [{:url base :title "Everything"
                            :icon "icon-th-list"},
                           {:url (str base "?query=@" (user request)) :title "Mentions"
                            :icon "icon-user"}],
                  "Feeds"    [{:url (str base "?format=atom") :title "Feed for everything"
                         :icon "icon-fire"},
                        {:url (str base "?format=atom&query=@" (user request)) :title "Feed for mentions"
                         :icon "icon-fire"}],
                  "Support"  [{:url "/statuses/info" :title "Server info"
                           :icon "icon-info-sign"},
                          {:url "https://github.com/innoq/statuses/issues" :title "Report issue"
                           :icon "icon-question-sign"}]]]

    (map (fn [[header elements]]
           [:li.nav-header header
            (map (fn [{:keys [url title icon]}] [:li (link-to url [:i {:class icon}] title)]) elements)])
      (partition 2 links))))

(defn format-time [time]
  [:time {:datetime (time/time-to-utc time)} (time/time-to-human time)])

(defn delete-form [id]
  (form-to [:delete (str base "/" id)]
    (html [:button {:type "submit" :class "btn btn-mini"} (html [:i.icon-trash.icon-label-holder "Delete"])])
    ))

(defn update [request {:keys [id text author time in-reply-to conversation]}]
  (list [:img.avatar {:src (avatar-uri author) :alt author}]
    [:div.content (common/linkify text)]
    [:div.meta [:span.author (link-to (str base "?author=" author) author)]
     [:span.time (link-to (str base "/" id) (format-time time))]
     (if in-reply-to
       (list
         [:span.reply (link-to (str base "/" in-reply-to) in-reply-to)]))
     ;; conversation link should always be shown if the post is part of a conversation
     (if conversation
       (list
         [:span.conversation (link-to (str "/statuses/conversations/" conversation)
                               conversation)])
       (if (= (user request) author)
         (list
           [:span.delete (delete-form id)])))
     ]))

(defn entry-form []
  (form-to {:class "form-inline" } [:post base]
           (text-field {:class "input-xxlarge" :autofocus "autofocus" } "text")
           (submit-button {:class "btn" } "Send update")))

(defn reply-form [id author]
  (form-to [:post base]
    (text-field {:class "input-xxlarge"
                 :autofocus "autofocus"
                        :value (str "@" author " ") } "text")
    (hidden-field "reply-to" id)
           (submit-button {:class "btn" } "Reply")))

(defn list-page [items next request]
  (common/layout
    (list [:div (entry-form)]
      [:div [:ul.updates (map (fn [item] [:li.post (update request item)]) items)]]
      (if next (link-to next "Next")))
    (nav-links request)))

(defn update-page [item request]
  (common/layout
    (list [:div.update (update request item)]
      (reply-form (:id item) (:author item)))
    (nav-links request)))



