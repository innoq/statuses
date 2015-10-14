(ns statuses.views.common
  (:require [hiccup.core :refer [html]]
            [hiccup.element :refer [link-to mail-to]]
            [hiccup.util :refer [escape-html url]]))

(defn icon [icon-name]
  [:span {:class (str "fa fa-" icon-name)}])

(def linker (doto (com.twitter.Autolink.)
              (.setNoFollow false)
              (.setLinkAttributeModifier
                (reify com.twitter.Autolink$LinkAttributeModifier
                  (modify [_ entity attributes]
                    (doto attributes
                      (.remove "class")
                      (.remove "title")))))
              (.setHashtagUrlBase "/statuses/updates?query=%23")
              (.setUsernameUrlBase "/statuses/updates?author=")))

(defn linkify [text]
  (when text
    (.autoLink linker text)))
