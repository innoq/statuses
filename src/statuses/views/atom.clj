(ns statuses.views.atom
  (:require [statuses.backend.time :as time]
            [hiccup.util :as util]))



(defn feed
  [items base-uri feed-uri]
  (let [update-ts (:time (first items))]
   (into [:feed {:xmlns "http://www.w3.org/2005/Atom"}
          [:title "innoQ Status updates"]
          [:id "some-id"]
          [:updated (time/time-for-atom update-ts)]
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
                 [:published (time/time-for-atom time)]                                  
                 [:updated (time/time-for-atom time)]                 
                 [:link {:href (str base-uri "/statuses/updates/" id) }]]) items))))