(ns statuses.backend.core
  (:require [clj-time.local :refer [local-now]]
            [statuses.configuration :refer [config]]))

(defn empty-db []
  ;; creates an empty in-memory DB for usage with the functions in this namespace
  {:next-id 0        ;; the next id that will be used for values with identity
   :posts {}         ;; the actual status updates, keyed by id
   :timeline ()      ;; ids of status updates in order of posting
   :conversations {} ;; conversations, each as an ordered vector, keyed by id
   })

(defn valid-status-length? [n]
  (let [entry-config (config :entry)]
    (<= (:min-length entry-config) n (:max-length entry-config))))

(defn get-update
  "returns update with id"
  [db id]
  (get-in db [:posts id]))


(defn get-filtered-by
  "Retrieve the latest n status updates, starting with offset,
   restricted by pred, ordered by time"
  ([db limit offset pred]
     (let [posts (map #(get (:posts db) %) (:timeline db))]
           (take limit (drop offset (filter pred posts))))))

(defn no-replies?
  [db {:keys [id]}]
  (not-any? #(= id (:in-reply-to (val %))) (get-in db [:posts])))

(defn can-delete?
  [db user {author :author :as post}]
  (and (= user author) (no-replies? db post)))

(defn label-updates
  "Assocs a keyword with the result of a predicate for each update"
  [kw pred items]
  (map #(assoc % kw (pred %)) items))

(defn get-latest
  "Retrieve the latest n status updates, starting with offset,
   optionally restricted to author, containing query, ordered by time"
  [db limit offset author query]
  (get-filtered-by db limit offset
                   (fn [item]
                     (and
                      (or (nil? author) (.equalsIgnoreCase author (:author item)))
                      (or (nil? query) (not= (.indexOf (:text item) query) -1))))))

(defn- add-conversation
  "Returns db with conversation added. Safe to call with reply-to = nil."
  [db id reply-to]
  (if-let [r (get-update db reply-to)]
    (let [conv-id (or (:conversation r) (:next-id db))
          conv    (or (get-in db [:conversations conv-id]) [reply-to])]
      (-> db
          (assoc :next-id (inc (:next-id db)))
          (assoc-in [:conversations conv-id] (conj conv id))
          (assoc-in [:posts id :conversation] conv-id)
          (assoc-in [:posts id :in-reply-to] reply-to)
          (assoc-in [:posts reply-to :conversation] conv-id)))
    db))

(defn add-update
  "Returns db with new item added. Optionally handles replies & conversations."
  ([db author text]
     (let [id (:next-id db)]
       (-> db
           (assoc-in [:posts id] {:id id
                                  :time (local-now)
                                  :author author
                                  :text text})
           (assoc :timeline (cons id (:timeline db)))
           (assoc :next-id (inc id)))))
  ([db author text reply-to]
     (let [id (:next-id db)]
       (-> db
           (add-update author text)
           (add-conversation id reply-to)))))

(defn get-conversation
  "Returns posts participating in conversation with id."
  [db id]
  (map #(get (:posts db) %) (get-in db [:conversations id])))

(defn get-count
  "Returns the number of posts in db."
  [db]
  (count (:posts db)))

(defn remove-update
  "Returns db with the update with id removed"
  [db id]
  (letfn [(rm-single [db id]
            (-> db
              (assoc :posts (dissoc (:posts db) id))
              (assoc :timeline (remove #(= id %) (:timeline db)))))
          (update-conv [db id conv-id]
            (let [conv (get-in db [:conversations conv-id])
                  new-conv (pop conv)
                  conv-remains? (seq (pop new-conv))]
              (if conv-remains?
                (assoc-in db [:conversations conv-id] new-conv)
                (let [last-status (peek new-conv)]
                  (-> db
                    (update-in [:conversations] dissoc conv-id)
                    (update-in [:posts last-status] dissoc :conversation))))))]
    (if-let [conv-id (get-in db [:posts id :conversation])]
      (-> db
        (update-conv id conv-id)
        (rm-single id))
      (rm-single db id))))

(defn add-testdata
  "Create a DB with a set of n test updates"
  [db n]
  (letfn [
    (random [col] (nth col (rand-int (count col))))
    (text [] (random
              ["We know some foreign languages, too, so let's throw in an English text."
               "Gerade ein ganz tolles Programm in Clojure geschrieben, beeindruckend"
               "Hoffentlich sieht das hier nie irgend jemand."
               "Irgendwann wird auch mal jemand verstehen, was Phillip hier meint."
               "Was ist das? Sieht aus, als hätte das ein krankes Pferd geschrieben."
               "Ich mag Lorem Ipsum nicht und bevorzuge selbst gemachten Schwachsinn"
               "Eigentlich sollten das hier 10 sein, aber nach 7 verließ mich die Lust"]))
    (author [] (random ["abc" "xyz" "uiui" "klaus" "hein" "peer"]))]
    (reduce (fn [db [author text]] (add-update db author text))
            db
            (repeatedly n #(list (author) (text))))))

