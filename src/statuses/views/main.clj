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
  [:span {:class (str "fa fa-" icon)}])

(defn- button
  ([class label] (button class label nil))
  ([class label icon]
  [:button {:type "submit" :class (str "btn btn-" class)} (html (glyphicon icon) [:span.btn-label label])]))

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
    (list (nav-link (mention-uri request)       "Mentions"        "at")
          ; too many navbar items break the navbar layout at ~850px screen width
          ;(nav-link (updates-uri request :atom) "Feed (all)"      "fire")
          (nav-link (mention-uri request :atom) "Feed (mentions)" "rss")
          (nav-link info-uri                    "Info"            "info")
          (nav-link github-issue-uri            "Issues"           "github")
          (preference "inline-images"           "Inline images?"  "cogs"))))

(defn get-ogdata [title item url]
  (list
    [:meta {:name "og:site_name" :content "innoQ Statuses"}]
    [:meta {:name "og:type" :content "article"}]
    [:meta {:property "article:section" :content "Technology"}]
    [:meta {:name "og:locale" :content "de"}]
    [:meta {:name "og:title" :content title}]
    (if (not (nil? item))
      (list
        [:meta {:name "og:image" :content (avatar-uri (:author item))}]
        [:meta {:name "og:description" :content (:text item)}]
        [:meta {:property "article:author" :content (:author item)}]
        [:meta {:property "article:published_time" :content (time/time-to-utc (:time item))}]
        [:meta {:name "og:url" :content (str url "/" (:id item))}])
        [:meta {:name "og:url" :content url}])))

(defn delete-form [id]
  (form-to {:class "delete-form" :onsubmit "return confirm('Delete status?')"} [:delete (str base "/" id)]
    (button "delete" "Delete" "remove")))

(defn entry-form []
  (form-to {:class "entry-form"} [:post base]
    [:div.input.input-group
     (text-field {:class "form-control" :autofocus "autofocus"} "entry-text")
     [:span.input-group-btn
      (button "default" "Send")]]
    [:div {"style" "clear: both"}]))

(defn reply-form [id author request]
  (common/simple
    (form-to {:class (str "reply-form form" id)} [:post base]
      [:div.input-group (text-field {:class "form-control" :autofocus "autofocus" :value (str "@" author " ")} "reply-text")
       [:span.input-group-btn (button "default" "Reply")]]
      (hidden-field "reply-to" id)
      [:div {"style" "clear: both"}])))


(defn update [request is-current {:keys [id text author time in-reply-to conversation can-delete?]}]
  (list
    [:div.avatar
     (link-to (str (config :profile-url-prefix) author) [:img {:src (avatar-uri author) :alt author}])]
    [:div.meta
     [:span.author (link-to (str base "?author=" author) author)]
     (if in-reply-to
       [:span.reply (link-to (str base "/" in-reply-to) in-reply-to)])
     [:span.actions (button "reply" "Reply" "reply")
      (if can-delete?
        [:span.delete (delete-form id)])]
     [:span.time [:a.permalink {:href (str base "/" id)} (time/format-time time)]]
     ]
    [:div.post-content (common/linkify text)]
  )
)

(defn list-page [items next request current-item]
  (let [title (if (nil? current-item) "timeline" (str "Status " (:id current-item)))]
    (common/layout
      title
      (get-ogdata title (or current-item nil) base)
      (list
        (if (nil? current-item) (entry-form))
        [:ul.updates (map (fn [item]
                            (if (= (:id current-item) (:id item))
                              [:li.post.current (update request true item)]
                              [:li.post (update request false item)]
                              )) items)]
        )
      (if next
        (link-to {:rel "next"} next "Next"))
      (nav-links request))))
