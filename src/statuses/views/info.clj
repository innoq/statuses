(ns statuses.views.info
  (:require [clojure.pprint :refer [pprint]]
            [hiccup.element :refer [link-to]]
            [statuses.backend.core :refer [get-count]]
            [statuses.backend.persistence :refer [db get-save-time]]
            [statuses.configuration :refer [config]]
            [statuses.routes :refer [commit-path]]
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

(defn- version-item []
  (let [version (config :version)]
    (item "Version"
        (when version
          (link-to (commit-path version) version)))))

(defn render-html [username request]
  (layout/default
    "Server Info"
    username
    [:table.table
     (version-item)
     (item "# of entries" (get-count @db))
     (item "Last save at" (get-save-time @db))
     (item "Base URI" (base-uri request))
     (if (= (config :run-mode) :dev)
       (item "Request" [:pre (with-out-str (pprint request))]))]))

