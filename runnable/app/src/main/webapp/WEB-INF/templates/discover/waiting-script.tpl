<script>
$(document).ready(function(){
	
	function sleep(ms) {
		  return new Promise(resolve => setTimeout(resolve, ms));
	}
	
	function reportInfo(info){
		$(".waiting-info").html(info);
	}
	
	function timeAgo(time) {
	    moment.updateLocale('en', {
	        relativeTime: {
	            future: "in %s",
	            past: "%s ago",
	            s: number=>number + "s ago",
	            ss: '%ds ago',
	            m: "1m ago",
	            mm: "%dm ago",
	            h: "1h ago",
	            hh: "%dh ago",
	            d: "1d ago",
	            dd: "%dd ago",
	            M: "a month ago",
	            MM: "%d months ago",
	            y: "a year ago",
	            yy: "%d years ago"
	        }
	    });

	    let secondsElapsed = moment().diff(time, 'seconds');
	    let dayStart = moment("2018-01-01").startOf('day').seconds(secondsElapsed);

	    if (secondsElapsed > 300) {
	        return moment(time).fromNow(true);
	    } else if (secondsElapsed < 60) {
	        return dayStart.format('s') + 's ago';
	    } else {
	        return dayStart.format('m:ss') + 'm ago';
	    }
	}

	/* var m = moment(); */
	var m = new Date();
	var pingJob = async function(){
		
		$.getJSON('jobs/' + "$jobId" + "/status", async function(data){
			reportInfo("Processing $sourceTitle.<br>Started " + timeAgo(m));
			if(data.status == "ready"){
				reportInfo('Work is ready, <a href="javascript:window.location.reload();">click here to see the result</a>.');
				/* window.location.reload(); */
				return true;
			}
			await sleep(1000);
			pingJob();
			return false;
		});
	}
	
	pingJob();
	
});
</script>