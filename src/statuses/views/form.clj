(ns statuses.views.form
  (:require [hiccup.def :refer [defelem]]
            [hiccup.util :refer [to-uri]]))

(defelem form-to
  "Create a form that points to a particular method and route.
  e.g. (form-to [:put \"/post\"]
         ...)"
  [[method action] & body]
  (let [method-str (.toUpperCase (name method))
        action-uri (to-uri action)]
    (-> (if (contains? #{:get :post} method)
          [:form {:method method-str, :action action-uri}]
          [:form {:method "POST", :action action-uri}
            [:input {:type "hidden"
                     :name "_method"
                     :value method-str}]])
        (concat body)
        (vec))))
