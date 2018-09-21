package led.discovery.app.resources;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

import javax.servlet.ServletContext;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import javax.ws.rs.core.UriInfo;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.exception.VelocityException;
import org.jsoup.Jsoup;
import org.jsoup.safety.Whitelist;
//import org.apache.velocity.Template;
//import org.apache.velocity.VelocityContext;
//import org.apache.velocity.app.VelocityEngine;
//import org.apache.velocity.exception.VelocityException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.hash.Hashing;

import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import led.discovery.app.Application;
import led.discovery.app.model.OutputModel;

@Path("")
public class Discover {
	@Context
	protected HttpHeaders requestHeaders;

	@Context
	protected UriInfo requestUri;

	@Context
	protected ServletContext context;

	private static Logger L = LoggerFactory.getLogger(Discover.class);

	@GET
	@Produces("text/html")
	public Response htmlGET(@QueryParam("url") String url) {
		try {
			VelocityContext vcontext = new VelocityContext();
			vcontext.put("StringEscapeUtils", StringEscapeUtils.class);
			VelocityEngine engine = (VelocityEngine) context.getAttribute(Application.VELOCITY);
			Template template = null;
			L.debug("velocity engine: {}", engine);
			try {
				if (url == null) {
					// Input URL
					vcontext.put("body", "discover/input-url.tpl");
				} else {
					L.debug("processing url: {}", url);
					OutputModel model = find(url);
					vcontext.put("source", url);
					vcontext.put("found", model.numberOfLEFound());
					vcontext.put("blocks", model.blocks());
					vcontext.put("body", "discover/show.tpl");
				}
				StringWriter sw = new StringWriter();
				template = engine.getTemplate("layout/main.tpl");
				template.merge(vcontext, sw);
				return Response.ok(sw.toString()).build();
			} catch (IOException e) {
				L.error("Problem with input url", e);
				throw new WebApplicationException("Problem with input url", e, 500);
			} catch (VelocityException mie) {
				L.error("", mie);
				mie.printStackTrace();
				throw new WebApplicationException("Problem with template engine", mie, 500);
			}
		} catch (Exception ee) {
			L.error("Internal error", ee);
			return Response.status(500).entity(ExceptionUtils.getStackTrace(ee)).build();
		}
	}

	@GET
	@Produces("application/json")
	public Response jsonGET(@QueryParam("url") String url) {

		if (url == null) {
			return Response.status(400).entity("Missing query parameter: url").build();
		} else {
			L.debug("processing url: {}", url);
			try {
				OutputModel model = find(url);
				StreamingOutput stream = model.streamJSON();
				return Response.ok(stream).header("Content-type", "application/json; charset=utf8").build();
			} catch (IOException e) {
				L.error("Problem with input url", e);
				throw new WebApplicationException(500);
			}
		}
	}

	@POST
	@Consumes("text/plain")
	public Response doPOST() {
		return Response.ok("POST").build();
	}

	public OutputModel find(String url) throws IOException {
		URLConnection u = new URL(url).openConnection();
		String type = u.getHeaderField("Content-Type");
		String text = IOUtils.toString(u.getInputStream(), StandardCharsets.UTF_8);
		// leave basic html tags
		if (text.indexOf("<html") > 0 && text.indexOf("<body") > 0) {
			text = Jsoup.clean(text, Whitelist.basicWithImages());
		}

		String hash = Hashing.sha256().hashString(text, StandardCharsets.UTF_8).toString();

		String nocache = requestUri.getQueryParameters().getFirst("nocache");
		String recache = requestUri.getQueryParameters().getFirst("recache");
		// If exists in cache reload it
		File cacheDir = new File((String) context.getAttribute(Application.CACHE_DIR));
		File cacheFile = new File(cacheDir, hash);
		OutputModel model;
		if (cacheFile.exists() && nocache == null && recache == null) {
			L.info("Reading from cache: {}", cacheFile);
			model = OutputModel.fromJSON(IOUtils.toString(new FileInputStream(cacheFile), StandardCharsets.UTF_8));
		} else {
			L.info("Computing annotations");
			final Annotation annotation = find(text, getMethodProperties("MusicEmbeddings"));
			model = OutputModel.build(annotation);
			model.setMetadata("source", url);
			model.setMetadata("bytes", Integer.toString(text.getBytes().length));
			model.setMetadata("type", type);
			model.setMetadata("hash", hash);
			if (nocache == null) {
				cacheFile.createNewFile();
				FileOutputStream fos = new FileOutputStream(cacheFile, false);
				OutputModel.writeAsJSON(model, fos);
				L.info("Written to cache: {}", cacheFile);
			}
		}
		return model;
	}

	public Annotation find(String text, Properties method) {
		StanfordCoreNLP pipeline = new StanfordCoreNLP(method);
		Annotation annotation = new Annotation(text);
		pipeline.annotate(annotation);
		return annotation;
	}

	public Properties getMethodProperties(String method) throws IOException {
		File properties = new File((String) context.getAttribute(Application.DATA_DIR), method + ".properties");
		if (properties.exists()) {
			Properties props = new Properties();
			props.load(new FileInputStream(properties));
			StringWriter writer = new StringWriter();
			props.list(new PrintWriter(writer));
			L.debug("Properties: \n{}", writer.getBuffer().toString());
			return props;
		} else {
			throw new IOException("Method does not exists");
		}
	}
}
