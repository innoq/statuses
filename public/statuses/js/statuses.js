/*jslint vars: true, white: true */
/*global jQuery */

(function($) {

"use strict";

$("#text").charCount(140);

// quick reply
$(".updates").on("click", ".post-content", function(ev) {
    var post = $(this).closest(".post");
    if(post.find(".new-reply").length) {
        $(".new-reply input[name=text]", post).focus();
    } else {
        var postURI = $("a.permalink", post).attr("href");
        $('<div class="new-reply" />').appendTo(post)
            .load(postURI + " .update + form", function( response, status, xhr ) {
                    $(".new-reply input[name=text]", post).charCount(140);
            }); // XXX: bad selector? -- XXX: introduces duplicate IDs
    }
});
}(jQuery));
