(ns statuses.views.atom
  (:require [clj-time.format :refer [formatters unparse]]
            [hiccup.util :refer [escape-html]]
            [statuses.views.common :refer [linkify]]))

(defn- as-rfc3339
  "Converts the given time to rfc3339. E.g.: 1985-04-12T23:20:50.52Z"
  [time]
  (unparse (formatters :date-time) time))

(defn- create-feed-entry-content
  "Creates the content for a feed entry."
  [content]
  [:content {:type "html"}
   (-> content
       linkify
       escape-html)])

(defn- create-feed-entry
  "Creates a new feed entry for the given uri and update."
  [base-uri {:keys [id author text time]}]
  [:entry
   [:title (str "Posted by @" author)]
   [:author
    [:name author]
    [:uri (str base-uri "?author=" author)]]
   (create-feed-entry-content text)
   [:id (str "tag:innoq.com,2012:statuses/" id)]
   [:published (as-rfc3339 time)]
   [:updated (as-rfc3339 time)]
   [:link {:href (str base-uri "/" id)}]])

(defn feed
  "Creates an atom feed for the given updates and uri."
  [items base-uri feed-uri]
  (into [:feed {:xmlns "http://www.w3.org/2005/Atom"}
         [:title "innoQ Status updates"]
         [:id base-uri]
         [:updated (as-rfc3339 (:time (first items)))]
         [:link {:rel "self" :href feed-uri :type "application/atom+xml"}]
         [:author
          [:name "innoQ"]
          [:uri base-uri]]]
        (map (partial create-feed-entry base-uri) items)))

