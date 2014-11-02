(ns statuses.views.common
  (:use [hiccup.page :only [include-css include-js html5]]
        [hiccup.element :only [link-to]]
        [hiccup.util :only [escape-html]]
        [statuses.configuration :only [config]]
        ))


(defn layout [content navigation]
            (html5
              [:head
               [:meta {:name "viewport"
                       :content "width=device-width, initial-scale=1.0, maximum-scale=1, user-scalable=no"}]
               [:title "innoQ Statuses"]
               (include-css "https://maxcdn.bootstrapcdn.com/bootstrap/3.2.0/css/bootstrap.min.css")
               (include-css "/statuses/css/statuses.css")
               [:link {:href "/statuses/updates?format=atom"
                       :rel "alternate"
                       :title (config :title)
                       :type "application/atom+xml"}]
               [:style "body {  }"]]
              [:body
               (list
                [:div.navbar.navbar-default.navbar-static-top {"data-toggle" "collapse" "data-target" ".nav-collapse" "style" "position: static; margin-bottom: 18px;" "role" "navigation" }
                  [:div.container-fluid
                   [:div.navbar-header
                    [:button.navbar-toggle.collapsed {"type" "button" "data-toggle" "collapse" "data-target" "#statuses-navbar-collapse"}
                     [:span.sr-only "Toggle navigation"]
                     [:span.icon-bar]
                     [:span.icon-bar]
                     [:span.icon-bar]]
                    [:a.navbar-brand {"href" "/statuses/updates"} "innoQ Statuses"]]]]
                [:div.container-fluid
                 [:div.row
                  [:div.col-xs-12.col-md-8 content]
                  [:div.col-xs-6.col-md-3
                   [:div.well {:style "padding:1em;"}
                    [:ul {:class "nav nav-list"}
                     navigation ]]]]])
                (include-js "https://ajax.googleapis.com/ajax/libs/jquery/1.11.1/jquery.min.js")
                (include-js "https://maxcdn.bootstrapcdn.com/bootstrap/3.2.0/js/bootstrap.min.js")
                (include-js "/statuses/lib/jquery-charCount.js")
                (include-js "/statuses/js/statuses.js")]))

(def uri #"\b((?:[a-z][\w-]+:(?:\/{1,3}|[a-z0-9%])|www\d{0,3}[.]|[a-z0-9.\-]+[.][a-z]{2,4}\/)(?:[^\s()<>]+|\(([^\s()<>]+|(\([^\s()<>]+\)))*\))+(?:\(([^\s()<>]+|(\([^\s()<>]+\)))*\)|[^\s`!()\[\]{};:'\".,<>?«»“”‘’]))")

(defn linkify [text]
  (let [handle  (fn [[_ m]] (str "@<a href='/statuses/updates?author=" m "'>" m "</a>"))
        hashtag (fn [[_ m]] (str "#<a href='/statuses/updates?query=%23" m "'>" m "</a>"))
        anchor  (fn [[m _]] (str "<a href='" m "'>" m "</a>"))]
    (-> text
        escape-html
        (clojure.string/replace #"@(\w+)" handle)
        (clojure.string/replace uri anchor)
        (clojure.string/replace #"(?:^|(?<=\s))#(\S+)" hashtag))))
