
	<div class="row">
        <div class="col-lg-8 col-md-10 mx-auto">
        <form method="GET" name="discover" id="urlForm" action="${findlerBasePath}/url" class="form-inline row">
			<div class="form-group controls col-md-10">
				<input style="width:100%" type="hidden" name="url" class="form-control" id="url">
            </div>
            <!-- <div class="form-group col-md-2" style="">
				<button type="submit" class="btn btn-primary" id="sendMessageButton">Discover!</button>
            </div> -->
		</form>
		</div>
	</div>
	<div class="row">
			#if(!$demo.isEmpty())
            <div class="col-lg-8 col-md-10 mx-auto">
            	<p>Select one of the following books:</p>
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
			$("#urlForm").submit();
		});
	});
});
</script>	