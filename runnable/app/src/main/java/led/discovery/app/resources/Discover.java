package led.discovery.app.resources;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
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

import org.apache.commons.collections.map.LinkedMap;
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

import com.google.gson.Gson;

import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.AnnotationPipeline;
import edu.stanford.nlp.pipeline.AnnotatorPool;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import led.discovery.app.Application;
import led.discovery.app.model.FileCache;
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
	public Response htmlGET(@QueryParam("url") String url, @QueryParam("th") Double th) {
		try {
			VelocityContext vcontext = new VelocityContext();
			vcontext.put("StringEscapeUtils", StringEscapeUtils.class);
			VelocityEngine engine = (VelocityEngine) context.getAttribute(Application.VELOCITY);
			Template template = null;
			L.debug("velocity engine: {}", engine);
			Properties properties = getMethodProperties("MusicEmbeddings");
			double defaultTh = Double.parseDouble(properties.getProperty("custom.led.heat.threshold"));
			if (th == null) {
				th = defaultTh;
			} else {
				properties.setProperty("custom.led.heat.threshold", Double.toString(th));
			}
			L.debug("Threshold: {}", th);
			L.debug("Property th: {}", properties.getProperty("custom.led.heat.threshold"));

			try {
				if (url == null) {
					// Input URL
					vcontext.put("body", "discover/input-url.tpl");
				} else {
					L.debug("processing url: {}", url);
					OutputModel model = findUrl(url, properties);
					vcontext.put("url", url);
					vcontext.put("found", model.numberOfLEFound());
					vcontext.put("blocks", model.blocks());
					vcontext.put("body", "discover/show.tpl");
					vcontext.put("cached", model.getMetadata("cached"));
					LinkedHashMap<Integer,Double> scale =sensitivityScale(Double.parseDouble(model.getMetadata("maxScore")), defaultTh);
					
					vcontext.put("sensitivityScale", new Gson().toJson(scale));
					vcontext.put("sensitivity", sensitivity(scale, th));
					vcontext.put("threshold", th);
					vcontext.put("maxScore", model.getMetadata("maxScore"));
					vcontext.put("minScore", model.getMetadata("minScore"));
					vcontext.put("th", model.getMetadata("th"));
				}
				StringWriter sw = new StringWriter();
				template = engine.getTemplate("layout/main.tpl");
				template.merge(vcontext, sw);
				return Response.ok(sw.toString()).build();
			} catch (IOException e) {
				L.error("Problem with input url", e);
				vcontext.put("body", "layout/error.tpl");
				vcontext.put("message", "There was a problem accessing the URL");
				vcontext.put("exception", e);
				StringWriter sw = new StringWriter();
				template = engine.getTemplate("layout/main.tpl");
				template.merge(vcontext, sw);
//				throw new WebApplicationException("Problem with input url", e, 500);
				return Response.status(500).entity(sw.toString()).build();
			} catch (VelocityException mie) {
				L.error("Problem with template engine", mie);
				throw new WebApplicationException(mie, 500);
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
				Properties properties = getMethodProperties("MusicEmbeddings");
				OutputModel model = findUrl(url, properties);
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

	private String downloadUrl(String url, boolean useCache, boolean forceWriteCache) throws IOException {
		if (getCache().contains(url) && useCache && !forceWriteCache) {
			L.info("Reading from cache: {}", url);
			return getCache().get(url);
		} else {
			URLConnection u = new URL(url).openConnection();
			String type = u.getHeaderField("Content-Type");
			String text = IOUtils.toString(u.getInputStream(), StandardCharsets.UTF_8);

			// leave basic html tags
			if (text.indexOf("<html") > 0 && text.indexOf("<body") > 0) {
				text = Jsoup.clean(text, Whitelist.basicWithImages());
			}
			if (useCache) {
				getCache().put(text, url);
			}
			return text;
		}
	}

	public FileCache getCache() {
		return (FileCache) context.getAttribute(Application.CACHE);
	}

	public OutputModel find(String source, String text, Properties properties, boolean usecache, boolean recache) throws IOException {
		StringWriter writer = new StringWriter();
		properties.list(new PrintWriter(writer));
		String th = properties.getProperty("custom.led.heat.threshold");
		String outputId = getCache().buildHash(source, th, writer.getBuffer().toString());

		// If exists in cache reload it
		OutputModel model;
		if (getCache().containsHash(outputId) && usecache && !recache) {
			L.info("Reading from cache: {}", outputId);
			model = OutputModel.fromJSON(getCache().getByHash(outputId));
			model.setMetadata("cached", "true");
		} else {
			L.info("Computing annotations");
			final Annotation annotation = annotate(text, properties);
			model = OutputModel.build(annotation);
			model.setMetadata("source", source);
			model.setMetadata("bytes", Integer.toString(text.getBytes().length));
			model.setMetadata("hash", outputId);
			model.setMetadata("th", properties.getProperty("custom.led.heat.threshold"));
			for (Object k : properties.keySet()) {
				model.setMetadata("property-" + k, properties.getProperty((String) k));
			}
			if (usecache) {
				L.info("Writing cache: {}", outputId);
				OutputStream fos = getCache().putStream(outputId);
				OutputModel.writeAsJSON(model, fos);
				L.trace("Written: {}", outputId);
			}
			model.setMetadata("cached", "false");
		}
		return model;
	}
	
	public OutputModel findUrl(String url, Properties properties) throws IOException {
		boolean usecache = requestUri.getQueryParameters().getFirst("nocache") == null;
		boolean recache = requestUri.getQueryParameters().getFirst("recache") != null;
		String text = downloadUrl(url, usecache, recache);
		OutputModel model = find(url, text, properties, usecache, recache);
		return model;
	}

	public Annotation annotate(String text, Properties method) {
		if(L.isDebugEnabled()) {
			StringWriter writer = new StringWriter();
			method.list(new PrintWriter(writer));
			L.debug("Annotate with properties: \n{}", writer.getBuffer().toString());
		}
		StanfordCoreNLP.clearAnnotatorPool();
		AnnotationPipeline pipeline = new StanfordCoreNLP(method);
		Annotation annotation = new Annotation(text);
		pipeline.annotate(annotation);
		return annotation;
	}

	public Properties getMethodProperties(String method) throws IOException {
		File properties = new File((String) context.getAttribute(Application.DATA_DIR), method + ".properties");
		if (properties.exists()) {
			Properties props = new Properties();
			props.load(new FileInputStream(properties));
			if(L.isDebugEnabled()) {
				StringWriter writer = new StringWriter();
				props.list(new PrintWriter(writer));
				L.debug("Properties: \n{}", writer.getBuffer().toString());
			}
			return props;
		} else {
			throw new IOException("Method does not exists");
		}
	}

	public static final LinkedHashMap<Integer, Double> sensitivityScale(double _max, double reset) throws NullPointerException {
		double valMax = _max; // 1.940708689499091;
		double valMin = 0.01;
		LinkedHashMap<Integer, Double> sensitivityScale = new LinkedHashMap<Integer,Double>();
		for (int step = 5; step <= 50; step += 5) {
			sensitivityScale.put(step, sensitivityValue(step, 100, valMin, reset));
		}
		for (int step = 50; step <= 100; step += 5) {
			sensitivityScale.put(step, sensitivityValue(step, 100, reset, valMax));
		}
		return sensitivityScale;
	}

	public static final double sensitivityValue(double step, double steps, double minVal, double maxVal) {
		return ((maxVal - minVal) * (step - 1) / (steps - 1)) + minVal;
	}
	
	public static final int sensitivity(LinkedHashMap<Integer, Double> scale, double th) {
		
		// Where this th is placed
		int last = 0;
		for(Entry<Integer,Double> en : scale.entrySet()) {
			if(th >= en.getValue()) {
				last = en.getKey();
			}else {
				break;
			}
		}
		L.debug("sensitivity: {} {}", th, last);
		return last;
	}
}
