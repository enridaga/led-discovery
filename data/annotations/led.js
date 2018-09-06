$(document).ready(function(){
	$('led-start').nextUntil('led-end').css("background-color","#afeeee");	

	var ledIds = $('led-start').map(function(i, element){
		return $(element).attr('led-id');
	});
	// console.log("ids",ledIds);
	var panel = $("<div id=\"left-panel\"></div>");
	var leds = $("<ul id='leds'></ul>");
	panel.append(leds);
	$("led-start").map(function(i, element){
		$(element).attr("id", $(element).attr("led-id"));
	});
	$("body").prepend(panel);
	$.each(ledIds, function(i)
	{
	    var li = $('<li/>')
	        .addClass('led-menu-item')
	        .attr('role', 'menuitem')
	        .appendTo(leds);
	    var aaa = $('<a/>')
	        .addClass('led-item')
			.attr("href", "#"+ledIds[i])
	        .text(ledIds[i])
	        .appendTo(li);
			li.append("&nbsp;")
	    // var link = $('<a/>')
 // 	        .addClass('led-item')
 // 			.attr("href", "http://data.open.ac.uk/page/led/lexp/"+ledIds[i])
 // 			.attr("target", "_blank")
 // 	        .text("[LINK]")
 // 	        .appendTo(li);
	});
});