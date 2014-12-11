(ns statuses.views.layout
  (:require [hiccup.element :refer [link-to]]
            [hiccup.form :refer [check-box]]
            [hiccup.page :refer [html5 include-css include-js]]
            [statuses.configuration :refer [config]]
            [statuses.routes :refer [info-path issue-path mention-path]]
            [statuses.views.common :refer [icon]]))

(defn preference [id title iconname]
  [:li [:a {:name id}
    (icon iconname)
    [:label {:for (str "pref-" id)} title]
    (check-box {:class "pref" :disabled "disabled"} (str "pref-" id))]])

(defn nav-link [url title iconname]
  [:li (link-to url (icon iconname) title)])

(defn nav-links [username]
  (list (nav-link (mention-path username)       "Mentions"        "at")
        (nav-link (mention-path username :atom) "Feed (mentions)" "rss")
        (nav-link (info-path)                   "Info"            "info")
        (nav-link (issue-path)                  "Issues"          "github")
        (preference "inline-images"             "Inline images?"  "cogs")))

(defn default
  ([title username content] (default title username content nil))
  ([title username content footer]
   (html5
     [:head
      [:meta {:charset "utf-8"}]
      [:meta {:name "viewport"
              :content "width=device-width, initial-scale=1.0, maximum-scale=1, user-scalable=no"}]
      [:title (str title " - innoQ Statuses")]
      (include-css "//maxcdn.bootstrapcdn.com/bootstrap/3.2.0/css/bootstrap.min.css")
      (include-css "//maxcdn.bootstrapcdn.com/font-awesome/4.2.0/css/font-awesome.min.css")
      (include-css "/statuses/css/statuses.css")
      [:link {:href "/statuses/updates?format=atom"
              :rel "alternate"
              :title (config :title); TODO: use title here?
              :type "application/atom+xml"}]
      [:style "body {  }"]]
     [:body
      [:header
       [:nav.navbar.navbar-default.navbar-fixed-top {:role "navigation"}
        [:div.container-fluid
         [:div.navbar-header
          [:button.navbar-toggle.collapsed {:type "button" :data-target ".navbar-collapse" :data-toggle "collapse"}
           [:span.sr-only "Toggle navigation"]
           [:span.icon-bar]
           [:span.icon-bar]
           [:span.icon-bar]]
          [:a {:class "navbar-brand", :href "/statuses/updates"} "Statuses"]]
         [:div.collapse.navbar-collapse
          [:ul.nav.navbar-nav (nav-links username)]]]]]
      [:main.container-fluid.tweet-wrapper content]
      [:footer footer]
      (include-js "//ajax.googleapis.com/ajax/libs/jquery/1.11.1/jquery.min.js")
      (include-js "//maxcdn.bootstrapcdn.com/bootstrap/3.2.0/js/bootstrap.min.js")
      (include-js "//cdnjs.cloudflare.com/ajax/libs/moment.js/2.8.4/moment.min.js")
      (include-js "/statuses/lib/jquery-charCount.js")
      (include-js "/statuses/lib/modernizr.min.js")
      (include-js "/statuses/js/statuses.js")])))

(defn simple [content] (html5 [:body content]))

