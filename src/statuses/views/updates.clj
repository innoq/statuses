(ns statuses.views.updates
  (:require [statuses.views.common :as common]
            [statuses.backend.core :as core])
  (:use [noir.core :only [defpage defpartial]]
        [noir.response :only [redirect]]
        [noir.request :only [ring-request]]
        [hiccup.element]
        [hiccup.form]
        [hiccup.page]))



(defpartial update [{:keys [id text author time in-reply-to]}]
  [:div.content text]
  [:div.meta
   [:span.author (link-to (str "/status/authors/" author) author)]
   [:span.time (link-to (str "/status/updates/" id) time)]
   (if in-reply-to [:span.reply (link-to (str "/status/updates/" in-reply-to) in-reply-to)])])

(defn user []
  (or (get-in ring-request [:headers "remote-user"]) "guest"))

(defn entry-form
  ([author]       (form-to [:post "/status"]
                           (text-field "text")
                           (submit-button "Send update")))
  ([author reply] (form-to [:post "/status"]
                           (text-field "text")
                           (hidden-field "reply-to" reply)
                           (submit-button "Reply"))))


(defpartial list-page [items]
  (common/layout
   (entry-form (user))
   [:ul.updates (map (fn [item] [:li.post (update item)]) items)]))

(defpartial update-page [item]
  (common/layout
   [:div.update (update item)]
   (entry-form (user) (:id item))))

(defpage "/" []
  (redirect "/status"))

(defpage "/status" []
  (list-page (core/get-latest @core/dummy-db 25)))


(defn parse-num [s] (if (nil? s) nil (read-string s)))

(defpage [:post "/status"] {:keys [text reply-to]}
  (swap! core/dummy-db core/add-update (user) text (parse-num reply-to))
  (redirect "/status"))

(defpage "/status/updates/:id" {:keys [id]}
  (update-page (core/get-update @core/dummy-db (Integer/parseInt id))))

(defpage "/status/authors/:author" {:keys [author]}
  (list-page (core/get-latest @core/dummy-db 25 author)))

(defpage "/db-dump" []
  (str @core/dummy-db))