package led.discovery.app.resources;

import javax.servlet.ServletContext;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.apache.http.HttpHeaders;
import org.apache.velocity.VelocityContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path("jobs")
public class Jobs extends AbstractResource{

	private static Logger L = LoggerFactory.getLogger(Jobs.class);

	@Context
	protected HttpHeaders requestHeaders;

	@Context
	protected UriInfo requestUri;

	@Context
	protected ServletContext context;

	@GET
	@Produces("text/html")
	public Response htmlGET() {
		L.debug("GET htmlGET");
		VelocityContext vc = getVelocityContext();
		vc.put("jobs", getJobManager().size());
		return ok(vc);
	}
	
	protected VelocityContext getVelocityContext() {
		VelocityContext vc = super.getVelocityContext();
		vc.put("body", getTemplate("/discover/jobs.tpl"));
		return vc;
	}
}
