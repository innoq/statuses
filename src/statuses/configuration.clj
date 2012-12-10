(ns statuses.configuration)

(def default-config
  {:title "Status Updates"
   :database-path "data/db.json"
   :save-interval 1
   :http-port     8080
   :run-mode      :dev})

(def config-holder (atom default-config))

(defn init! [path]
  (if path
    (try
      (println "Initializing configuration from" path ":")
      (swap! config-holder merge (read-string (slurp path)))
      (catch java.io.FileNotFoundException e
        (println "Configuration not found, exiting.")
        (System/exit -1)))
    (println "Using default configuration.")))

(defn config
  ([] @config-holder)
  ([key] (get @config-holder key)))