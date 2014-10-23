/*jslint vars: true, white: true */
/*global jQuery */

(function($) {

"use strict";

var imgRegEx = /!(http[^\s]+)/gi;
var markdownImgRegEx = /!\[(.+)\]\((http[^\s]+)\)/gi;

$("#text").charCount(140);

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

$('.post-content').each(function() {
    var contentField = $(this);
    var currentText = contentField.text();
    currentText.replace(markdownImgRegEx, function(match, p1, p2, offset, string) {
        $('<img />').attr("src", p2).attr("alt", p1).insertAfter(contentField);
    });

    currentText.replace(imgRegEx, function(match, p1, offset, string) {
        $('<img alt="image" />').attr("src", p1).insertAfter(contentField);
    });
});

// set cursor to the end of the inserted content
function focusField(field) {
    field.focus(function() {
        var val = this.value;
        this.value = '';
        this.value = val;
    });
    field.focus();
}

}(jQuery));
