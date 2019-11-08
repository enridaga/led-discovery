package led.discovery.app.model;

import org.apache.stanbol.commons.jobs.api.JobResult;

public class OutputModelJobResult implements JobResult {
	OutputModel model = null;
	Exception exception = null;
	String message;
	boolean success;

	public OutputModelJobResult(OutputModel model, String message) {
		this.model = model;
		this.message = message;
		this.success = true;
	}

	public OutputModelJobResult(String message, Exception exception) {
		this.model = null;
		this.message = message;
		this.success = false;
		this.exception = exception;
	}

	@Override
	public String getMessage() {
		return message;
	}

	@Override
	public boolean isSuccess() {
		return success;
	}

	public OutputModel getOutputModel() {
		return model;
	}
	
	public Exception getException() {
		return exception;
	}
}
