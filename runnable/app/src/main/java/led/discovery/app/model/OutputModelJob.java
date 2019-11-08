package led.discovery.app.model;

import org.apache.stanbol.commons.jobs.api.Job;

public abstract class OutputModelJob implements Job {

	@Override
	public abstract OutputModelJobResult call() throws Exception;

}
