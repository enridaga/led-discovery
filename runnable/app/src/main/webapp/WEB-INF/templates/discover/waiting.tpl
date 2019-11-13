<article class="led-waiting">
	<div class="container">
		<div class="row">
			#if( ! $error )
			<!-- <div class="col-lg-8 col-md-10 mx-auto">FindLEr is analysing the text... waiting for job $jobId</div> -->
			<div class="col-lg-12 col-md-12 col-sm-12 col-xs-12 waiting-monitor"><svg width="54px" height="54px" aria-hidden="true" focusable="false" data-prefix="fas" data-icon="spinner" role="img" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 512 512" class="svg-inline--fa fa-spinner fa-w-16 fa-spin fa-lg"><path fill="currentColor" d="M304 48c0 26.51-21.49 48-48 48s-48-21.49-48-48 21.49-48 48-48 48 21.49 48 48zm-48 368c-26.51 0-48 21.49-48 48s21.49 48 48 48 48-21.49 48-48-21.49-48-48-48zm208-208c-26.51 0-48 21.49-48 48s21.49 48 48 48 48-21.49 48-48-21.49-48-48-48zM96 256c0-26.51-21.49-48-48-48S0 229.49 0 256s21.49 48 48 48 48-21.49 48-48zm12.922 99.078c-26.51 0-48 21.49-48 48s21.49 48 48 48 48-21.49 48-48c0-26.509-21.491-48-48-48zm294.156 0c-26.51 0-48 21.49-48 48s21.49 48 48 48 48-21.49 48-48c0-26.509-21.49-48-48-48zM108.922 60.922c-26.51 0-48 21.49-48 48s21.49 48 48 48 48-21.49 48-48-21.491-48-48-48z" class=""></path></svg> <span class="waiting-info"></span></div>
			#else
			<div class="col-lg-12 col-md-12 col-sm-12 col-xs-12 waiting-monitor">Something went wrong :(</div>
			#end
		</div>
	</div>
</article>
