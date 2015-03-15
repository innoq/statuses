(ns statuses.views.test.json
  (:require [clojure.test :refer [deftest is]]
            [clj-time.core :refer [date-time]]
            [statuses.routes :refer [avatar-path profile-path update-path]]
            [statuses.views.json :refer [decorate]]))

(defn- item [can-delete? id time author text]
  {:can-delete? can-delete? :id id :time time :author author :text text})

(defn- test-item [item ref-item]

        (is (= (:id item)
               (:id ref-item))
            "id is unchanged")

        (is (= (:can-delete? item)
               (:can-delete? ref-item))
            "can-delete? matches")

        (is (= (:author  item)
               (:author ref-item))
            "author is unchanged")

        (is (= (:time item)
               (:time ref-item))
            "time is unchanged")

        (is (= (:text  item)
               (:text ref-item))
            "text is unchanged")

        (is (= (:self  item)
               (update-path (:id ref-item)))
            "self reference is set")

        (is (= (:avatar  item)
               (avatar-path (:author ref-item)))
            "avatar is set")

        (is (= (:profile  item)
               (profile-path (:author ref-item)))
            "profile is set"))

(deftest test-decorate
  (let [item1 (item false 42 (date-time 2015 2 20 0 0 0 0) "hein" "Irgendwann wird auch mal jemand verstehen, was Phillip hier meint.")
        item2 (item true 41 (date-time 2015 2 20 0 0 0 0) "hein" "We know some foreign languages, too, so let's throw in an English text.")
        item3 (item false 40 (date-time 2015 2 20 0 0 0 0) "uiui" "Hoffentlich sieht das hier nie irgend jemand.")

        items [item1 item2 item3]

        decorated (decorate items)]

        ;; could be more "functional" but for the sake of readability
        (test-item (first decorated) item1)
        (test-item (second decorated) item2)
        (test-item (nth decorated 2) item3)))
