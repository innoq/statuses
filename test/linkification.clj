(ns linkification
  (:use clojure.test
        [statuses.views.common :only [linkify]]))

(deftest linkify-basic
  (is (= (linkify "lorem ipsum") "lorem ipsum")))

(deftest linkify-uris
  (is (=
      (linkify "lorem http://example.org ipsum")
      "lorem <a href=\"http://example.org\">http://example.org</a> ipsum"))
  (is (=
      (linkify "http://example.org lipsum")
      "<a href=\"http://example.org\">http://example.org</a> lipsum"))
  (is (=
      (linkify "lipsum http://example.org")
      "lipsum <a href=\"http://example.org\">http://example.org</a>")))

(deftest linkify-hashtags
  (is (=
      (linkify "lorem #hashtag ipsum")
      "lorem #<a href=\"/statuses/updates?query=%23hashtag\">hashtag</a> ipsum"))
  (is (=
      (linkify "#hashtag lipsum")
      "#<a href=\"/statuses/updates?query=%23hashtag\">hashtag</a> lipsum"))
  (is (=
      (linkify "lipsum #hashtag")
      "lipsum #<a href=\"/statuses/updates?query=%23hashtag\">hashtag</a>"))
  (is (=
      (linkify "#WTF^2")
      "#<a href=\"/statuses/updates?query=%23WTF%5E2\">WTF^2</a>")))

(deftest linkify-uris-with-fragment-identifier
  (is (=
      (linkify "lorem http://example.org#anchor ipsum")
      "lorem <a href=\"http://example.org#anchor\">http://example.org#anchor</a> ipsum")))
  (is (=
      (linkify "#hashtag lipsum http://example.org#anchor-name")
      "#<a href=\"/statuses/updates?query=%23hashtag\">hashtag</a> lipsum <a href=\"http://example.org#anchor-name\">http://example.org#anchor-name</a>"))
  (is (=
      (linkify "lipsum http://example.org#anchor-name #hashtag")
      "lipsum <a href=\"http://example.org#anchor-name\">http://example.org#anchor-name</a> #<a href=\"/statuses/updates?query=%23hashtag\">hashtag</a>"))

(deftest linkify-email-addresses
  (is (=
      (linkify "hello foo@example.org how are you")
      "hello <a href=\"mailto:foo@example.org\">foo@example.org</a> how are you")))

(deftest linkify-mentions
  (is (=
      (linkify "@foo")
      "@<a href=\"/statuses/updates?author=foo\">foo</a>"))
  (is (=
      (linkify "@foo how are you")
      "@<a href=\"/statuses/updates?author=foo\">foo</a> how are you"))
  (is (=
      (linkify "how are you @foo")
      "how are you @<a href=\"/statuses/updates?author=foo\">foo</a>"))
  (is (=
      (linkify "nice to see @foo again")
      "nice to see @<a href=\"/statuses/updates?author=foo\">foo</a> again"))
  (is (=
      (linkify "@?")
      "@?"))
  (is (=
      (linkify "@foo: test")
      "@<a href=\"/statuses/updates?author=foo\">foo</a>: test"))
  (is (=
      (linkify "@foo.bar test")
      "@<a href=\"/statuses/updates?author=foo\">foo</a>.bar test")))
