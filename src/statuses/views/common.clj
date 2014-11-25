(ns statuses.views.common
  (:use [hiccup.page :only [include-css include-js html5]]
        [hiccup.element :only [link-to]]
        [hiccup.util :only [escape-html]]
        [statuses.configuration :only [config]]
        ))


(defn parse-num [s default]
  (if (nil? s) default (read-string s)))

(defn layout [title additional-meta content footer navigation]
            (html5
              [:head
               [:meta {:name "viewport" :content "width=device-width, initial-scale=1.0, maximum-scale=1, user-scalable=no"}]
               additional-meta
               [:title (str title " - innoQ Statuses")]
               (include-css "https://maxcdn.bootstrapcdn.com/bootstrap/3.2.0/css/bootstrap.min.css")
               (include-css "//maxcdn.bootstrapcdn.com/font-awesome/4.2.0/css/font-awesome.min.css")
               (include-css "/statuses/css/statuses.css")
               [:link {:href "/statuses/updates?format=atom"
                       :rel "alternate"
                       :title (config :title)
                       :type "application/atom+xml"}]]
              [:body
               (list
                [:header.navbar.navbar-default.navbar-fixed-top {:role "navigation"}
                  [:div.container-fluid
                   [:div.navbar-header
                    [:button.navbar-toggle.collapsed {:type "button" :data-target ".navbar-collapse" :data-toggle "collapse"}
                     [:span.sr-only "Toggle navigation"]
                     [:span.icon-bar]
                     [:span.icon-bar]
                     [:span.icon-bar]]
                    [:a {:class "navbar-brand", :href "/statuses/updates"} "Statuses"]]
                    [:div.collapse.navbar-collapse
                     [:ul.nav.navbar-nav
                      navigation
                     ]]]]
                 [:main.container-fluid.tweet-wrapper content]
                 [:footer footer]
                 )
                (include-js "https://ajax.googleapis.com/ajax/libs/jquery/1.11.1/jquery.min.js")
                (include-js "https://maxcdn.bootstrapcdn.com/bootstrap/3.2.0/js/bootstrap.min.js")
                (include-js "/statuses/lib/jquery-charCount.js")
                (include-js "/statuses/lib/modernizr.min.js")
                (include-js "/statuses/js/statuses.js")]))

(defn simple [content]
  (html5
    [:body
       content]))

(def uri #"\b((?:[a-z][\w-]+:(?:\/{1,3}|[a-z0-9%])|www\d{0,3}[.]|[a-z0-9.\-]+[.][a-z]{2,4}\/)(?:[^\s()<>]+|\(([^\s()<>]+|(\([^\s()<>]+\)))*\))+(?:\(([^\s()<>]+|(\([^\s()<>]+\)))*\)|[^\s`!()\[\]{};:'\".,<>?«»“”‘’]))")

;; see: http://www.regular-expressions.info/email.html
(def email #"([a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*@(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?)")

(defn linkify [text]
  (let [handle  (fn [[_ m]] (str "@<a href='/statuses/updates?author=" m "'>" m "</a>"))
        hashtag (fn [[_ m]] (str "#<a href='/statuses/updates?query=%23" m "'>" m "</a>"))
        anchor  (fn [[m _]] (str "<a href='" m "'>" m "</a>"))
        mailto  (fn [[m _]] (str "<a href='mailto:" m "'>" m "</a>"))]
    (-> text
        escape-html
        (clojure.string/replace #"(?:^|(?<=\s))@(\w+)" handle)
        (clojure.string/replace uri anchor)
        (clojure.string/replace email mailto)
        (clojure.string/replace #"(?:^|(?<=\s))#(\S+)" hashtag))))
