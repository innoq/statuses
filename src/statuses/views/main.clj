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

(defn glyphicon [icon]
  [:i {:class (str "glyphicon glyphicon-" icon)}])

(defn nav-links [request]
  (list [:li [:a {:href base} [:span {:class "glyphicon glyphicon-th-list"}] "Everything"]]
        [:li [:a {:href (str base "?query=@" (user request))} [:span {:class "glyphicon glyphicon-user"}] "Mentions"]]
        [:li [:a {:href (str base "?format=atom")} [:span {:class "glyphicon glyphicon-fire"}] "Feed (all)"]]
        [:li [:a {:href (str base "?format=atom&query=@" (user request))} [:span {:class "glyphicon glyphicon-fire"}] "Feed (mentions)"]]
        [:li [:a {:href "/statuses/info"} [:span {:class "glyphicon glyphicon-info-sign"}] "Info"]]
        [:li [:a {:href "https://github.com/innoq/statuses/issues"} [:span {:class "glyphicon glyphicon-question-sign"}] "Issue"]]))

(defn format-time [time]
  [:time {:datetime (time/time-to-utc time)} (time/time-to-human time)])

(defn delete-form [id]
  (form-to {:class "delete-form" :onsubmit "return confirm('Delete status?')"} [:delete (str base "/" id)]
    (html [:button {:type "submit" :class "btn btn-sm"} (html [:span.glyphicon.glyphicon-trash ][:span.btn-label "Delete"])])
    ))

(defn update [request {:keys [id text author time in-reply-to conversation can-delete?]}]
  (list
    [:div.avatar
     (link-to (str (config :profile-url-prefix) author) [:img {:src (avatar-uri author) :alt author}])]
    [:div.post-content (common/linkify text)]
    [:div.meta [:span.author (link-to (str base "?author=" author) author)]
     [:span.time [:a.permalink {:href (str base "/" id)} (format-time time)]]
     (if in-reply-to
       (list
         [:span.reply (link-to (str base "/" in-reply-to) in-reply-to)]))
     ;; conversation link should always be shown if the post is part of a conversation
     (if conversation
       (list
         [:span.conversation (link-to (str "/statuses/conversations/" conversation)
                               conversation)]))
     [:button {:type "submit" :class "btn btn-sm btn-reply"} (html [:span.glyphicon.glyphicon-edit ][:span.btn-label "Reply"])]
     (if can-delete?
       (list
         [:span.delete (delete-form id)]))
     ]))

(defn entry-form []
  (form-to {:class "entry-form"} [:post base]
         [:div.input.input-group
           (text-field {:class "form-control" :autofocus "autofocus"} "entry-text")
           [:span.input-group-btn
             [:button {:type "submit" :class "btn btn-default"} "Send" ]]]
         [:div {"style" "clear: both"}]))

(defn reply-form [id author]
  (form-to {:class "reply-form" } [:post base]
    [:div.input-group
      (text-field {:class "form-control" :autofocus "autofocus" :value (str "@" author " ")} "reply-text")
      [:span.input-group-btn
        [:button {:type "submit" :class "btn btn-default"} "Reply" ]]]
    (hidden-field "reply-to" id)
    [:div {"style" "clear: both"}]))

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



