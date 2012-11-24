(ns statuses.views.atom
  (:require [statuses.backend.time :as time]
            [hiccup.util :as util]))



(defn feed
  [items base-uri feed-uri]
  (let [update-ts (:time (first items))]
   (into [:feed {:xmlns "http://www.w3.org/2005/Atom"}
          [:title "innoQ Status updates"]
          [:id feed-uri]
          [:updated (time/time-to-rfc3339 update-ts)]
          [:link {:rel "self" :href (str base-uri feed-uri) :type "application/atom+xml"}]
          [:author
           [:name "innoQ"]
           [:uri base-uri]]]
         (map (fn make-atom-entry
                [{:keys [id author text time]}]
                [:entry
                 [:title (str "Posted by @" author)]
                 [:summary (util/escape-html text)]
                 [:id id]
                 [:published (time/time-to-rfc3339 time)]
                 [:updated (time/time-to-rfc3339 time)]
                 [:link {:href (str base-uri "/statuses/updates/" id) }]]) items))))