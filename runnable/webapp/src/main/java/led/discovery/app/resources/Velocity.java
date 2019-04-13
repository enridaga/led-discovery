package led.discovery.app.resources;

import java.io.StringWriter;
import java.net.URI;

import javax.servlet.ServletContext;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import led.discovery.app.Application;

@Path("")
public class Velocity {

	@Context
	protected HttpHeaders requestHeaders;

	@Context
	protected UriInfo requestUri;

	@Context
	protected ServletContext context;

	private static Logger L = LoggerFactory.getLogger(Velocity.class);

	@GET
	public Response get() {
		VelocityContext vcontext = getVelocityContext();
		vcontext.put("source", "Ciao");
		return Response.ok(getRenderer(vcontext).toString()).build();
	}

	public VelocityContext getVelocityContext() {
		VelocityContext vcontext = new VelocityContext();
		vcontext.put("StringEscapeUtils", StringEscapeUtils.class);
		vcontext.put("findlerBasePath", "/findler");
		return vcontext;
	}

	public String getTemplate(String relativePath) {
		String base = (String) context.getAttribute(Application.TEMPLATES);
		return base + relativePath;	
	}
	
	public StringWriter getRenderer(VelocityContext vcontext) {
		StringWriter sw = new StringWriter();
		VelocityEngine engine = (VelocityEngine) context.getAttribute(Application.VELOCITY);
		Template template = null;
		template = engine.getTemplate(getTemplate("/layout/main.tpl"));
		template.merge(vcontext, sw);
		return sw;
	}

}
