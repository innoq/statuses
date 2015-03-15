(ns statuses.views.json
  (:require [statuses.routes :refer [avatar-path profile-path]]))

(defn add-avatar [items]
  (map (fn [item] (assoc item :avatar (avatar-path (:author item)))) items))

(defn add-profile [items]
  (map (fn [item] (assoc item :self (profile-path (:author item)))) items))

(defn decorate [items]
  (->> items
    (add-avatar)
    (add-profile)))
