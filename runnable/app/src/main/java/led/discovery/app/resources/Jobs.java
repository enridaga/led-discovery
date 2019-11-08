package led.discovery.app.resources;

import java.util.concurrent.ExecutionException;

import javax.servlet.ServletContext;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.apache.http.HttpHeaders;
import org.apache.stanbol.commons.jobs.api.JobManager;
import org.apache.velocity.VelocityContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import led.discovery.app.model.OutputModelJobResult;

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
	
	@GET
	@Path("{jobId}/status")
	@Produces("application/json")
	public Response jsonGETstatus(@PathParam("jobId") String jobId) {
		JobManager jm = getJobManager();
		if(jm.hasJob(jobId)) {
			return Response.status(200).entity("{\"status\": \"200\", \"message\": \"Job found\", \"status\": \"" + (jm.ping(jobId).isDone() ? "ready" : "running") + "\"}").build();
		}else {
			return Response.status(404).entity("{\"status\": \"404\", \"message\": \"Job not found\"}").build();
		}
		
	}
	
	@GET
	@Path("{jobId}/output")
	@Produces("application/json")
	public Response jsonGET(@PathParam("jobId") String jobId) {
		JobManager jm = getJobManager();
		if(jm.hasJob(jobId) && jm.ping(jobId).isDone()) {
			OutputModelJobResult r;
			try {
				r = (OutputModelJobResult) jm.ping(jobId).get();
				return Response.status(200).entity(r.getOutputModel().streamJSON()).build();
			} catch (InterruptedException | ExecutionException e) {
				L.error("", e);
				throw new WebApplicationException(e);
			}
		}else {
			return Response.status(404).entity("{\"status\": \"404\", \"message\": \"Job not found\"}").build();
		}
		
	}
	
	protected VelocityContext getVelocityContext() {
		VelocityContext vc = super.getVelocityContext();
		vc.put("body", getTemplate("/discover/jobs.tpl"));
		return vc;
	}
}
