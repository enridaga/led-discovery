
	<div class="row">
        <div class="col-lg-8 col-md-10 mx-auto">
        <p>Type a URL pointing to a plain text file:</p>
		<form method="GET" name="discover" id="urlForm" action="${findlerBasePath}/url" class="form-inline row">
			<div class="form-group controls col-md-10">
				<input style="width:100%" type="text" name="url" class="form-control" placeholder="URL" id="url" required data-validation-required-message="Please enter a URL.">
            </div>
            <div class="form-group col-md-2">
				<button type="submit" class="btn btn-primary" id="sendMessageButton">Discover!</button>
            </div>
		</form>
		</div>
	</div>
	<div class="row">
			#if(!$demo.isEmpty())
            <div class="col-lg-8 col-md-10 mx-auto">
            	<p>Try:</p>
            	<ul>
            	#foreach( $u in $demo.entrySet() )
    				<li class="demo-try-url"><a href="javascript:void(0)" data-url="$u.getKey()">$u.getValue()</a></li>
  				#end
            	</ul>
            </div>
            #end
	</div>
<script>
document.addEventListener("DOMContentLoaded", function(event) { 
	$(document).ready(function(){
		$(".demo-try-url a").click(function(){
			$("#url").val($(this).data("url"));
			$("#sendMessageButton").click();
		});
	});
});
</script>	


