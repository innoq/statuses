(ns statuses.views.main
  (:require [statuses.views.common :as common]
            [statuses.views.atom :as atom]
            [statuses.backend.time :as time])
  (:use [hiccup.core :only [html]]
        [hiccup.element :only [link-to]]
        [hiccup.form :only [form-to text-field hidden-field submit-button]]
        [hiccup.util :only [escape-html]]))

(def base "/statuses/updates")

(defn user [request]
  (or (get-in request [:headers "remote_user"]) "guest"))

(defn avatar-uri [username]
  (str "https://testldap.innoq.com/liqid2/users/" username "/avatar/32x32")) ; TODO: configurable

(defn nav-links [request]
  (let [elems [ base  "Everything"
                (str base "?query=@" (user request)) "Mentions"
                (str base "?format=atom&author=@" (user request)) "Feed for everything"
                (str base "?format=atom&query=@" (user request)) "Feed for mentions"
                "/statuses/info" "Server info" ]]
    (map (fn [[url text]] [:li (link-to url text)]) (partition 2 elems))))

(def uri #"\b((?:[a-z][\w-]+:(?:\/{1,3}|[a-z0-9%])|www\d{0,3}[.]|[a-z0-9.\-]+[.][a-z]{2,4}\/)(?:[^\s()<>]+|\(([^\s()<>]+|(\([^\s()<>]+\)))*\))+(?:\(([^\s()<>]+|(\([^\s()<>]+\)))*\)|[^\s`!()\[\]{};:'\".,<>?«»“”‘’]))")

(defn linkify [text]
  (let [handle  (fn [[_ m]] (str "@<a href='/statuses/updates?author=" m "'>" m "</a>"))
        hashtag (fn [[_ m]] (str "#<a href='/statuses/updates?query=%23" m "'>" m "</a>"))
        anchor  (fn [[m _]] (str "<a href='" m "'>" m "</a>"))]
    (-> text
        escape-html
        (clojure.string/replace #"@(\w*)" handle)
        (clojure.string/replace uri anchor)
        (clojure.string/replace #"#(\w*)" hashtag))))

(defn format-time [time]
    [:time {:datetime (time/time-to-utc time)} (time/time-to-human time)])

(defn update [{:keys [id text author time in-reply-to conversation]}]
  (list [:img.avatar {:src (avatar-uri author) :alt author}]
        [:div.content (linkify text)]
        [:div.meta
         [:span.author (link-to (str base "?author=" author) author)]
         [:span.time (link-to (str base "/" id) (format-time time))]
         (if in-reply-to
           (list
            [:span.reply (link-to (str base "/" in-reply-to) in-reply-to)]
            [:span.conversation (link-to (str "/statuses/conversations/" conversation)
                                         conversation)]))]))


(defn entry-form []
  (form-to [:post base]
           (text-field {:class "input-xxlarge" :autofocus "autofocus" } "text")
           (submit-button "Send update")))

(defn reply-form [id author]
  (form-to [:post base]
           (text-field {:class "input-xxlarge"
                        :autofocus "autofocus"
                        :value (str "@" author) } "text")
           (hidden-field "reply-to" id)
           (submit-button "Reply")))

(defn list-page [items next request]
  (common/layout
   (list [:div (entry-form)]
         [:div [:ul.updates (map (fn [item] [:li.post (update item)]) items)]]
         (if next (link-to next "Next")))
   (nav-links request)))

(defn update-page [item request]
  (common/layout
   (list [:div.update (update item)]
         (reply-form (:id item) (:author item)))
   (nav-links request)))



