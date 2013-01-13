(ns linkification
  (:use clojure.test
        [statuses.views.common :only [linkify]]))

(deftest linkify-basic []
  (is (= (linkify "lorem ipsum") "lorem ipsum")))

(deftest linkify-uris []
  (is (=
      (linkify "lorem http://example.org ipsum")
      "lorem <a href='http://example.org'>http://example.org</a> ipsum"))
  (is (=
      (linkify "http://example.org lipsum")
      "<a href='http://example.org'>http://example.org</a> lipsum"))
  (is (=
      (linkify "lipsum http://example.org")
      "lipsum <a href='http://example.org'>http://example.org</a>")))

(deftest linkify-hashtags []
  (is (=
      (linkify "lorem #hashtag ipsum")
      "lorem #<a href='/statuses/updates?query=%23hashtag'>hashtag</a> ipsum"))
  (is (=
      (linkify "#hashtag lipsum")
      "#<a href='/statuses/updates?query=%23hashtag'>hashtag</a> lipsum"))
  (is (=
      (linkify "lipsum #hashtag")
      "lipsum #<a href='/statuses/updates?query=%23hashtag'>hashtag</a>")))
