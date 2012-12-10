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

(function($){$.fn.charCount=function(limit){return this.each(function(){var countBox=$('<div></div>'),textArea=$(this),textAreaPosition=textArea.position();countBox.addClass('charCountBox').html(limit).insertAfter(textArea);countBox.css({'top':textAreaPosition.top-(countBox.outerHeight()/2.5),'left':textAreaPosition.left+textArea.outerWidth()-(countBox.outerWidth()/1.5),'width':countBox.width(),'height':countBox.height()}).attr('title','Number of characters left.');var countChars=function(){var count=limit-textArea.val().length;if(count<0){textArea.val(textArea.val().substr(0,limit));count=0;countBox.addClass('limitReached');}
else{countBox.removeClass('limitReached');}
countBox.html(count);};countChars();textArea.keyup(countChars).change(countChars);});};}(jQuery));