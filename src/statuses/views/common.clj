(ns statuses.views.common
  (:use [hiccup.page :only [include-css include-js html5]]
        [hiccup.element :only [link-to]]))


(defn layout [content navigation]
            (html5
              [:head
               [:title "innoQ Statuses"]
               (include-css "/statuses/css/bootstrap.css")
               (include-css "/statuses/css/bootstrap-responsive.css")
               (include-css "/statuses/css/statuses.css")
               [:link {:href "/statuses/updates?format=atom"
                       :rel "alternate"
                       :title "innoQ Statuses Feed"
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
                     [:li.active
                      [:a {"href" "/statuses/updates"} "Statuses"]]
                     [:li
                      [:a {"href" "https://internal.innoq.com/blogging/"} "PPP"]]]]]]]
                [:div.container
                 [:div.row
                  [:div.span10 content]
                  [:div.span2
                   [:div.well {:style "padding:10px 0px 10px 0px;"}
                    [:ul {:class "nav nav-list"}
                     navigation ]]]]])]))
