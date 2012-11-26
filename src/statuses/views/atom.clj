(ns statuses.views.atom
  (:require [statuses.backend.time :as time]
            [hiccup.util :as util]
            [statuses.views.common :as common]))



(defn feed
  [items base-uri feed-uri]
  (let [update-ts (:time (first items))
        full-uri (str base-uri feed-uri)]
   (into [:feed {:xmlns "http://www.w3.org/2005/Atom"}
          [:title "innoQ Status updates"]
          [:id full-uri]
          [:updated (time/time-to-rfc3339 update-ts)]
          [:link {:rel "self" :href full-uri :type "application/atom+xml"}]
          [:author
           [:name "innoQ"]
           [:uri base-uri]]]
         (map (fn make-atom-entry
                [{:keys [id author text time]}]
                [:entry
                 [:title (str "Posted by @" author)]
                 [:content {:type (str "html")} (util/escape-html (common/linkify text))]
                 [:id (str "tag:innoq.com,2012:statuses/" id)]
                 [:published (time/time-to-rfc3339 time)]
                 [:updated (time/time-to-rfc3339 time)]
                 [:link {:href (str base-uri "/statuses/updates/" id) }]]) items))))