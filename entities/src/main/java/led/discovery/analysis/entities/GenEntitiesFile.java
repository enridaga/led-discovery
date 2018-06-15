package led.discovery.analysis.entities;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.safety.Whitelist;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import led.discovery.analysis.entities.spot.SpotlightAnnotation;
import led.discovery.analysis.entities.spot.SpotlightClient;
import led.discovery.analysis.entities.spot.SpotlightResponse;

/**
 * Generating Entities
 * 
 * @author enridaga
 *
 */
public class GenEntitiesFile {
	private File trainingFile;
	private File outputFile;
	private File cache;
	private SpotlightClient provider;
	private static final Logger L = LoggerFactory.getLogger(GenEntitiesFile.class);

	public GenEntitiesFile(String[] args) {
		this.trainingFile = new File(args[0]);
		this.outputFile = new File(args[1]);
		// provider = new SpotlightService(args[2]);
		this.provider = new SpotlightClient(args[2]);
		this.cache = new File(args[3]);
	}

	private void _clean() {
		if (outputFile.exists()) {
			outputFile.delete();
		}
		if (!cache.exists()) {
			cache.mkdirs();
		}
	}

	public void run() throws FileNotFoundException, IOException {
		_clean();

		L.info("Loading training file");
		Map<String, String> posMap = new HashMap<String, String>();
		Map<String, String> negMap = new HashMap<String, String>();
		int plen = 0;
		int nlen = 0;
		try (CSVParser reader = new CSVParser(new FileReader(trainingFile), CSVFormat.DEFAULT)) {
			Iterator<CSVRecord> it = reader.iterator();
			while (it.hasNext()) {
				CSVRecord r = it.next();
				String polarity = r.get(0);
				String fname = r.get(1);
				int tlen = Integer.parseInt(r.get(2));
				String content = StringEscapeUtils.unescapeCsv(r.get(3));
				Map<String, String> map;
				if ("1".equals(polarity)) {
					map = posMap;
					plen += tlen;
				} else {
					map = negMap;
					nlen += tlen;
				}
				String en = entities(fname, content);
				L.info("{} {}", fname, en.length());
				map.put(fname, en);
			}
		}
		L.info("{} positives examples loaded ({} avg length)", posMap.size(), plen);
		L.info("{} negative examples loaded ({} avg length)", negMap.size(), nlen);
		L.info("Writing {} positive entries", posMap.size());
		L.info("Writing {} negative entries", negMap.size());
		try (FileWriter fw = new FileWriter(outputFile, true)) {
			for (Entry<String, String> entry : posMap.entrySet()) {
				writeEntry(true, entry.getKey(), entry.getValue(), fw);
			}
			for (Entry<String, String> entry : negMap.entrySet()) {
				writeEntry(false, entry.getKey(), entry.getValue(), fw);
			}
		}
	}

	private boolean cacheContains(String fname) {
		try {
			if (FileUtils.directoryContains(cache, new File(fname))) {
				return true;
			}
		} catch (IOException e) {
			L.error("Cache error", e);
		}
		return false;
	}

	private String cacheGet(String fname) {
		String xml = "";
		try (FileInputStream is = new FileInputStream(fname);) {
			xml = IOUtils.toString(is, StandardCharsets.UTF_8);
		} catch (IOException e1) {
			L.error("Cache error", e1);
		}
		return xml;
	}

	private void cacheWrite(String fname, String xml) {
		File f = new File(cache, fname);
		if (f.exists()) {
			f.delete();
		}
		try (FileOutputStream os = new FileOutputStream(f);) {
			IOUtils.write(xml, os, StandardCharsets.UTF_8);
		} catch (IOException e1) {
			L.error("Cache error", e1);
		}
	}

	private String entities(String fname, String txt) {
		String xml = null;
		List<String> uris = new ArrayList<String>();
		if (cacheContains(fname)) {
			xml = cacheGet(fname);
		} else {
			String text = Jsoup.clean(txt, Whitelist.simpleText());
			try {
				SpotlightResponse r = provider.perform(text, 0.4, 0);
				xml = r.getXml().replaceAll("&[^\\s;]+[\\s;]", " ");
				L.trace("{}", xml);
			} catch (Exception io) {
				L.error("An error occurred", io);
			}
			cacheWrite(fname, xml);
		}
		List<SpotlightAnnotation> annotations = SpotlightResponse.asList(xml, 0.4);
		for (SpotlightAnnotation ann : annotations) {
			uris.add(ann.getUri());
		}
		L.trace("{} entities", uris.size());
		return StringUtils.join(uris, " ");
	}

	private void writeEntry(boolean positive, String name, String content, FileWriter w) throws IOException {
		w.write((positive) ? "1" : "0");
		w.write(",");
		w.write(name);
		w.write(",");
		w.write(Integer.toString(content.length()));
		w.write(",");
		w.write(StringEscapeUtils.escapeCsv(content.replaceAll("\\R+", " ")));
		w.write("\n");
	}

	public static final void main(String[] args) throws FileNotFoundException, IOException {
		new GenEntitiesFile(args).run();
	}
}
