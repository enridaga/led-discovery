package led.discovery.app.resources;

import java.io.StringWriter;

import javax.servlet.ServletContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.stanbol.commons.jobs.api.JobManager;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;

import led.discovery.app.Application;

public class AbstractResource {

	@Context
	protected HttpHeaders requestHeaders;
	@Context
	protected UriInfo requestUri;
	@Context
	protected ServletContext context;

	protected AbstractResource() {
		super();
	}

	protected Response error500(String message, Exception e) {
		return Response.status(500).entity(errorPage(message, e).toString()).build();
	}

	protected Response error400(String message) {
		return Response.status(400).entity(errorPage(message).toString()).build();
	}

	protected StringWriter errorPage(String message, Exception e) {
		VelocityContext vcontext = getVelocityContext();
		vcontext.put("body", getTemplate("/layout/error.tpl"));
		vcontext.put("message", message);
		vcontext.put("exception", e);
		return getRenderer(vcontext);
	}

	protected StringWriter errorPage(String message) {
		VelocityContext vcontext = getVelocityContext();
		vcontext.put("body", getTemplate("/layout/error.tpl"));
		vcontext.put("message", message);
		return getRenderer(vcontext);
	}

	public String getTemplate(String relativePath) {
		String base = (String) context.getAttribute(Application.TEMPLATES);
		return base + relativePath;
	}

	protected StringWriter getRenderer(VelocityContext vcontext) {
		StringWriter sw = new StringWriter();
		VelocityEngine engine = (VelocityEngine) context.getAttribute(Application.VELOCITY);
		Template template = null;
		template = engine.getTemplate(getTemplate("/layout/main.tpl"));
		template.merge(vcontext, sw);
		return sw;
	}

	protected VelocityContext getVelocityContext() {
		VelocityContext vcontext = new VelocityContext();
		vcontext.put("StringEscapeUtils", StringEscapeUtils.class);
		vcontext.put("findlerBasePath", "/findler");
		vcontext.put("servlet", this);
		vcontext.put("userInputEnabled", (boolean) context.getAttribute(Application.USER_INPUT_ENABLED));
		return vcontext;
	}

	protected JobManager getJobManager() {
		return (JobManager) context.getAttribute(Application.JOB_MANAGER);
	}
	
	protected Response ok(VelocityContext vc) {
		return Response.ok(getRenderer(vc).toString()).build();
	}
}