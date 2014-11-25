(ns statuses.views.atom
  (:require [clj-time.format :refer [formatters unparse]]
            [hiccup.core :refer [html]]
            [hiccup.util :refer [escape-html]]
            [statuses.views.common :refer [linkify]]))

(defn as-rfc3339
  "Converts the given time to rfc3339. E.g.: 1985-04-12T23:20:50.52Z"
  [time]
  (unparse (formatters :date-time) time))

(defn create-feed-entry-content
  "Creates the content for a feed entry."
  [content]
  [:content {:type "html"}
   (-> content
       linkify
       escape-html)])

(defn create-feed-entry
  "Creates a new feed entry for the given uri and update."
  [base-uri {:keys [id author text time conversation in-reply-to]}]
  [:entry
   [:title (str "Posted by @" author)]
   [:author
    [:name author]
    [:uri (str base-uri "/updates?author=" author)]]
   (create-feed-entry-content text)
   [:id (str "tag:innoq.com,2012:statuses/" id)]
   [:published (as-rfc3339 time)]
   [:updated (as-rfc3339 time)]
   [:link {:rel "alternate"
           :type "text/html"
           :href (str base-uri "/updates/" id)}]
   (when conversation
     [:link {:rel "related"
             :type "text/html"
             :href (str base-uri "/conversations/" conversation)}])
  (when in-reply-to
    [:link {:rel "prev"
            :type "text/html"
            :href (str base-uri "/updates/" in-reply-to)}])])

(defn feed
  "Creates an atom feed for the given updates and uri."
  [items base-uri feed-uri]
  (into [:feed {:xmlns "http://www.w3.org/2005/Atom"}
         [:title "innoQ Status updates"]
         [:id feed-uri]
         [:updated (as-rfc3339 (:time (first items)))]
         [:link {:rel "self"
                 :type "application/atom+xml"
                 :href feed-uri}]
         [:author
          [:name "innoQ"]
          [:uri (str base-uri "/updates")]]]
        (map (partial create-feed-entry base-uri) items)))

(defn render-atom
  "Renders the atom feed for the given items with the given URIs."
  [items base-uri feed-uri]
  (html (feed items base-uri feed-uri)))

