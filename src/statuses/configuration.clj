(ns statuses.configuration)

(def default-config
  {:title "Status Updates"
   :database-path "data/db.json"
   :save-interval 1
   :http-port     8080
   :run-mode      :dev})

(def config-holder (atom default-config))

(defn init! [path]
  (println "Initializing configuration from" path)
  (reset! config-holder (merge default-config (read-string (slurp path)))))

(defn config
  ([] @config-holder)
  ([key] (get @config-holder key)))