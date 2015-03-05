(ns statuses.views.common
  (:require [hiccup.core :refer [html]]
            [hiccup.element :refer [link-to mail-to]]
            [hiccup.util :refer [escape-html url]]))

(defn icon [icon-name]
  [:span {:class (str "fa fa-" icon-name)}])

(def uri #"\b((?:[a-z][\w-]+:(?:\/{1,3}|[a-z0-9%])|www\d{0,3}[.]|[a-z0-9.\-]+[.][a-z]{2,4}\/)(?:[^\s()<>]+|\(([^\s()<>]+|(\([^\s()<>]+\)))*\))+(?:\(([^\s()<>]+|(\([^\s()<>]+\)))*\)|[^\s`!()\[\]{};:'\".,<>?«»“”‘’]))")

;; see: http://www.regular-expressions.info/email.html
(def email #"([a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*@(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?)")

(defn linkify [text]
  (letfn [(handle  [[_ m]] (html "@" (link-to (url "/statuses/updates" {:author m}) m)))
          (hashtag [[_ m]] (html "#" (link-to (url "/statuses/updates" {:query (str "#" m)}) m)))
          (anchor  [[m _]] (html (link-to m m)))
          (mailto  [[m _]] (html (mail-to m)))]
    (let [escaped-text (escape-html text)]
      (try
        (-> escaped-text
            (clojure.string/replace #"(?:^|(?<=\s))@(\w+)" handle)
            (clojure.string/replace uri anchor)
            (clojure.string/replace email mailto)
            (clojure.string/replace #"(?:^|(?<=\s))#(\S+)" hashtag))
        (catch Exception e escaped-text)))))

