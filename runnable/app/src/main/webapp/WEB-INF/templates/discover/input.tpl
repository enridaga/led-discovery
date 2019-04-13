<br/>
<div class="container">
<ul class="nav col-lg-8 col-md-10 mx-auto">
  <li class="nav-item">
    <a class=" btn btn-warning nav-link active le-input-selector" data-enables="inputURLForm" >Input URL</a>
  </li>
  <li class="nav-item">
    <a class=" btn btn-warning nav-link le-input-selector" data-enables="inputFileForm" >Upload file</a>
  </li>
</ul>
<div id="inputURLForm" class="le-input-area">
#parse($servlet.getTemplate('/discover/input-url.tpl'))
</div>
<div id="inputFileForm" class="le-input-area le-invisible">
#parse($servlet.getTemplate('/discover/input-file.tpl'))
</div>
<script>
document.addEventListener("DOMContentLoaded", function(event) { 
	$(document).ready(function(){
		$(".le-input-selector").click(function(){
			/*console.log("Clicked", this);*/
			var elem = $(this);
			var target = elem.data("enables");
			$(".le-input-area").hide();
			$("#" + target).show();
			/* console.log("target", target); */
			$(".le-input-selector").removeClass("active");
			elem.addClass("active");
		});
	});
});
</script>
</div>