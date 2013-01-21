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

(deftest linkify-uris-with-fragment-identifier []
  (is (=
      (linkify "lorem http://example.org#anchor ipsum")
      "lorem <a href='http://example.org#anchor'>http://example.org#anchor</a> ipsum")))
  (is (=
      (linkify "#hashtag lipsum http://example.org#anchor-name")
      "#<a href='/statuses/updates?query=%23hashtag'>hashtag</a> lipsum <a href='http://example.org#anchor-name'>http://example.org#anchor-name</a>"))
  (is (=
      (linkify "lipsum http://example.org#anchor-name #hashtag")
      "lipsum <a href='http://example.org#anchor-name'>http://example.org#anchor-name</a> #<a href='/statuses/updates?query=%23hashtag'>hashtag</a>"))
