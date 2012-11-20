(ns statuses.views.main
  (:require [statuses.views.common :as common]
            [statuses.backend.core :as core]
            [statuses.backend.json :as json]
            [clj-time.format :as format]
            [clj-time.local :as local])
  (:use [statuses.backend.persistence :only [db get-save-time]]
        [noir.core :only [defpage defpartial render]]
        [noir.response :only [redirect set-headers]]
        [noir.request :only [ring-request]]
        [hiccup.element]
        [hiccup.form]
        [hiccup.page]
        [hiccup.util]))

(defn user []
  (or (get-in (ring-request) [:headers "remote_user"]) "guest"))

(defn nav-links []
  (let [elems [ "/statuses/updates"  "Everything"
                (str "/statuses/search?q=@" (user)) "Mentions"
                "/statuses/info" "Server info" ]]
    (map (fn [[url text]] [:li (link-to url text)]) (partition 2 elems))))

(defn format-time [time]
  (let [rfc822 (local/format-local-time time :rfc822)
        human  (format/unparse (format/formatter "yyyy-MM-dd HH:mm") time)]
    [:time {:datetime rfc822} human]))

(def uri #"\b((?:[a-z][\w-]+:(?:\/{1,3}|[a-z0-9%])|www\d{0,3}[.]|[a-z0-9.\-]+[.][a-z]{2,4}\/)(?:[^\s()<>]+|\(([^\s()<>]+|(\([^\s()<>]+\)))*\))+(?:\(([^\s()<>]+|(\([^\s()<>]+\)))*\)|[^\s`!()\[\]{};:'\".,<>?«»“”‘’]))")

(defn linkify [text]
  (let [handle  (fn [[_ m]] (str "@<a href='/statuses/search?q=@" m "'>" m "</a>"))
        hashtag (fn [[_ m]] (str "#<a href='/statuses/search?q=%23" m "'>" m "</a>"))
        anchor  (fn [[m _]] (str "<a href='" m "'>" m "</a>"))]
    (-> text
        escape-html
        (clojure.string/replace #"@(\w*)" handle)
        (clojure.string/replace uri anchor)
        (clojure.string/replace #"#(\w*)" hashtag))))


(defpartial update [{:keys [id text author time in-reply-to]}]
  [:div.content (linkify text)]
  [:div.meta
   [:span.author (link-to (str "/statuses/authors/" author) author)]
   [:span.time (link-to (str "/statuses/updates/" id) (format-time time))]
   (if in-reply-to
     [:span.reply (link-to (str "/statuses/updates/" in-reply-to) in-reply-to)])])


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

(defn make-etag [item]
  (str (:time item)))

(defn list-page [items next]
  (set-headers {"etag" (make-etag (first items)) "content-type" "text/html;charset=utf-8"}
   (common/layout
     (list [:div (entry-form)]
           [:div [:ul.updates (map (fn [item] [:li.post (update item)]) items)]]
           (link-to next "Next"))
     (nav-links))))

(defpartial update-page [item]
  (common/layout
   (list [:div.update (update item)]
         (reply-form (:id item) (:author item)))
   (nav-links)))


(defpage "/" []
  (redirect "/statuses/updates"))

(defpage "/statuses" []
  (redirect "/statuses/updates"))

(defn parse-num [s default]
  (if (nil? s) default (read-string s)))

(defn parse-args [{:keys [q limit offset]}]
  (let [lmt (parse-num limit 25)
        off (parse-num offset 0)
        req (ring-request)
        q-string (if q (str "&q=" q))
        next (str
              (name (:scheme req))
              "://"
              (get-in req [:headers "host"])
              (:uri req)
              "?limit=" lmt
              "&offset=" (+ lmt off)
              q-string)]
    [q lmt off next]))

(defpage "/statuses/authors/:author" {:keys [author json] :as req}
  (let [[query limit offset next] (parse-args req)]
    (let [current-etag (make-etag (first (core/get-latest @db 1 offset author)))
          last-etag (get-in (ring-request) [:headers "if-none-match"])]
      (if (not= current-etag last-etag)
        (let [items (core/get-latest @db limit offset author)]
          (if json
            (set-headers {"etag" (make-etag (first items))
                          "content-type" "application/json"}
                         (json/as-json {:items items, :next next}))
            (list-page items next)))
        (redirect (:uri (ring-request)) :not-modified)))))


(defpage "/statuses/search" {:as req}
  (let [[query limit offset next] (parse-args req)]
    (list-page (core/get-latest-with-text @db limit offset (str query)) next)))

(defpage updates-page-json "/statuses/updates.json" {:as req}
  (println "Received JSON request")
  (render "/statuses/authors/:author" (assoc req :json true)))

(defpage updates-page "/statuses/updates" {:as req}
  (render "/statuses/authors/:author" req))

(defpage "/statuses/updates/:id" {:keys [id]}
  (update-page (core/get-update @db (Integer/parseInt id))))

(def max-length 140)

(defpage "/statuses/too-long/:length" {:keys [length]}
  (common/layout
   (str "Sorry, the maximum lenght is " max-length " but you tried " length " characters")
   (nav-links)))

(defpage [:post "/statuses/updates"] {:keys [text reply-to]}
  (let [length (.length text)]
    (if (<= length max-length)
      (do (swap! db core/add-update (user) text (parse-num reply-to nil))
          (redirect "/statuses"))
      (redirect (str "/statuses/too-long/" length)))))

(defpage "/statuses/info" []
  (let [item (fn [header content] (list [:tr [:td header] [:td content]]))]
        (common/layout
         [:table.table
          (item "# of entries" (core/get-count @db))
          (item "Last save at" (get-save-time @db))]
          (nav-links))))

