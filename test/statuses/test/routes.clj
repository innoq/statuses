(ns statuses.test.routes
  (:require [clojure.test :refer [deftest is]]
            [statuses.routes :as sut]))

(deftest test-query-params
  (is (=
       (sut/query-params {})
       ""))
  (is (=
       (sut/query-params {:foo "bar"})
       "?foo=bar"))
  (is (=
       (sut/query-params {:foo "bar" :bar "foo"})
       "?foo=bar&bar=foo"))
  (is (=
       (sut/query-params {:foo "bar" :bar nil})
       "?foo=bar"))
  (is (=
       (sut/query-params {:foo nil :bar nil}))))

