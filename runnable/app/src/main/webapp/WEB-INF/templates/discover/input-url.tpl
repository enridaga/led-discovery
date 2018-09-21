
<div class="container">
	<div class="row">
        <div class="col-lg-8 col-md-10 mx-auto">
        <p>Type a URL pointing to a plain text file.</p>
		<form method="GET" name="discover" id="contactForm">
			<div class="control-group">
              <div class="form-group floating-label-form-group controls">
                <label>URL</label>
                <input type="text" name="url" class="form-control" placeholder="URL" id="url" required data-validation-required-message="Please enter a URL.">
                <p class="help-block text-danger"></p>
              </div>
            </div>
            <br/>
			<div id="success"></div>
            <div class="form-group">
              <button type="submit" class="btn btn-primary" id="sendMessageButton">Discover!</button>
            </div>
		</form>
		</div>
	</div>
</div>

