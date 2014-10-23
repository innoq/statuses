/*jslint eqeqeq: true, browser: true */
/*global jQuery */

/**
* charCount
* -------------
*
* Author: Tom Coote
* Web site: tomcoote.co.uk
* jQuery version: 1.6.1
* Plugin version: 1.0
* Licence: MIT
*
* Example: $(textarea).charCount(100);
*/
(function($) {
	$.fn.charCount = function(limit, button) {
		return this.each(function() {
			var countBox = $('<span></span>'),
				textArea = $(this),
				textAreaPosition = textArea.position();
				
			countBox.addClass('input-group-addon').attr('title', 'Number of characters left.').html(limit).insertBefore(textArea);
			
			var countChars = function() {
				var count = limit - textArea.val().length;
				if (count < 0) {
					//textArea.val(textArea.val().substr(0, limit));
					//count = 0;
					button.prop("disabled", true);
					countBox.addClass('limitReached');
				}
				else {
					button.prop("disabled", false);
					countBox.removeClass('limitReached');
				}
				
				countBox.html(count);
			};
			countChars();
			textArea.keyup(countChars).change(countChars);
		});
	};
}(jQuery));
