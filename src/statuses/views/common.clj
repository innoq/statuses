(ns statuses.views.common
  (:require [hiccup.core :refer [html]]
            [hiccup.element :refer [link-to]]
            [hiccup.util :refer [escape-html url]]))

(defn icon [icon-name]
  [:span {:class (str "fa fa-" icon-name)}])

(def uri #"\b((?:[a-z][\w-]+:(?:\/{1,3}|[a-z0-9%])|www\d{0,3}[.]|[a-z0-9.\-]+[.][a-z]{2,4}\/)(?:[^\s()<>]+|\(([^\s()<>]+|(\([^\s()<>]+\)))*\))+(?:\(([^\s()<>]+|(\([^\s()<>]+\)))*\)|[^\s`!()\[\]{};:'\".,<>?«»“”‘’]))")

;; see: http://www.regular-expressions.info/email.html
(def email #"([a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*@(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?)")

(defn linkify [text]
  (let [handle  (fn [[_ m]] (str "@<a href='/statuses/updates?author=" m "'>" m "</a>"))
        hashtag (fn [[_ m]] (html "#" (link-to (url "/statuses/updates" {:query (str "#" m)}) m)))
        anchor  (fn [[m _]] (str "<a href='" m "'>" m "</a>"))
        mailto  (fn [[m _]] (str "<a href='mailto:" m "'>" m "</a>"))]
    (-> text
        escape-html
        (clojure.string/replace #"(?:^|(?<=\s))@(\w+)" handle)
        (clojure.string/replace uri anchor)
        (clojure.string/replace email mailto)
        (clojure.string/replace #"(?:^|(?<=\s))#(\S+)" hashtag))))

