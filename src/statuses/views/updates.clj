(ns statuses.views.updates
  (:require [statuses.views.common :as common]
            [statuses.backend.core :as core]
            [clj-time.format :as format]
            [clj-time.local :as local])
  (:use [statuses.backend.persistence :only [db]]
        [noir.core :only [defpage defpartial render]]
        [noir.response :only [redirect]]
        [noir.request :only [ring-request]]
        [hiccup.element]
        [hiccup.form]
        [hiccup.page]))

(defn user []
  (or (get-in (ring-request) [:headers "remote_user"]) "guest"))

(defn nav-links []
  (let [elems [ "/statuses/updates"  "Everything"
                (str "/statuses/mentions/" (user)) "Mentions" ]]
    (map (fn [[url text]] [:li (link-to url text)]) (partition 2 elems))))

(defn format-time [time]
  (let [rfc822 (local/format-local-time time :rfc822)
        human  (format/unparse (format/formatter "yyyy-MM-dd HH:mm") time)]
    [:time {:datetime rfc822} human]))

(defpartial update [{:keys [id text author time in-reply-to]}]
  [:div.content text]
  [:div.meta
   [:span.author (link-to (str "/statuses/authors/" author) author)]
   [:span.time (link-to (str "/statuses/updates/" id) (format-time time))]
   (if in-reply-to
     [:span.reply (link-to (str "/statuses/updates/" in-reply-to) in-reply-to)])])

(defn entry-form [& [reply]]
  (form-to [:post "/statuses/updates"]
           (text-field {:class "input-xxlarge" :autofocus "autofocus" } "text")
           (if reply
             (list (hidden-field "reply-to" reply)
                   (submit-button "Reply"))
             (submit-button "Send update"))))

(defpartial list-page [items next]
  (common/layout
   (list [:div (entry-form)]
         [:div [:ul.updates (map (fn [item] [:li.post (update item)]) items)]]
         (link-to next "Next"))
   (nav-links)))

(defpartial update-page [item]
  (common/layout
   (list [:div.update (update item)]
         (entry-form (:id item)))
   (nav-links)))


(defpage "/" []
  (redirect "/statuses/updates"))

(defpage "/statuses" []
  (redirect "/statuses/updates"))

(defn parse-num [s default]
  (if (nil? s) default (read-string s)))

(defpage "/statuses/authors/:author" {:keys [author lmt ofst]}
  (let [limit (parse-num lmt 25)
        offset (parse-num ofst 0)
        next (str (:uri (ring-request)) "?limit=" limit "&offset=" (+ limit offset))]
        (list-page (core/get-latest @db
                                    limit offset
                                    author)
                   next)))

(defpage updates-page "/statuses/updates" {:as req}
  (render "/statuses/authors/:author" req))

(defpage "/statuses/updates/:id" {:keys [id]}
  (update-page (core/get-update @db (Integer/parseInt id))))

(defpage [:post "/statuses/updates"] {:keys [text reply-to]}
  (swap! db core/add-update (user) text (parse-num reply-to nil))
  (redirect "/statuses"))

