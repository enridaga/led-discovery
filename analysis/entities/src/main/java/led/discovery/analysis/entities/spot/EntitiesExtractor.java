package led.discovery.analysis.entities.spot;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.Charset;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.jsoup.Jsoup;
import org.jsoup.safety.Whitelist;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * To extract entities
 * 
 * @author enridaga
 *
 */
public class EntitiesExtractor {
	private static final Logger log = LoggerFactory.getLogger(EntitiesExtractor.class);
	private SpotlightClient client;

	public EntitiesExtractor(String service) {
		this.client = new SpotlightClient(service);
	}

	private static final void printRow(File input, PrintStream ps, SpotlightAnnotation ann, char c) {
		ps.print(input.getName());
		ps.print(c);
		ps.print(ann.getUri());
		ps.print(c);
		ps.print(ann.getSimilarity());
		ps.println();
	}

	public void extract(String folder, String output) throws IOException {
		File inputDirectory = new File(folder);
		if(! (inputDirectory.exists() && inputDirectory.isDirectory())) {
			log.error("Not a directory or not exists: {}", folder);
			throw new IOException("cannot read from " + folder);
		}
		File outputFile = new File(output);
		if(outputFile.exists()) {
			outputFile.delete();
		}
		outputFile.createNewFile();
		
		PrintStream ps = new PrintStream(outputFile, "UTF-8");
		for (File f : inputDirectory.listFiles()) {
			try {
				String text = IOUtils.toString(new FileInputStream(f), Charset.forName("UTF8"));
				text = Jsoup.clean(text, Whitelist.simpleText());
				List<SpotlightAnnotation> annotations = client.perform(text, 0.4, 0).asList();
				for (SpotlightAnnotation ann : annotations) {
					printRow(f, ps, ann, ',');
				}
			} catch (IOException e) {
				log.error("", e);
			}
		}
	}
	
	public static final void main(String[] args) throws IOException {
		String server = args[0];
		String folder = args[1];
		String output = args[2];
		if(args.length > 3) {
			String blacklist = args[3];
		}
		new EntitiesExtractor(server).extract(folder, output);
	}
}
