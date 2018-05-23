package led.discovery.analysis.entities.spot;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SpotlightClient {
	private final static Logger log = LoggerFactory.getLogger(SpotlightClient.class);
	public static final double DEFAULT_CONFIDENCE = 0.4;
	public static final int DEFAULT_SUPPORT = 0;
	private String spotlight;

	public SpotlightClient(String spotlight) {
		this.spotlight = spotlight;
	}

	public SpotlightResponse perform(String string) throws IOException {
		return perform(string, DEFAULT_CONFIDENCE, DEFAULT_SUPPORT);
	}

	public SpotlightResponse perform(String text, double confidence, int support) throws IOException {
		long sss;
		// URL connection channel.
		StringBuilder stringBuilder;
		try {
			HttpURLConnection urlConn;
			String querystring = new StringBuilder().append("text=").append(URLEncoder.encode(text, "UTF-8")).append("&confidence=").append(confidence).append("&support=").append(support).toString();
			// 8192 bytes as max URL length
			boolean doPost = true; // XXX We always do a HTTP POST

			if (spotlight.getBytes().length + querystring.getBytes().length > 8192) {
				doPost = true;
			}
			log.info("{} {}", spotlight, text);
			if (doPost) {
				urlConn = (HttpURLConnection) new URL(spotlight).openConnection();
				// Let the run-time system (RTS) know that we want input.
				urlConn.setDoInput(true);
				// Let the RTS know that we want to do output.
				urlConn.setDoOutput(true);
				// No caching, we want the real thing.
				urlConn.setUseCaches(false);
				// Request method
				urlConn.setRequestMethod("POST");
				// Specify the content type.
				urlConn.setRequestProperty("Accept", "text/xml");
				// Send POST output.
				DataOutputStream printout = new DataOutputStream(urlConn.getOutputStream());

				printout.writeBytes(querystring);
				printout.flush();
				printout.close();
			} else {
				// Do GET
				urlConn = (HttpURLConnection) new URL(spotlight + '?' +
					querystring).openConnection();
				urlConn.setRequestProperty("Accept", "text/xml");
			}
			sss = System.currentTimeMillis();
			// Get response data.
			BufferedReader input = new BufferedReader(new InputStreamReader(urlConn.getInputStream()));
			sss = (System.currentTimeMillis() - sss);
			String line;
			String test = "";
			// FIXME Can't we simply ask HttpURLConnection to not return
			// headers?
			boolean httpHeader = true;
			stringBuilder = new StringBuilder();
			while ((line = input.readLine()) != null) {
				if (httpHeader) {
					if (line.length() > 5) {
						test = line.substring(0, 5);
					}
					if (test.equals("<?xml")) {
						httpHeader = false;
						stringBuilder.append(line);
					}
				} else {
					stringBuilder.append(line);
				}
			}

			input.close();
		} catch (IOException e) {
			throw e;
		}

		return new SpotlightResponse(stringBuilder.toString(), sss, confidence, support);
	}

	public static void main(String[] args) throws IOException {
		SpotlightClient client = new SpotlightClient(args[0]);
		List<SpotlightAnnotation> list = client.perform(args[1]).asList();
		for (SpotlightAnnotation sa : list) {
			System.err.println(sa.toString());
		}

	}
}
