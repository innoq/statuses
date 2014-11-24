(ns statuses.routing
  (:require [statuses.backend.persistence :as persistence]
            [compojure.route :as route]
            [ring.util.response :as resp]
            [statuses.views.common :as common]
            [statuses.views.atom :as atom]
            [statuses.views.info :as info-view]
            [statuses.backend.core :as core]
            [statuses.backend.json :as json]
            [statuses.backend.time :as time])
  (:use [statuses.backend.persistence :only [db]]
        [compojure.core :only [defroutes GET POST DELETE]]
        [hiccup.core :only [html]]
        [statuses.views.main :only [list-page nav-links user base reply-form]]))

(defn parse-num [s default]
  (if (nil? s) default (read-string s)))

(defn base-uri [request]
  (str
   (name (or (get-in request [:headers "x-forwarded-proto"]) (:scheme request)))
   "://"
   (get-in request [:headers "host"])))

(defn content-type
  [type body]
  (assoc-in {:body body} [:headers "content-type"] type))

(def max-length 140)

(defn build-query-string
  [m]
  (clojure.string/join "&" (map (fn [[key val]] (str (name key) "=" val)) m)))

(defn next-uri [params request]
  (if (< (:offset params) (core/get-count @db))
    (str (base-uri request) (:uri request) "?" (build-query-string params))
    nil))

(defmacro with-etag
  "Ensures body is only evaluated if etag doesn't match. Try to do this in Java, suckers."
  [request etag & body]
  `(let [last-etag# (get-in ~request [:headers "if-none-match"])
         etag-str# (str ~etag)]
      (if (= etag-str# last-etag#)
        {:location (:uri ~request), :status 304, :body ""}
        (assoc-in ~@body [:headers "etag"] etag-str#))))

(defn updates-page [params request]
  (let [next (next-uri (update-in params [:offset] (partial + (:limit params))) request)
        {:keys [limit offset author query format]} params]
    (with-etag request (:time (first (core/get-latest @db 1 offset author query)))
      (let [items (->> (core/get-latest @db limit offset author query)
                    (core/label-updates
                      :can-delete?
                      (partial core/can-delete? @db (user request))))]
        (cond
         (= format "json") (content-type
                            "application/json"
                            (json/as-json {:items items, :next next}))
         (= format "atom") (content-type
                             "application/atom+xml;charset=utf-8"
                             (html (atom/feed items
                                              (str (base-uri request) "/statuses")
                                              (str (base-uri request)
                                                   "/statuses/updates?"
                                                   (:query-string request)))))
         :else             (content-type
                            "text/html;charset=utf-8"
                            (list-page items next request nil)))))))

(defn new-update
  "Handles the request to add a new update. Checks whether the post values 'entry-text' or
  'reply-text' have a valid length and if so, creates the new update."
  [{:keys [form-params] :as request}]
  (let [{:strs [entry-text reply-text reply-to]} form-params
        field-value (or entry-text reply-text "")
        length (.length field-value)]
    (if (and (<= length max-length) (> length 0))
      (do (swap! db core/add-update (user request) field-value (parse-num reply-to nil))
          (resp/redirect "/statuses/updates"))
      (resp/redirect (str "/statuses/too-long/" length)))))

(defn keyworded
  "Builds a map with keyword keys from one with strings as keys"
  [m]
  (zipmap (map keyword (keys m)) (vals m)))

(defn transform-params [m defaults]
  (reduce (fn [result [key default]]
            (update-in result [key] #(parse-num % default)))
          (keyworded m)
          defaults))

(defn handle-list-view [request]
  (updates-page (transform-params (:params request) {:limit 25 :offset 0}) request))

(defn delete-entry
  "Deletes an entry if the current user is the author of the entry"
  [id request]
  (if-let [item (core/get-update @db (Integer/parseInt id))]
    (if-not (= (user request) (:author item))
      (resp/response (str "Delete failed as you are not " (:author item)))
      (do (swap! db core/remove-update (Integer/parseInt id))
          (resp/redirect "/statuses/updates")))))

(defn page
  "Returns a listing with either the conversation of the specified item or just the item"
  [id request]
  (when-let [item (core/get-update @db (Integer/parseInt id))]
    (let [items (core/get-conversation @db (:conversation item))]
      (list-page (if (empty? items) (list item) items) nil request (:id item)))))

(defn conversation
  "XXX: Only kept for backwards compatibility to not break existing links to /statuses/conversation/123"
  [id request]
  (list-page (core/get-conversation @db (Integer/parseInt id)) nil request nil))

(defn info [request]
  (info-view/render-html request))

(defn too-long [length request]
  (common/layout
    "text length violation"
   (str "Sorry, the maximum length is " max-length " but you tried " length " characters")
    nil
   (nav-links request)))

(defn replyform
  "Returns a basic HTML form to reply to a certain post."
  [id request]
  (if-let [item (core/get-update @db (Integer/parseInt id))]
    (reply-form (:id item) (:author item) request)))


(defroutes app-routes
  (DELETE [(str base "/:id"), :id #"[0-9]+"]         [id :as r]     (delete-entry id r))
  (POST base                             []             new-update)
  (GET  base                             []             handle-list-view)
  (GET  [(str base "/:id/replyform"), :id #"[0-9]+"] [id :as r]     (replyform id r))
  (GET  [(str base "/:id"), :id #"[0-9]+"]           [id :as r]     (page id r))
  (GET  "/statuses/conversations/:id"    [id :as r]     (conversation id r))
  (GET  "/statuses/info"                 []             info)
  (GET  "/statuses/too-long/:length"     [length :as r] (too-long length r))
  (GET  "/"                              []             (resp/redirect base))
  (GET  "/statuses"                      []             (resp/redirect base))
  (route/not-found "Not Found"))

