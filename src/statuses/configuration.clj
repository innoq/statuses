(ns statuses.configuration)

(def default-config
  {:title "Status Updates"
   :database-path "data/db.json"
   :save-interval 1
   :http-port     8080
   :run-mode      :dev
   :avatar-url    "http://assets.github.com/images/gravatars/gravatar-user-420.png"})

(def config-holder (atom default-config))

(defn config
  ([] @config-holder)
  ([key] (get @config-holder key)))

(defn init! [path]
  (if path
    (try
      (println "Initializing configuration from" path ":")
      (swap! config-holder merge (read-string (slurp path)))
      (catch java.io.FileNotFoundException e
        (println "Configuration not found, exiting.")
        (System/exit -1)))
    (println "Using default configuration."))
  (try
    (println "Initializing commit revision from" path ":")
    (swap! config-holder merge {:version (slurp "headrev.txt")})
    (println "Version is" (config :version))
    (catch java.io.FileNotFoundException e
      (println "Version not found, continuing anyway"))))

