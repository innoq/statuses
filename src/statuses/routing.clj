(ns statuses.routing
  (:require [statuses.backend.persistence :as persistence]
            [clojure.pprint :as pp]
            [compojure.route :as route]
            [ring.util.response :as resp]
            [statuses.views.common :as common]
            [statuses.views.atom :as atom]
            [statuses.backend.core :as core]
            [statuses.backend.json :as json]
            [statuses.backend.time :as time])
  (:use [statuses.backend.persistence :only [db get-save-time]]
        [compojure.core :only [defroutes GET POST DELETE]]
        [hiccup.core :only [html]]
        [statuses.views.main]))



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
  (let [next (next-uri (update-in params [:offset] #(+ 25 %)) request)
        {:keys [limit offset author query format]} params]
    (with-etag request (:time (first (core/get-latest @db 1 offset author query)))
      (let [items (core/get-latest @db limit offset author query)]
        (cond
         (= format "json") (content-type
                            "application/json"
                            (json/as-json {:items items, :next next}))
         (= format "atom") (content-type
                             "application/atom+xml;charset=utf-8"
                             (html (atom/feed items (base-uri request) (:uri request))))
         :else             (content-type
                            "text/html;charset=utf-8"
                            (list-page items next request)))))))

(defn new-update [{:keys [form-params] :as request}]
  (let [{:strs [text reply-to]} form-params
        length (.length text)]
    (if (<= length max-length)
      (do (swap! db core/add-update (user request) text (parse-num reply-to nil))
          (resp/redirect "/statuses"))
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
  (if (= (user request) (:author (core/get-update @db (Integer/parseInt id))))
    (do (swap! db core/remove-update (Integer/parseInt id))
      (resp/redirect "/statuses"))
    (resp/response (str "Delete failed as you are not " :author )))
  )

(defn page [id request]
  (update-page (core/get-update @db (Integer/parseInt id)) request))

(defn conversation [id request]
  (list-page (core/get-conversation @db (Integer/parseInt id)) nil request))

(defn info [request]
  (let [item (fn [header content] (list [:tr [:td header] [:td content]]))]
        (common/layout
         [:table.table
          (item "# of entries" (core/get-count @db))
          (item "Last save at" (get-save-time @db))
          (item "Base URI" (base-uri request))
          (item "Request" [:pre (with-out-str (pp/pprint request))])]
          (nav-links request))))

(defn too-long [length request]
  (common/layout
   (str "Sorry, the maximum lenght is " max-length " but you tried " length " characters")
   (nav-links request)))


(defroutes app-routes
  (DELETE (str base "/:id")              [id :as r]     (delete-entry id r))
  (POST base                             []             new-update)
  (GET  base                             []             handle-list-view)
  (GET  (str base "/:id")                [id :as r]     (page id r))
  (GET  "/statuses/conversations/:id"    [id :as r]     (conversation id r))
  (GET  "/statuses/info"                 []             info)
  (GET  "/statuses/too-long/:length"     [length :as r] (too-long length r))
  (GET  "/"                              []             (resp/redirect base))
  (GET  "/statuses"                      []             (resp/redirect base))
  (route/not-found "Not Found"))

