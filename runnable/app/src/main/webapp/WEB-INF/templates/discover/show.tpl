<!-- Post Content -->
<article class="led-show">
	<div class="container">
		<div class="row">
			<div class="col-lg-8 col-md-10 mx-auto">
				<!-- 
				#if($cached == "true")
				<p class="">Cached resource</p>
				#end
			 -->
				<p>Source:
					#if($source.indexOf("http:") == 0|| $source.indexOf("https:") == 0)
					<a href="$source">$sourceTitle</a> 
					#elseif($source.indexOf("findler:") == 0)
					<a href="$findlerBasePath/../$source.substring(8)">$sourceTitle</a> 
					#else
					<b>$sourceTitle</b>
					#end
				</p>
				<p>
					<strong>$found</strong> traces of listening experiences found.	
				</p>
				#if( $!userInputEnabled )
				<p><span>Skepticism: <span class="le-sensitivity-label">$sensitivity/100</span></span>
								- Increase to obtain less results.</p>
				
				<form class="form-inline justify-content-between sensitivity">
					<input type="hidden" name="id" value="$source" id="sourceId">
					<input type="hidden" name="th" value="$th" id="th">
					<div class="form-group">
						<div class="btn-group ">
							<button aria-label="Reset" type="button"
							disabled="disabled"
							title="Reset score"
							class="btn btn-warning le-sensitivity-reset">
							<strong><i class="fa fa-hashtag"></i></strong>
							</button>
							<button aria-label="Less" type="button"
								title="More results"
								class="btn btn-sm btn-warning le-sensitivity-less">
								<strong><i class="fa fa-caret-left"></i></strong>
							</button>
						</div>
						<div class="btn-group ">
						<input id="sensitivity" data-slider-id='sensitivitySlider'
							data-slider-handle="square" type="text"
							data-slider-tooltip="always"
							data-slider-value="$sensitivity" value="$sensitivity" />
						</div>
						<div class="btn-group btn-group">
							<button aria-label="More" type="button"
								title="Less results"
								class="btn btn-warning le-sensitivity-more">
								<strong><i class="fa fa-caret-right"></i></strong>
							</button>
							<button aria-label="Reload" type="submit" class="btn btn-primary le-sensitivity-update" disabled="disabled">
							<strong><i class="fa fa-sync"></i></strong>
						</button>
					</div>
				</div>
				</form>
				<p></p>
				#end
				<div class="form-group btn-group btn-group-toggle" data-toggle="buttons">
					<label class="btn btn-warning active" id="asList" data-toggle="tooltip" data-placement="top" title="Show as list"> <input type="radio"
						name="options" id="option2" autocomplete="off" checked value="list">
						As List
					</label> <label class="btn btn-warning " id="inText" data-toggle="tooltip" data-placement="top" title="Show in context"> <input type="radio"
						name="options" id="option1" autocomplete="off" 
						value="highlight"> In text
					</label> <label class="btn btn-dark jumpToFirst" data-toggle="tooltip" data-placement="top" title="Jump to the first result">first</label>
				</div>
				
				<!-- <div class="le-remember-actions">
				<a href="https://twitter.com/intent/tweet?text=FindLEr&url="><i class="fa fa-twitter"></i></a>
				</div> -->
				<hr />
				#set( $number = 0) #foreach( $block in $blocks ) #if($block.isLE())
				#set( $number = $number + 1) #end 
				#set( $score = $block.getMetadata("score"))
				<div class="block-$block.isLE()"
					id="le-$block.offsetStart()-$block.offsetEnd()"
					name="le-$block.offsetStart()-$block.offsetEnd()"
					data-score="$score">
					<div class="le-text">$StringEscapeUtils.escapeHtml($block.getText())</div>
					<div class="le-meta">
						<!-- [$block.offsetStart():$block.offsetEnd()] -->
						<div class="btn-toolbar justify-content-between" role="toolbar"
							aria-label="Toolbar" data-score="$score">
							<div class="input-group">
								<span class="badge badge-pill badge-warning le-counter">$number</span>
							</div>
							<!-- <div class="input-group"><span class="badge badge-pill badge-warning le-badge">Score: </span></div> -->
							<div class="input-group le-group-rating">
								<span class="badge badge-pill le-badge">Feedback:</span> <select
									class="le-rating"
									name="rating-$block.offsetStart()-$block.offsetEnd()"
									autocomplete="off" style="display: block;"
									data-from="$block.offsetStart()" data-to="$block.offsetEnd()"
									data-url="$source">
									<option value=""></option>
									<option value="1" data-html="<i class=&quot;fa fa-skull&quot; data-toggle=&quot;tooltip&quot; data-placement=&quot;top&quot; title=&quot;Definitely not&quot;></i>">1</option>
									<option value="2" data-html="<i class=&quot;fa fa-times&quot; data-toggle=&quot;tooltip&quot; data-placement=&quot;top&quot; title=&quot;Probably not&quot;></i>">2</option>
									<option value="3" data-html="<i class=&quot;fa fa-question&quot; data-toggle=&quot;tooltip&quot; data-placement=&quot;top&quot; title=&quot;Maybe&quot;></i>">3</option>
									<option value="4" data-html="<i class=&quot;fa fa-check&quot; data-toggle=&quot;tooltip&quot; data-placement=&quot;top&quot; title=&quot;Probably true&quot;></i>">4</option>
									<option value="5" data-html="<i class=&quot;fa fa-check-double&quot; data-toggle=&quot;tooltip&quot; data-placement=&quot;top&quot; title=&quot;Definitely true&quot;></i>">5</option>
								</select>
							</div>
							<div class="btn-group btn-group-sm" role="group"
								aria-label="group">
								<button type="button" class="btn btn-dark wrapUnwrap" data-toggle="tooltip" data-placement="top" title="Wrap/unwrap">
									<i class="fa fa-paragraph"></i>&nbsp;
								</button>
								<button type="button" class="btn btn-dark showInContext" data-toggle="tooltip" data-placement="top" title="Show in context">
									<i class="fa fa-stream"></i>&nbsp;
								</button>
								#if( $number > 1 )
								<button type="button" class="btn btn-warning jumpToPrevious" data-toggle="tooltip" data-placement="top" title="Jump to the previous result">
									<i class="fa fa-arrow-circle-o-up"></i>&nbsp;
								</button>
								#end #if( $number < $found )
								<button type="button" class="btn btn-warning jumpToNext" data-toggle="tooltip" data-placement="top" title="Jump to the next result">
									<i class="fa fa-arrow-circle-o-down"></i>&nbsp;
								</button>
								#end
								<button type="button" class="btn btn-dark jumpToTop" data-toggle="tooltip" data-placement="top" title="Jump to the top">
									<i class="fa fa-level-up"></i>&nbsp;
								</button>
							</div>
						</div>
					</div>
				</div>
				#end
			</div>
		</div>
	</div>
