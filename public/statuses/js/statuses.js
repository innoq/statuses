/*jslint vars: true, white: true */
/*global jQuery */

(function($) {

"use strict";

var imgRegEx = /!<a href="(http[^\s]+)">.+<\/a>/gi;
var markdownImgRegEx = /!\[(.+)\]\(<a href="(http[^\s]+)">.+<\/a>\)/gi;
var entryFormButton = $(".entry-form button");

$("#entry-text").charCount(140, entryFormButton);

// quick reply
$(".updates").on("click", ".btn-reply", function(ev) {
    $("div.new-reply").remove();
    var post = $(this).closest(".post");
    var postURI = $("a.permalink", post).attr("href");
    $('<div />').addClass("new-reply").appendTo(post).
        load(postURI + " form.reply-form", // XXX: introduces duplicate IDs
            function(response, status, xhr) {
                var input = $(".new-reply input[name=reply-text]", post);
                var button = $(".new-reply button", post);
                input.charCount(140, button);
                focusField(input);
            }
        );
});

$(".post-content").each(function(i, node) {
    var contentField = $(node);
    var currentText = contentField.html();
    currentText = currentText.replace(markdownImgRegEx, function(match, p1, p2, offset, string) {
        return toHtml($('<img style="max-width: 100%" style="max-width: 100%"/>').attr("src", p2).attr("alt", p1));
    });

    currentText = currentText.replace(imgRegEx, function(match, p1, offset, string) {
        return toHtml($('<img alt="image" style="max-width: 100%"/>').attr("src", p1));
    });
    if (currentText !== contentField.text()) {
        contentField.html(currentText);
    }
});

function focusField(field) {
    field.bind("focus", moveCursorToEOL); // XXX: no need for separate event handler?
    field.focus();
    field.unbind("focus", moveCursorToEOL);
}

// move cursor to the end -- XXX: crude!?
function moveCursorToEOL() {
    var val = this.value;
    this.value = "";
    this.value = val;
}

function toHtml(anyObject) {
    // workaround: Use a <div> as wrapper because jQuery's html() only returns the inner html. *sigh*
    // See http://jquery-howto.blogspot.de/2009/02/how-to-get-full-html-string-including.html
    return $("<div/>").append(anyObject).html();
}

}(jQuery));
