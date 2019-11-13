package led.discovery.app.resources;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.http.client.utils.URIBuilder;
import org.apache.stanbol.commons.jobs.api.JobManager;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.exception.VelocityException;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.jsoup.Jsoup;
import org.jsoup.safety.Whitelist;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

import led.discovery.app.Application;
import led.discovery.app.model.FileCache;
import led.discovery.app.model.Findler;
import led.discovery.app.model.FindlerManager;
import led.discovery.app.model.OutputModel;
import led.discovery.app.model.OutputModelJob;
import led.discovery.app.model.OutputModelJobResult;

@Path("")
public class Discover extends AbstractResource {
	private static Logger L = LoggerFactory.getLogger(Discover.class);

	private LinkedHashMap<Integer, Double> sensitivityScale = null;

	private static Map<String, String> _demoData = null;

	private Map<String, String> getDemoData(){
		if (_demoData == null) {
			_demoData = new HashMap<String, String>();
			// Check if demo URL exists
			// FIXME Change file name to collection.txt
			File demo = new File((String) context.getAttribute(Application.DATA_DIR), "demo.txt");
			if (demo.exists()) {
				L.debug("Demo file exists");
				String dco;
				try {
					dco = IOUtils.toString(new FileInputStream(demo), StandardCharsets.UTF_8);
					if (L.isDebugEnabled()) {
						L.debug("Demo: \n{}", dco);
					}
					for (String s : dco.split("\n")) {
						String[] tk = s.split("\\|");
						L.debug("{} {}", tk[1], tk[0]);
						_demoData.put(tk[1].trim(), tk[0]);
					}
				} catch (Exception e) {
					L.error("Cannot read demo url lists at " + demo, e);
				}
			} else {
				L.debug("Demo file does not exist: {}", demo);
			}
		}
		return Collections.unmodifiableMap(_demoData);
	}
	@GET
	@Produces("text/html")
	public Response htmlGET() {
		L.debug("GET htmlGETs");
		VelocityContext vcontext = getVelocityContext();
		
		vcontext.put("demo", getDemoData());
		String tmpl = "/discover/selectbook.tpl";
		if ((boolean) context.getAttribute(Application.USER_INPUT_ENABLED)) {
			tmpl = "/discover/input.tpl";
		}
		vcontext.put("body", getTemplate(tmpl));
		return Response.ok(getRenderer(vcontext).toString()).build();
	}

	@GET
	@Path("url")
	@Produces("text/html")
	public Response htmlGETurl(@QueryParam("url") String url) {
		L.debug("GET htmlGETurl");
		if (url == null) {
			return Response.status(HttpURLConnection.HTTP_BAD_REQUEST)
					.entity(errorPage("url parameter is mandatory").toString()).build();
		}
		try {
			saveUrl(url);
			return redirectToSource(url);
		} catch (Exception ee) {
			L.error("Internal error", ee);
			return Response.status(500).entity(ExceptionUtils.getStackTrace(ee)).build();
		}
	}

