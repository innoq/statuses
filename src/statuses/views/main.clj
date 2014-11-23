(ns statuses.views.main
  (:require [statuses.views.common :as common]
            [statuses.views.atom :as atom]
            [statuses.backend.time :as time])
  (:use [hiccup.core :only [html]]
        [hiccup.element :only [link-to]]
        [hiccup.form :only
          [form-to text-field hidden-field submit-button check-box]]
        [statuses.configuration :only [config]]
        ))

(def base "/statuses/updates")

(defn user [request]
  (or (get-in request [:headers "remote_user"]) "guest"))

(defn avatar-uri [username]
  (clojure.string/replace (config :avatar-url) "{username}" username))

(defn- updates-uri
  ([request] (updates-uri request nil))
  ([request format] (str base (if format (str "?format=" (name format)) ""))))

(defn- mention-uri
  ([request] (mention-uri request nil))
  ([request format] (str base "?query=@" (user request)
    (if format (str "&format=" (name format)) ""))))

(defn- glyphicon [icon]
  [:span {:class (str "glyphicon glyphicon-" icon)}])

(defn- nav-link [url title icon]
  [:li (link-to url (glyphicon icon) title)])

(defn- preference [id title icon]
  [:li [:a {:name id}
    (glyphicon icon)
    [:label {:for (str "pref-" id)} title]
    (check-box {:class "pref" :disabled "disabled"} (str "pref-" id))]])

(defn nav-links [request]
  (let [github-issue-uri "https://github.com/innoq/statuses/issues"
        info-uri         "/statuses/info"]
    (list (nav-link (mention-uri request)       "Mentions"        "user")
          (nav-link (updates-uri request :atom) "Feed (all)"      "fire")
          (nav-link (mention-uri request :atom) "Feed (mentions)" "fire")
          (nav-link info-uri                    "Info"            "info-sign")
          (nav-link github-issue-uri            "Issues"           "question-sign")
          (preference "inline-images"           "Inline images?"  "wrench"))))

(defn format-time [time]
  [:time {:datetime (time/time-to-utc time)} (time/time-to-human time)])

(defn delete-form [id]
  (form-to {:class "delete-form" :onsubmit "return confirm('Delete status?')"} [:delete (str base "/" id)]
    (html [:button {:type "submit" :class "btn btn-delete"} (html [:span.fa.fa-remove ][:span.btn-label "Delete"])])
    ))

(defn update [request {:keys [id text author time in-reply-to conversation can-delete?]}]
  (list
    [:div.avatar
     (link-to (str (config :profile-url-prefix) author) [:img {:src (avatar-uri author) :alt author}])]
    [:div.meta
     [:span.author (link-to (str base "?author=" author) author)]
     [:span.time [:a.permalink {:href (str base "/" id)} (format-time time)]]
     (if in-reply-to
       (list
         [:span.reply (link-to (str base "/" in-reply-to) in-reply-to)]))
     ;; conversation link should always be shown if the post is part of a conversation
     (if conversation
       (list
         [:span.conversation (link-to (str "/statuses/conversations/" conversation)
                               conversation)]))
     [:span.actions
      [:button {:type "submit" :class "btn btn-reply"} (html [:span.fa.fa-reply ][:span.btn-label "Reply"])]
      (if can-delete?
        (list
          [:span.delete (delete-form id)]))]
     ]
    [:div.post-content (common/linkify text)]
  )
)

(defn entry-form []
  (form-to {:class "entry-form"} [:post base]
         [:div.input.input-group
           (text-field {:class "form-control" :autofocus "autofocus"} "entry-text")
           [:span.input-group-btn
             [:button {:type "submit" :class "btn btn-default"} "Send" ]]]
         [:div {"style" "clear: both"}]))

(defn reply-form [id author]
  (form-to {:class (str "reply-form form" id) } [:post base]
    [:div.input-group
      (text-field {:class "form-control" :autofocus "autofocus" :value (str "@" author " ")} "reply-text")
      [:span.input-group-btn
        [:button {:type "submit" :class "btn btn-default"} "Reply" ]]]
    (hidden-field "reply-to" id)
    [:div {"style" "clear: both"}]))

(defn list-page [items next request]
  (common/layout
    (list
      (entry-form)
      [:ul.updates (map (fn [item] [:li.post (update request item)]) items)])
    (if next
        (link-to {:rel "next"} next "Next"))
    (nav-links request)))

(defn update-page [item request]
  (common/layout
    (list
      [:div.update (update request item)]
      (reply-form (:id item) (:author item)))
    nil
    (nav-links request)))

