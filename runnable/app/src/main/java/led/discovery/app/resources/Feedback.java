package led.discovery.app.resources;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.apache.commons.text.translate.CsvTranslators.CsvEscaper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import led.discovery.app.Application;

@Path("feedback")
public class Feedback {
	@Context
	protected HttpHeaders requestHeaders;

	@Context
	protected UriInfo requestUri;

	@Context
	protected ServletContext context;

	String feedbackFile;
	private static Logger L = LoggerFactory.getLogger(Feedback.class);

	public Feedback() {
		String pattern = "yyyy-MM-dd";
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
		String date = simpleDateFormat.format(new Date());
		feedbackFile = "/feedback" + date + ".csv";
		L.info("Feedback file: {}", feedbackFile);
	}

	@GET
	public Response htmlGET() {
		L.info("HIT GET");
		return Response.ok("OK!").build();
	}

	private final static Object lock = new Object();

	@POST
	@Consumes("application/x-www-form-urlencoded")
	@Produces("application/json")
	public Response POST(@Context HttpServletRequest request, @FormParam("url") String url,
			@FormParam("text") String text, @FormParam("from") int from, @FormParam("to") int to,
			@FormParam("rating") int rating, @FormParam("th") double th) {
		try {
			String sessionId = request.getSession().getId();
			L.debug("[{}] {} - {}:{} {}", new Object[] { sessionId, url, from, to, rating });
			String dataDir = (String) context.getAttribute(Application.DATA_DIR);

			String pattern = "-yyyy-MM-dd";
			SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
			String date = simpleDateFormat.format(new Date());
			StringBuilder sb = new StringBuilder();
			sb.append(sessionId);
			sb.append(',');
			sb.append(new CsvEscaper().translate(url));
			sb.append(',');
			sb.append(Double.toString(th));
			sb.append(',');
			sb.append(Integer.toString(from));
			sb.append(':');
			sb.append(Integer.toString(to));
			sb.append(',');
			sb.append(Integer.toString(rating));
			sb.append(',');
			sb.append(new CsvEscaper().translate(text));
			sb.append('\n');
			synchronized (lock) {
				FileWriter fw = new FileWriter(new File(dataDir, feedbackFile), true);
				fw.write(sb.toString());
				fw.close();
			}
			L.debug("Written to {}", feedbackFile);
			return Response.ok().build();
		} catch (IOException e) {
			L.error("Problem registering feedback", e);
			throw new WebApplicationException("Problem with input url", e, 500);
		}
	}
}
