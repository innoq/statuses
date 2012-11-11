(ns statuses.views.common
  (:use [noir.core :only [defpartial]]
        [hiccup.page-helpers :only [include-css include-js html5]]))

(defpartial layout [& content]
            (html5
              [:head
               [:title "innoQ Statuses"]
               (include-css "/css/bootstrap.css")
               (include-css "/css/bootstrap-responsive.css")
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
                      [:a {"href" "/status"} "Status"]]
                     [:li
                      [:a {"href" "https://internal.innoq.com/blogging/"} "PPP"]]]]]]]
                [:div.container content]
                (include-js "http://ajax.googleapis.com/ajax/libs/jquery/1.7.1/jquery.min.js")
                (include-js "/js/bootstrap.min.js"))]))