	@GET
	@Path("source")
	@Produces("text/html")
	public Response htmlGETsource(@QueryParam("id") String sourceId, @QueryParam("th") Double th, @QueryParam("method") String method) {
		L.debug("GET htmlGETsource");
		if (sourceId == null) {
			return Response.status(400).entity("Missing query parameter: url").build();
		}
		if(method == null) {
			method = getDefaultMethod();
		}
		boolean usecache = requestUri.getQueryParameters().getFirst("nocache") == null;
		boolean recache = requestUri.getQueryParameters().getFirst("recache") != null;
		try {
			// TODO move threshold to annotation metadata
			Properties properties = getMethodProperties(method);
			double defaultTh = Double.parseDouble(properties.getProperty("custom.led.heat.threshold"));
			if (th == null) {
				th = defaultTh;
			} else {
				properties.setProperty("custom.led.heat.threshold", Double.toString(th));
			}
			L.debug("Threshold: {}", th);
			
			final FindlerManager fm = buildManager(properties);
			String outputCacheId = fm.buildOutputModelCacheId(sourceId);
			L.debug("use cache: {}", usecache);
			L.debug("update cache: {}", recache);
			L.debug("sourceId: {}", sourceId);
			
			L.debug("output cache id: {}", outputCacheId);
			if (!recache && usecache && fm.hasOutputModelCachedBySourceId(sourceId)) {
				L.debug("Visualise result");
				OutputModel model = fm.fromCacheBySourceId(sourceId);
				VelocityContext vcontext = getVelocityContext();
				vcontext = prepareContext(model, defaultTh, th);
				vcontext.put("source", sourceId);
				vcontext.put("sourceTitle", getDemoData().containsKey(sourceId) ? getDemoData().get(sourceId) : sourceId);
				return Response.ok(getRenderer(vcontext).toString()).build();
			} else {
				L.debug("Output not ready");
				// TODO recache
				// If a job with that source is not running, start one.
				// Start a background job
				// Don't use cache = start a background job
				String jobId;
				// If a job is already running
				if(!outputHasJobId(outputCacheId)) {
					L.debug("Create a new Job");
					// Don't use cache = start a background job
					jobId = startJob(fm, sourceId, usecache || recache);
				} else {
					L.debug("Get info on existing Job");
					jobId = outputGetJobId(outputCacheId);
					L.debug("Job Id: {}", jobId);
				}
				
				// If Job is ready and output is not, then an error occurred!
				boolean error = false;
				L.debug("Job done: {}", getJobManager().ping(jobId).isDone());
				if(getJobManager().ping(jobId).isDone()) {
					
					if(!fm.hasOutputModelCachedBySourceId(sourceId)) {
						Object jr = (Object) getJobManager().ping(jobId).get();
						L.debug("ping(jobId).get() is of type {}", jr.getClass());
						
						// Some error occurred
						error = true;
					}
				}
				
				VelocityContext vcontext = getVelocityContext();
				vcontext.put("body", getTemplate("/discover/waiting.tpl"));
				vcontext.put("scripts", new String[] {getTemplate("/discover/waiting-script.tpl")});
				vcontext.put("source", sourceId);
				vcontext.put("sourceTitle", getDemoData().containsKey(sourceId) ? getDemoData().get(sourceId) : sourceId);
				vcontext.put("error", error);
				vcontext.put("jobId", jobId);
				vcontext.put("startedAt", getJobStartTime(jobId));
				return Response.ok(getRenderer(vcontext).toString()).build();
			}
		} catch (VelocityException mie) {
			L.error("Problem with template engine", mie);
			throw new WebApplicationException(mie, 500);
		} catch (Exception ee) {
			L.error("Internal error", ee);
			return Response.status(500).entity(ExceptionUtils.getStackTrace(ee)).build();
		} 
	}
	

	public FindlerManager buildManager(Properties properties) throws IOException {
		return new FindlerManager(new Findler(properties), getCache());
	}

