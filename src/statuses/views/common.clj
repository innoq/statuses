(ns statuses.views.common
  (:use [hiccup.page :only [include-css include-js html5]]
        [hiccup.element :only [link-to]]
        [hiccup.util :only [escape-html]]
        [statuses.configuration :only [config]]
        ))


(defn layout [content navigation]
            (html5
              [:head
               [:title "innoQ Statuses"]
               (include-css "/statuses/lib/bootstrap.css")
               (include-css "/statuses/lib/bootstrap-responsive.css")
               (include-css "/statuses/lib/jquery-charCount.css")
               (include-css "/statuses/css/statuses.css")
               [:link {:href "/statuses/updates?format=atom"
                       :rel "alternate"
                       :title (config :title)
                       :type "application/atom+xml"}]
               [:style "body { padding-top: 60px; }"]]
              [:body
               (list
                [:div.navbar.navbar-fixed-top {"data-toggle" "collapse" "data-target" ".nav-collapse"}
                 [:div.navbar-inner
                  [:div.container
                   [:a.btn.btn-navbar
                    [:span.icon-bar]]
                  [:a.brand "innoQ"]
                   [:div.nav-collapse
                    [:ul.nav
                     [:li  
                      [:a  {"href" "/dashboard/"} "Dashboard"]]
                     [:li.active
                      [:a {"href" "/statuses/updates"} "Statuses"]]
                     [:li
                      [:a {"href" "/blogging/"} "PPP"]]]]]]]
                [:div.container
                 [:div.row
                  [:div.span10 content]
                  [:div.span2
                   [:div.well {:style "padding:10px 0px 10px 0px;"}
                    [:ul {:class "nav nav-list"}
                     navigation ]]]]])
                (include-js "/statuses/lib/jquery.js")
                (include-js "/statuses/lib/jquery-charCount.min.js")
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
