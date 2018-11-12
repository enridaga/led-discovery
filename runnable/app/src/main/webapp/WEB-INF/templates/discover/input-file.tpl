	<div class="row">
		<div class="col-lg-8 col-md-10 mx-auto">
			<p>Upload a plain text file:</p>
			<form class="form-inline row" method="POST" name="discover"
				id="fileForm" action="${findlerBasePath}/file" enctype="multipart/form-data">
				<div class="input-group controls col-md-10">
					<input type="file" name="file">
				</div>
				<div class="form-group col-md-2">
					<button type="submit" class="btn btn-primary"
						id="sendMessageButton">Discover!</button>
				</div>
			</form>
		</div>
	</div>


