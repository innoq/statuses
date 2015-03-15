(ns statuses.views.json
  (:require [statuses.routes :refer [avatar-path profile-path update-path]]))

(defn add-avatar [items]
  (map #(assoc % :avatar (avatar-path (:author %))) items))

(defn add-profile [items]
  (map #(assoc % :profile (profile-path (:author %))) items))

(defn add-self [items]
  (map #(assoc % :self (update-path (:id %))) items))

(defn decorate [items]
  (->> items
    (add-avatar)
    (add-profile)
    (add-self)))
