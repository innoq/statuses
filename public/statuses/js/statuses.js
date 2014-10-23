/*jslint vars: true, white: true */
/*global jQuery */

(function($) {

"use strict";

$("#text").charCount(140);

function focusField(fieldName) {
    // set cursor to the end of the inserted content
    fieldName.focus(function() {
        var val = this.value;
        this.value = '';
        this.value = val;
    });
    fieldName.focus();
}
// quick reply
$(".updates").on("click", ".post-content", function(ev) {
    var post = $(this).closest(".post");
    if(post.find(".new-reply").length) {
        focusField($(".new-reply input[name=text]", post));
    } else {
        var postURI = $("a.permalink", post).attr("href");
        $('<div class="new-reply" />').appendTo(post)
            .load(postURI + " .update + form", function( response, status, xhr ) {
                    var input = $(".new-reply input[name=text]", post);
                    input.charCount(140);
                    focusField(input);
            }); // XXX: bad selector? -- XXX: introduces duplicate IDs
    }
});

var imgRegEx = /!(http[^\s]+)/gi;
var markdownImgRegEx = /!\[(.+)\]\((http[^\s]+)\)/gi;
$('.post-content').each(function() {
    var contentField = $(this);
    var currentText = contentField.text();
    currentText = currentText.replace(markdownImgRegEx, function(match, p1, p2, offset, string) {
        $('<img />').attr("src", p2).attr("alt", p1).insertAfter(contentField);
        if (p1 !== null) {
            return "";
        }
        return match;
    });
    // if (result != currentText) {
    //     currentText = result;
    // }

    currentText = currentText.replace(imgRegEx, function(match, p1, offset, string) {
        $('<img alt="image" />').attr("src", p1).insertAfter(contentField);
        if (p1 !== null) {
            return "";
        }
        return match;
    });
    if (currentText != contentField.text()) {
        contentField.text(currentText);
    }
});
}(jQuery));
