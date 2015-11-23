(ns statuses.views.main
  (:require [clj-time.local :refer [format-local-time]]
            [hiccup.core :refer [html]]
            [hiccup.element :refer [link-to]]
            [hiccup.form :refer [form-to hidden-field text-field]]
            [statuses.configuration :refer [config]]
            [statuses.routes :refer [avatar-path update-path
                                     updates-path profile-path]]
            [statuses.views.common :as common :refer [icon]]
            [statuses.views.layout :as layout]))

(defn- button
  ([class label] (button class label nil))
  ([class label icon-name]
  [:button {:type "submit" :class (str "btn btn-" class)} (html (icon icon-name) [:span.btn-label label])]))

(defn format-time [time]
  [:time {:datetime (format-local-time time :date-time)} (format-local-time time :rfc822)])

(defn delete-form [id]
  (form-to {:class "delete-form" :onsubmit "return confirm('Delete status?')"} [:delete (update-path id)]
    (button "delete" "Delete" "remove")))

(defn entry-form []
  (form-to {:class "entry-form"} [:post (updates-path)]
    [:div.input.input-group
     (text-field {:class "form-control" :autofocus "autofocus"} "entry-text")
     [:span.input-group-btn
      (button "default" "Send")]]
    [:div {"style" "clear: both"}]))

(defn reply-form [id author]
  (layout/simple
    (form-to {:class (str "reply-form form" id)} [:post (updates-path)]
      [:div.input-group (text-field {:class "form-control" :autofocus "autofocus" :value (str "@" author " ")} "reply-text")
       [:span.input-group-btn (button "default" "Reply")]]
      (hidden-field "reply-to" id)
      [:div {"style" "clear: both"}])))


(defn update-item [is-current {:keys [id text author time in-reply-to conversation can-delete?]}]
  (list
    [:div.avatar
     (link-to (profile-path author) [:img {:src (avatar-path author) :alt author}])]
    [:div.meta
     [:span.author (link-to (str (updates-path) "?author=" author) author)]
     (if in-reply-to
       [:span.reply (link-to (update-path in-reply-to) in-reply-to)])
     [:span.actions (button "reply" "Reply" "reply")
      (if can-delete?
        [:span.delete (delete-form id)])]
     [:span.time [:a.permalink {:href (update-path id)} (format-time time)]]
     ]
    [:div.post-content (common/linkify text)]
  )
)

(defn list-page [items next username current-item-id]
  (layout/default
    (if current-item-id (str "Status " current-item-id) "timeline")
    username
    (list
      (when-not current-item-id (entry-form))
      [:ul.updates (map (fn [item]
                          (if (= current-item-id (:id item))
                            [:li.post.current (update-item true item)]
                            [:li.post (update-item false item)]))
                        items)])
    (when next (link-to {:rel "next"} next "Next"))))

