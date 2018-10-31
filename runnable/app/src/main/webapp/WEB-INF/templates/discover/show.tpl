<!-- Post Content -->
<article class="led-show">
	<div class="container">
		<div class="row">
			<div class="col-lg-8 col-md-10 mx-auto">
				#if($cached == "true")
				<p class="">Cached resource</p>
				#end
				<p>
					Source: <a href="$url">$url</a>
				</p>
				<p>
					Found <strong>$found</strong> traces of listening experiences
				</p>
<!-- 				<pre>
Sensitivity: $th
Max score: $maxScore
Min score: $minScore
sensitivity: $sensitivity
				</pre>
 -->				

				<form class="">
					<input type="hidden" name="url" value="$url" id="url"> <input
						type="hidden" name="th" value="$th" id="th">
					<div class="form-group ">
						<label for="sensitivity">Sensitivity</label> <input
							id="sensitivity" 
							data-slider-id='sensitivitySlider'
							data-slider-handle="square" 
							type="text" 
							data-slider-value="$sensitivity" 
							value="$sensitivity"/>
					</div>
					<button type="submit" class="btn btn-primary ">update</button>
				</form>


				<div class="btn-group btn-group-toggle" data-toggle="buttons">
					<label class="btn btn-warning active"> <input type="radio"
						name="options" id="option1" autocomplete="off" checked
						value="highlight"> In text
					</label> <label class="btn btn-warning "> <input type="radio"
						name="options" id="option2" autocomplete="off" value="list">
						As List
					</label> <label class="btn btn-dark jumpToFirst">Jump to the first</label>
				</div>



				<hr />
				#set( $number = 0) #foreach( $block in $blocks ) #if($block.isLE())
				#set( $number = $number + 1) #end #set( $score =
				$block.getMetadata("score"))
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
									<option value="1">No</option>
									<option value="2">&gt;</option>
									<option value="3">?</option>
									<option value="4">&gt;</option>
									<option value="5">Yes</option>
								</select>
							</div>
							<div class="btn-group btn-group-sm" role="group"
								aria-label="First group">
								#if( $number > 1 )
								<button type="button" class="btn btn-warning jumpToPrevious">
									<i class="fa fa-arrow-circle-o-up"></i>&nbsp;
								</button>
								#end #if( $number < $found )
								<button type="button" class="btn btn-warning jumpToNext">
									<i class="fa fa-arrow-circle-o-down"></i>&nbsp;
								</button>
								#end
								<button type="button" class="btn btn-dark jumpToTop">
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
<div id="scaleJson" class="data-container">
$sensitivityScale
</div>
<script>
document.addEventListener("DOMContentLoaded", function(event) { 
	$(document).ready(function(){
		$("[name=options]").change(function(event, what){
			var action = $(event.target).val();
			if(action == 'list'){
				$("div.block-false").hide();
//				$("div.block-true div.le-meta").show();
			}else{
				$("div.block-false").show();
//				$("div.block-true div.le-meta").hide();
			}
		});
		
		$(".jumpToFirst").click(function(){
			var targetLE = $("div.block-true:first");
		 	$([document.documentElement, document.body]).animate({
        		scrollTop: targetLE.offset().top-80
    		}, 1000);
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
				  var feedbackId = url+":"+from+""+to;
				  /* console.log(feedbackId, url); */
		    	  if(window.localStorage &&  window.localStorage.getItem(feedbackId) ){
				    var rating = window.localStorage.getItem(feedbackId);
				   /*  console.log(feedbackId, rating); */
				    $(this).val(rating+"");
		    	  }
		      }).barrating('show', {
		    	    theme: 'bars-square',
		            showValues: true,
		            showSelectedRating: false,
		            allowEmpty: true
		        });
		});
		
		$(".le-rating").change(function(event){
			var rating = $(event.target).val();
			var url = $(event.target).data("url");
			var from = $(event.target).data("from");
			var to = $(event.target).data("to");
			var text = $(event.target).closest("div.block-true").find(".le-text").html();
			var feedbackId = url+":"+from+""+to;
			/* console.log("hit rating", feedbackId); */
			#set ( $d = "$")
			var jqxhr = ${d}.post( "findler/feedback", { url: url, text: text, from: from, to: to, rating: rating } ).done(function() {
			    alert( "Thank you for your feedback" );
			    if(window.localStorage){
			    	window.localStorage.setItem(feedbackId,rating);
			    }
			  }).fail(function() {
			    alert( "Cannot submit feedback" );
			  }).always(function() {
			    /* alert( "finished" ); */
			  });

 			
		});
		
		var sensitivityScale = JSON.parse($("#scaleJson").html());
		
		// Sensitivity slider
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
		}).on("slide", function(slideEvt) {
			var val = slideEvt.value;
			// Lookup sensitivity table
			th = sensitivityScale[val];
			/* console.log(val, th); */
			$("#th").val(th);
		}); 

	});
});
   </script>