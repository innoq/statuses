(ns statuses.views.too-long
  (:require [statuses.configuration :refer [config]]
            [statuses.views.common :refer [layout]]
            [statuses.views.main :refer [nav-links]]))

(defn- error-message [length]
  (let [entry-config (config :entry)]
    (list
      [:span.glyphicon.glyphicon-warning-sign {:style "margin-right: 0.3em"}]
      (str "Sorry, your status must between " (:min-length entry-config)
           " and " (:max-length entry-config) " characters long!"
           " Your status was " length " characters long."))))

(defn render-html [length request]
  (layout
    "text length violation"
    [:div.alert.alert-danger {:role "alert"}
      (error-message length)]
    nil
    (nav-links request)))

