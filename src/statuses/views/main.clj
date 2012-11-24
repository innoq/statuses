(ns statuses.views.main
  (:require [statuses.backend.persistence :as persistence]
            [clojure.pprint :as pp]
            [compojure.handler :as handler]
            [compojure.route :as route]
            [ring.util.response :as resp]
            [statuses.views.common :as common]
            [statuses.views.atom :as atom]
            [statuses.backend.core :as core]
            [statuses.backend.json :as json]
            [statuses.backend.time :as time])
  (:use [statuses.backend.persistence :only [db get-save-time]]
        [compojure.core]
        [compojure.response]
        [hiccup.core :only [html]]
        [hiccup.element]
        [hiccup.form]
        [hiccup.page]
        [hiccup.util]))

(defn user [request]
  (or (get-in request [:headers "remote_user"]) "guest"))

(defn nav-links [request]
  (let [elems [ "/statuses/updates"  "Everything"
                (str "/statuses/updates?q=@" (user request)) "Mentions"
                "/statuses/info" "Server info" ]]
    (map (fn [[url text]] [:li (link-to url text)]) (partition 2 elems))))

(def uri #"\b((?:[a-z][\w-]+:(?:\/{1,3}|[a-z0-9%])|www\d{0,3}[.]|[a-z0-9.\-]+[.][a-z]{2,4}\/)(?:[^\s()<>]+|\(([^\s()<>]+|(\([^\s()<>]+\)))*\))+(?:\(([^\s()<>]+|(\([^\s()<>]+\)))*\)|[^\s`!()\[\]{};:'\".,<>?«»“”‘’]))")

(defn linkify [text]
  (let [handle  (fn [[_ m]] (str "@<a href='/statuses/updates?author=" m "'>" m "</a>"))
        hashtag (fn [[_ m]] (str "#<a href='/statuses/updates?q=%23" m "'>" m "</a>"))
        anchor  (fn [[m _]] (str "<a href='" m "'>" m "</a>"))]
    (-> text
        escape-html
        (clojure.string/replace #"@(\w*)" handle)
        (clojure.string/replace uri anchor)
        (clojure.string/replace #"#(\w*)" hashtag))))

(defn format-time [time]
    [:time {:datetime (time/time-to-utc time)} (time/time-to-human time)])

(defn update [{:keys [id text author time in-reply-to]}]
  (list [:div.content (linkify text)]
        [:div.meta
         [:span.author (link-to (str "/statuses/updates?author=" author) author)]
         [:span.time (link-to (str "/statuses/updates/" id) (format-time time))]
         (if in-reply-to
           [:span.reply (link-to (str "/statuses/updates/" in-reply-to) in-reply-to)])]))


(defn entry-form []
  (form-to [:post "/statuses/updates"]
           (text-field {:class "input-xxlarge" :autofocus "autofocus" } "text")
           (submit-button "Send update")))

(defn reply-form [id author]
  (form-to [:post "/statuses/updates"]
           (text-field {:class "input-xxlarge"
                        :autofocus "autofocus"
                        :value (str "@" author) } "text")
           (hidden-field "reply-to" id)
           (submit-button "Reply")))

(defn list-page [items next request]
  (common/layout
   (list [:div (entry-form)]
         [:div [:ul.updates (map (fn [item] [:li.post (update item)]) items)]]
         (link-to next "Next"))
   (nav-links request)))

(defn update-page [item request]
  (common/layout
   (list [:div.update (update item)]
         (reply-form (:id item) (:author item)))
   (nav-links request)))

(defn parse-num [s default]
  (if (nil? s) default (read-string s)))

(defn base-uri [request]
  (str
   (name (:scheme request))
   "://"
   (get-in request [:headers "host"])))

(defn content-type
  [type body]
  (assoc-in {:body body} [:headers "content-type"] type))

(defn page [id request]
  (update-page (core/get-update @db (Integer/parseInt id)) request))

(def max-length 140)

(defn too-long [length request]
  (common/layout
   (str "Sorry, the maximum lenght is " max-length " but you tried " length " characters")
   (nav-links request)))

(defn info [request]
  (let [item (fn [header content] (list [:tr [:td header] [:td content]]))]
        (common/layout
         [:table.table
          (item "# of entries" (core/get-count @db))
          (item "Last save at" (get-save-time @db))
          (item "Request" [:pre (with-out-str (pp/pprint request))])]
          (nav-links request))))

(defn new-update [{:keys [form-params] :as request}]
  (let [{:strs [text reply-to]} form-params
        length (.length text)]
    (if (<= length max-length)
      (do (swap! db core/add-update (user request) text (parse-num reply-to nil))
          (resp/redirect "/statuses"))
      (resp/redirect (str "/statuses/too-long/" length)))))

(defn next-uri [q limit offset format author request]
  (str (base-uri request) 
       (:uri request)
       "?limit=" limit "&offset=" (+ limit offset)
       (if q (str "&q=" q))
       (if author (str "&author=" author))
       (if format (str "&format=" format))))

(defmacro with-etag
  "Ensures body is only evaluated if etag doesn't match. Try to do this in Java, suckers."
  [request etag & body]
  `(let [last-etag# (get-in ~request [:headers "if-none-match"])
         etag-str# (str ~etag)]
      (if (= etag-str# last-etag#)
        {:location (:uri ~request), :status 304, :body ""}
        (assoc-in ~@body [:headers "etag"] etag-str#))))

(defn updates-page [q limit offset format author request]
  (let [next (next-uri q limit offset format author request)]
    (with-etag request (:time (first (core/get-latest @db 1 offset author)))
      (let [items (core/get-latest @db limit offset author)]
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
  (let [{:strs [q limit offset author format]} (:params request)]
    (updates-page q (parse-num limit 25) (parse-num offset 0) format author request)))


(defroutes app-routes
  (POST "/statuses/updates"              []             new-update)
  (GET  "/statuses/updates"              []             handle-list-view)
  (GET  "/statuses/updates/:id"          [id :as r]     (page id r))
  (GET  "/statuses/info"                 []             info)
  (GET  "/statuses/too-long/:length"     [length :as r] (too-long length r))
  (GET  "/"                              []             (resp/redirect "/statuses/updates"))
  (GET  "/statuses"                      []             (resp/redirect "/statuses/updates"))
  (route/not-found "Not Found"))

