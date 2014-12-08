(ns statuses.views.info
  (:require [clojure.pprint :refer [pprint]]
            [statuses.backend.core :refer [get-count]]
            [statuses.backend.persistence :refer [db get-save-time]]
            [statuses.configuration :refer [config]]
            [statuses.views.layout :as layout]))

(defn- base-uri [request]
  (str
   (name (or (get-in request [:headers "x-forwarded-proto"]) (:scheme request)))
   "://"
   (get-in request [:headers "host"])))

(defn- item [header content]
  [:tr
   [:td header]
   [:td content]])

(defn render-html [username request]
  (layout/default
    "Server Info"
    username
    [:table.table
     (item "Version" (config :version))
     (item "# of entries" (get-count @db))
     (item "Last save at" (get-save-time @db))
     (item "Base URI" (base-uri request))
     (if (= (config :run-mode) :dev)
       (item "Request" [:pre (with-out-str (pprint request))]))]))