	@GET
	@Path("source")
	@Produces("application/json")
	public Response jsonGETsource(@QueryParam("id") String sourceId, @QueryParam("th") Double th, @QueryParam("method") String method) {
		L.debug("GET jsonGETsource");
		if (sourceId == null) {
			return Response.status(400).entity("Missing query parameter: url").build();
		}
		if(method == null) {
			method = getDefaultMethod();
		}
		
		boolean usecache = requestUri.getQueryParameters().getFirst("nocache") == null;
		boolean recache = requestUri.getQueryParameters().getFirst("recache") != null;
		try {
			// TODO move threshold to annotation metadata
			Properties properties = getMethodProperties(method);
			double defaultTh = Double.parseDouble(properties.getProperty("custom.led.heat.threshold"));
			if (th == null) {
				th = defaultTh;
			} else {
				properties.setProperty("custom.led.heat.threshold", Double.toString(th));
			}
			L.debug("Threshold: {}", th);
			
			final FindlerManager fm = buildManager(properties);
			if (!recache && usecache && fm.hasOutputModelCachedBySourceId(sourceId)) {
				// Cleanup the Job Queue
				outputRemoveJobId(fm.buildOutputModelCacheId(sourceId));
				OutputModel model = fm.fromCacheBySourceId(sourceId);
				return Response.ok(model.streamJSON()).header("Content-type", "application/json; charset=utf8").build();
			}
			String jobId;
			// If a job is already running
			if(!outputHasJobId(fm.buildOutputModelCacheId(sourceId))) {
				// Don't use cache = start a background job
				jobId = startJob(fm, sourceId, usecache || recache);
			}else {
				jobId = outputGetJobId(fm.buildOutputModelCacheId(sourceId));
			}

			// This service returns 201 Created on success
			String location = requestUri.getBaseUri() + "/jobs/" + jobId;
			String info = new StringBuilder().append("{ \"message\":\"Job started.\",\n\"location\":\"")
					.append("\"Location\": \"").append(location).append("\"").toString();
			return Response.created(URI.create(location)).header("Content-type", "text/plain").entity(info).build();
		} catch (IOException e1) {
			L.error("", e1);
			throw new WebApplicationException(e1);
		}
	}

	private String startJob(FindlerManager fm, String sourceId, boolean cacheOutput) {
		L.debug("processing source: {}", sourceId);
		JobManager jm = getJobManager();
		String jobId = jm.execute(new OutputModelJob() {

			@Override
			public OutputModelJobResult call() throws Exception {

				try {
					String text = getCache().get(sourceId);
					OutputModel model = fm.find(sourceId, text, cacheOutput);
					return new OutputModelJobResult(model, "OK");
				} catch (IOException e) {
					L.error("Problem with input url", e);
					return new OutputModelJobResult(e.getMessage(), e);
				}
			}

			@Override
			public String buildResultLocation(String jobId) {
				return "jobs/" + jobId;
			}
		});
		outputSetJobId(fm.buildOutputModelCacheId(sourceId), jobId, new Date());
		L.debug("created job: {}", jobId);
		return jobId;
	}
	
	@POST
	@Path("file")
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	public Response upload(@FormDataParam("file") InputStream fileInputStream,
			@FormDataParam("file") FormDataContentDisposition disposition) {
		L.debug("POST upload");
		try {
			String extension = disposition.getType();
			L.debug("extension is {}", extension);
			if (extension.equals("txt")) {
				return error400("Please upload a file with extension .txt (was " + extension + ")");
			}
			String text = IOUtils.toString(fileInputStream, StandardCharsets.UTF_8);
			String name = disposition.getFileName();
			String size = Long.toString(disposition.getSize());
			L.debug("Processing {} of type {} and size {}", new Object[] { name, extension, size });
			String source = name + "." + extension + "." + size;
			saveFile(source, text);
			return redirectToSource(source);
		} catch (IOException e) {
			return Response.status(500).entity(errorPage("There was a problem reading the file", e).toString()).build();
		} catch (VelocityException mie) {
			L.error("Problem with template engine", mie);
			throw new WebApplicationException(mie, 500);
		} catch (URISyntaxException e) {
			throw new WebApplicationException(e, 500);
		}
	}

	public Response redirectToSource(String sourceId) throws URISyntaxException {
		return Response.seeOther(requestUri.resolve(new URIBuilder("source").addParameter("id", sourceId).build()))
				.build();
	}

	private void saveFile(String source, String text) throws IOException {
		if (text.indexOf("<html") > 0 && text.indexOf("<body") > 0) {
			text = Jsoup.clean(text, Whitelist.none());
		}
		getCache().put(text, source);
	}

