/*jslint vars: true, white: true */
/*global jQuery */

(function($) {

"use strict";

// character count
$("input[name=text]").charCount(140);

// quick reply
$(".updates").on("click", ".post .content", function(ev) {
    var post = $(this).closest(".post");
    if(post.find(".new-reply").length) {
        $(".new-reply input[name=text]", post).focus();
    } else {
        var postURI = $(".meta .time a", post).attr("href"); // XXX: bad selector (due to awkward markup?)
        $('<div class="new-reply" />').appendTo(post).
                load(postURI + " .update + form"); // XXX: bad selector? -- XXX: introduces duplicate IDs
    }
});

}(jQuery));