</article>
<div class="alert alert-success" id="feedback-success-alert" style="background-color: #FFF; border-color:#D39E00 ;border-width: 5px; width: 500px; position:fixed; left: 50%; margin-left:-250px; top:-100px;">
    <strong>Feedback recorded!</strong>
</div>
<div id="scaleJson" class="data-container">$sensitivityScale</div>
<script>
document.addEventListener("DOMContentLoaded", function(event) { 
	$(document).ready(function(){
		$("[name=options]").change(function(event, what){
			var action = $(event.target).val();
			if(action == 'list'){
				$("div.block-false").hide();
				$("button.showInContext i").removeClass("fa-list").addClass("fa-stream");
			}else{
				$("div.block-false").show();
				$("button.showInContext i").removeClass("fa-stream").addClass("fa-list");
			}
		});
		
		function jumpToMe(e){
			var targetLE = $(e.target).closest("div.block-true");
			if(!targetLE){
				return;
			}
			$([document.documentElement, document.body]).animate({
        		scrollTop: targetLE.offset().top-80
    		}, 1000);
		}
		
		$(".showInContext").click(function(e){
			if($("#inText").hasClass("active")){
				// In text
				$("#asList").click();
				$(".showInContext").attr("data-original-title","Show in context")
			}else{
				// As list
				$("#inText").click();
				$(".showInContext").attr("data-original-title","Show as list")
			}
			$(".showInContext").tooltip()
			jumpToMe(e);
		});
		
		$(".jumpToFirst").click(function(){
			var targetLE = $("div.block-true:first");
		 	$([document.documentElement, document.body]).animate({
        		scrollTop: targetLE.offset().top-80
    		}, 1000);
		});
		$(".wrapUnwrap").click(function(event){
			/* console.log(event); */
			if($(event.target).hasClass("btn-dark")){
				$(".le-text").addClass("le-text-unwrap");
				$(".wrapUnwrap").addClass("btn-light").removeClass("btn-dark");
			}else{
				$(".le-text").removeClass("le-text-unwrap");
				$(".wrapUnwrap").removeClass("btn-light").addClass("btn-dark");
			}
			jumpToMe(event);
		});
		$(".jumpToPrevious").click(function(event){
			var targetLE = $(event.target).closest("div.block-true").prevAll("div.block-true").first();
			if(!targetLE){
				return;
			}
			$([document.documentElement, document.body]).animate({
        		scrollTop: targetLE.offset().top-80
    		}, 1000);
		});
		$(".jumpToNext").click(function(event){
			var targetLE = $(event.target).closest("div.block-true").nextAll("div.block-true").first();
			if(!targetLE){
				return;
			}
			$([document.documentElement, document.body]).animate({
        		scrollTop: targetLE.offset().top-80
    		}, 1000);
		});
		$(".jumpToTop").click(function(event){
			$([document.documentElement, document.body]).animate({
        		scrollTop: 0
    		}, 1000);
		});
		
		$(function() {
		      $('.le-rating').each(function(){
		    	  var url = $(this).data("url");
				  var from = $(this).data("from");
				  var to = $(this).data("to");
				  var feedbackId = url+":"+from+":"+to;
				  /* console.log(feedbackId, url); */
		    	  if(window.localStorage &&  window.localStorage.getItem(feedbackId) ){
				    var rating = window.localStorage.getItem(feedbackId);
				   /*  console.log(feedbackId, rating); */
				    $(this).val(rating+"");
		    	  }
		      }).barrating('show', {
				  //theme: 'movie-rating',
				  theme: 'bars-square',
				  // theme: 'fontawesome-stars',
		            showValues: true,
		            showSelectedRating: false,
		            allowEmpty: true
		        });
		});
		
		var sensitivityScale = JSON.parse($("#scaleJson").html());
		var currentSensitivity = $("#sensitivity").val();
		var currentSensitivityLabel = $(".le-sensitivity-label").html();
		
		$("#feedback-success-alert").hide();
		$(".le-rating").change(function(event){
			
			/* var e = event || window.event;

		    var pageX = e.pageX;
		    var pageY = e.pageY;

		    // IE 8
		    if (pageX === undefined) {
		        pageX = e.clientX + document.body.scrollLeft + document.documentElement.scrollLeft;
		        pageY = e.clientY + document.body.scrollTop + document.documentElement.scrollTop;
		    } */
		    $("#feedback-success-alert").css("top", 200);
			
			var rating = $(event.target).val();
			var url = $(event.target).data("url");
			var from = $(event.target).data("from");
			var to = $(event.target).data("to");
			var text = $(event.target).closest("div.block-true").find(".le-text").html();
			var feedbackId = url+":"+from+":"+to;
			var jqxhr = jQuery.post( "/findler/feedback", { url: url, text: text, from: from, to: to, rating: rating, th: currentSensitivity } ).done(function() {
			    /* alert( "Thank you for your feedback" ); */
			    $("#feedback-success-alert").fadeTo(2000, 500).slideUp(500, function(){
			        $("#feedback-success-alert").fadeOut(500);
			    });
			    if(window.localStorage){
			    	window.localStorage.setItem(feedbackId,rating);
			    }
			  }).fail(function() {
			    alert( "Cannot submit feedback" );
			  }).always(function() {
			    /* alert( "finished" ); */
			  });
		});
		
		// Sensitivity slider
		function onChangeSensitivity(val){
			th = sensitivityScale[val];
			$("#th").val(th);
			var newSensitivityLabel = val+"/100";
			//console.log(currentSensitivityLabel, newSensitivityLabel);
			if(newSensitivityLabel!=currentSensitivityLabel){
				$(".le-sensitivity-label").addClass("le-sensitivity-modified");	
				$(".le-sensitivity-update").removeAttr("disabled");
				$(".le-sensitivity-reset").removeAttr("disabled");

			}else{
				$(".le-sensitivity-label").removeClass("le-sensitivity-modified");
				$(".le-sensitivity-update").attr("disabled","disabled");
				$(".le-sensitivity-reset").attr("disabled","disabled");
			}
			$(".le-sensitivity-label").html(newSensitivityLabel);
		}
		// With JQuery
		 $("#sensitivity").slider({
			min: 5,
			max: 100,
			step: 5,
			/* ticks: [5, 10, 15, 25, 30, 35, 40, 45, 50, 55, 60, 65, 70, 75, 80, 85, 90, 95, 100],
		    ticks_labels: [5, 10, 15, 25, 30, 35, 40, 45, 50, 55, 60, 65, 70, 75, 80, 85, 90, 95, 100],
		    ticks_snap_bounds: 30, */
			/* orientation: 'vertical', */
			tooltip_position:'top',
			tooltip: 'always'
		}).on("change", function(el){
			onChangeSensitivity(el.target.value);
			//$(this).trigger("slide");
		}).on("slide", function(slideEvt) {
			onChangeSensitivity(slideEvt.value);
		}); 

		$(".le-sensitivity-more").click(function(){
			var val = $("#sensitivity").val();
			/* console.log("more", val); */
			if(val >= 100) return;
			var value = parseInt(val) + 5;
			$("#sensitivity").slider("setValue", value);
			onChangeSensitivity(value);
		});
		$(".le-sensitivity-less").click(function(){
			var val = $("#sensitivity").val();
			/* console.log("less", val); */
			if(val <= 5) return;
			var value = parseInt(val) - 5;
			$("#sensitivity").slider("setValue", value);
			onChangeSensitivity(value);
		});
		$(".le-sensitivity-reset").click(function(){
			/* console.log("reset"); */
			$("#sensitivity").slider("setValue", currentSensitivity);
			onChangeSensitivity(currentSensitivity);
		});
		$(".le-group-rating").hover(function(){
			$(this).find('[data-toggle="tooltip"]').tooltip()
		});
		$('[data-toggle="tooltip"]').tooltip()
	});
});
   </script>