	private void saveUrl(String url) throws IOException {
		String location;
		if(url.startsWith("findler:")) {
			L.debug("findler uri (before): {}", url);
			location = requestUri.getBaseUri().resolve("/" + url.substring(8)).toString();
			L.debug("findler uri (after): {}", location);
		} else {
			location = url;
		}
		URLConnection u = new URL(location).openConnection();
		// String type = u.getHeaderField("Content-Type");
		String text = IOUtils.toString(u.getInputStream(), StandardCharsets.UTF_8);

		// leave basic html tags
		if (text.indexOf("<html") > 0 && text.indexOf("<body") > 0) {
			text = Jsoup.clean(text, Whitelist.none());
		}

		getCache().put(text, url);
	}

	public FileCache getCache() {
		return (FileCache) context.getAttribute(Application.CACHE);
	}

	private VelocityContext prepareContext(OutputModel model, double defaultTh, double th) {
		VelocityContext vcontext = getVelocityContext();
		vcontext.put("found", model.numberOfLEFound());
		vcontext.put("blocks", model.blocks());
		vcontext.put("body", getTemplate("/discover/show.tpl"));
		vcontext.put("cached", model.getMetadata("cached"));
		vcontext.put("sensitivityScale", new Gson().toJson(getSensitivityScale(defaultTh)));
		vcontext.put("sensitivity", sensitivity(getSensitivityScale(defaultTh), th));
		vcontext.put("threshold", th);
		vcontext.put("maxScore", model.getMetadata("maxScore"));
		vcontext.put("minScore", model.getMetadata("minScore"));
		vcontext.put("th", model.getMetadata("th"));
		return vcontext;
	}

	private Properties getMethodProperties(String method) throws IOException {
		File properties = new File((String) context.getAttribute(Application.DATA_DIR), method + ".properties");
		if (properties.exists()) {
			Properties props = new Properties();
			props.load(new FileInputStream(properties));
			if (L.isDebugEnabled()) {
				StringWriter writer = new StringWriter();
				props.list(new PrintWriter(writer));
				L.debug("Properties: \n{}", writer.getBuffer().toString());
			}
			return props;
		} else {
			throw new IOException("Method does not exists");
		}
	}

	private final LinkedHashMap<Integer, Double> getSensitivityScale(double reset) throws NullPointerException {
		if (sensitivityScale == null) {
			double valMax = 1.0;
			double valMin = 0.01;
			sensitivityScale = new LinkedHashMap<Integer, Double>();
			for (int step = 5; step <= 100; step += 5) {
				sensitivityScale.put(step, sensitivityValue(step, 100, valMin, valMax, reset));
			}
		}
		return sensitivityScale;
	}

	public static final double sensitivityValue(double step, double steps, double minVal, double maxVal, double mean) {
		int middle = Math.round(Math.round(steps / 2));
		double factor;
		if (step == middle) {
			return mean;
		} else if (step == steps) {
			return maxVal;
		} else if (step > middle) {
//			return ((maxVal - minVal) * (step - 1) / (steps - 1)) + minVal;
			// Above mean
			double linear = ((maxVal - mean) * (step - middle - 1) / (middle - 1)) + mean;
			double linearStep = (maxVal - mean) / middle;
			factor = linearStep / (step - middle);
			L.trace("linear {} linearStep {} factor {}", new Object[] { linear, linearStep, factor });
			return linear - factor;
		} else if (step < middle && !(step < 1)) {
			// Below mean
			double linear = ((mean - minVal) * (step - 1) / (middle - 1)) + minVal;
			double linearStep = (mean - minVal) / middle;
			factor = linearStep / (middle - step);
			L.trace("linear {} linearStep {} factor {}", new Object[] { linear, linearStep, factor });
			return linear + factor;
		}
		return minVal;
	}

	public static final int sensitivity(LinkedHashMap<Integer, Double> scale, double th) {
		// Where this th is placed
		int last = 0;
		for (Entry<Integer, Double> en : scale.entrySet()) {
			if (th >= en.getValue()) {
				last = en.getKey();
			} else {
				break;
			}
		}
		L.trace("sensitivity: {} {}", th, last);
		return last;
	}
}
