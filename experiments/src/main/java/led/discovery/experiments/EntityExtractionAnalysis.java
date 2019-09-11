package led.discovery.experiments;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;
import led.discovery.annotator.DBpediaSpotlightAnnotator.DBpediaEntityAnnotation;
import led.discovery.annotator.DBpediaSpotlightAnnotator.EntityLabel;
import led.discovery.benchmark.Bookmark;
import led.discovery.benchmark.ExcerptFinder;

public class EntityExtractionAnalysis {
	private static final Logger L = LoggerFactory.getLogger(EntityExtractionAnalysis.class);
	private File dataDir;
	private File sourcesDir;
	private File spotlightDir;
	private File input;
//	private List<String> experiments;
	private File output;
	private Properties spotlightP;

	public EntityExtractionAnalysis(String[] args) throws IOException {
		dataDir = new File(args[0]);
		sourcesDir = new File(dataDir, "sources");
		input = new File(dataDir, "input.csv");
		output = new File(dataDir, "analysis.csv");
		spotlightP = new Properties();
		spotlightP.load(new FileReader(new File(dataDir, "eeanalysis.properties")));
	}

	public void run() throws IOException {

		// Iterate over csv and download TXT version of sources from archive.org
		try (CSVParser reading = new CSVParser(new FileReader(input), CSVFormat.DEFAULT);
				FileWriter fw = new FileWriter(output, true)) {

			//
			Iterator<CSVRecord> iter = reading.getRecords().iterator();
			boolean skip = true;
			Map<String, Source> sources = new HashMap<String, Source>();
			Map<String, Excerpt> excerpts = new HashMap<String, Excerpt>();
			Map<String, Entities> sourcesEntities = new HashMap<String, Entities>();
			Map<String, Entities> excerptEntities = new HashMap<String, Entities>();

			while (iter.hasNext()) {
				CSVRecord r = iter.next();
				if (skip) {
					skip = false;
					continue;
				}
				String excerptKey = r.get(0).substring(r.get(0).lastIndexOf('/') + 1);
				String source = r.get(1);
				String excerpt = r.get(2);
				L.info("LE: {}", excerptKey);
				L.info("Source: {}", source);
				L.info("Excerpt: {}", excerpt.substring(0, excerpt.length() > 50 ? 50 : excerpt.length()));
				try {
					// Load the source
					String sourceKey = extractArchiveId(source);
					if (!sources.containsKey(sourceKey)) {
						Source s = new Source(source);
						sources.put(sourceKey, s);
					}

					// Load the excerpt
					if (!excerpts.containsKey(excerptKey)) {
						Excerpt e = new Excerpt(excerptKey, sources.get(sourceKey), excerpt);
						excerpts.put(excerptKey, e);
					}

					L.info("Bookmark at: {}", excerpts.get(excerptKey).getBookmark()[0]);

					// Load source entities
					if (!sourcesEntities.containsKey(sourceKey)) {
						Entities entities = new Entities(sourceKey, sources.get(sourceKey).getContent());
						sourcesEntities.put(sourceKey, entities);
					}

					L.info("Source entities: {}", sourcesEntities.get(sourceKey).entities.size());
					// Load excerpt entities
					if (!excerptEntities.containsKey(excerptKey)) {
						Entities entities = new Entities(excerptKey, excerpt);
						excerptEntities.put(sourceKey, entities);
					}

					L.info("Excerpt entities: {}", excerptEntities.get(excerptKey).entities.size());
				} catch (Exception e) {
					L.warn(" - Skipping! ({})", e.getClass().toString());
					e.printStackTrace();
					continue;
				}
			}
		}
	}

	private Map<String, List<Integer[]>> spotlight(String text) {
		Annotation annotation = new Annotation(text);
		StanfordCoreNLP pipeline = new StanfordCoreNLP(spotlightP);
		pipeline.annotate(annotation);

		Map<String, List<Integer[]>> map = new HashMap<String, List<Integer[]>>();
		List<CoreMap> sentences = annotation.get(SentencesAnnotation.class);
		for (CoreMap sentence : sentences) {
			int sentenceOffset = sentence.get(CoreAnnotations.TokensAnnotation.class).get(0).beginPosition();
			List<EntityLabel> entityLabel = sentence.get(DBpediaEntityAnnotation.class);
			for (EntityLabel l : entityLabel) {
				Integer[] position = new Integer[] { l.beginPosition() + sentenceOffset,
						l.endPosition() + sentenceOffset };
				if (!map.containsKey(l.getUri())) {
					map.put(l.getUri(), new ArrayList<Integer[]>());
				}
				List<Integer[]> pp = map.get(l.getUri());
				pp.add(position);
			}
		}
		return map;
	}

	private void _clean() throws IOException {
		output.delete();
		output.createNewFile();
	}

	public static final void main(String[] args) throws IOException {
		new EntityExtractionAnalysis(args).run();
	}

	public static String extractArchiveId(URI u) throws Exception {
		String[] p = u.getPath().split("/");
		String name = "";
		if (p[2].equals("items")) {
			name = p[3];
		} else {
			name = p[2];
		}
		return name;
	}

	public static String extractArchiveId(String u) throws Exception {
		return extractArchiveId(new URI(u));
	}

	public static String toTxtArchiveLink(String s) throws Exception {
		try {
			URI u = new URI(s);

			StringBuilder sb = new StringBuilder();
			sb.append("https://archive.org/download/");
			String name = extractArchiveId(u);
			sb.append(name);

			sb.append("/").append(name).append("_djvu.txt");
			String target = sb.toString();
			L.info(" - Link: {}", target);
			return target;
		} catch (URISyntaxException e) {
			L.error(" - Not a URI: ", e.getMessage());
			throw e;
		} catch (Exception e) {
			L.error(" - Unexpected: ", e.getMessage());
			throw e;
		}

	}

	public class Entities implements Serializable {
		private static final long serialVersionUID = 1L;
		private Map<String, List<Integer[]>> entities;
		private String key;
		private String text;

		Entities(String key, String text) throws IOException {
			this.key = key;
			this.text = text;
			init();
		}

		@SuppressWarnings("unchecked")
		public void init() throws IOException {

			try {
				File sourceFile = new File(sourcesDir, getFileName());
				if (!sourceFile.exists()) {
					L.debug("Compute entities and write to: {}", sourceFile);
					try {
						Map<String, List<Integer[]>> map = spotlight(text);
						FileOutputStream fileOutputStream = new FileOutputStream(sourceFile);
						ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);
						objectOutputStream.writeObject(map);
						objectOutputStream.flush();
						objectOutputStream.close();
					} catch (Exception e) {
						L.error(" - Can't spotlight: ", e);
						throw e;
					}
				}
				L.debug("Read entities from: {}", sourceFile);
				FileInputStream fileInputStream = new FileInputStream(sourceFile);
				ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);
				Object object = objectInputStream.readObject();
				objectInputStream.close();
				this.entities = (Map<String, List<Integer[]>>) object;
				L.debug(" - Entities legnth: {}", entities.size());
			} catch (Exception e) {
				throw new IOException(e);
			}

		}

		public String getFileName() throws Exception {
			try {
				StringBuilder sb = new StringBuilder();
				sb.append(this.key);
				sb.append(".entities");
				String target = sb.toString();
				L.debug(" - File: {}", target);
				return target;
			} catch (Exception e) {
				L.error(" - Unexpected: ", e.getMessage());
				throw e;
			}
		}

		public int size() {
			return entities.size();
		}

		public Map<String, List<Integer[]>> map() {
			return Collections.unmodifiableMap(entities);
		}
	}

	public class Source implements Serializable {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		private String originalUrl;
		private int hash;
		private String content;

		Source(String s) throws IOException {
			this.originalUrl = s;
			this.hash = new HashCodeBuilder().append(s).append(Source.class).toHashCode();
			this.init();
		}

		private void init() throws IOException {

			try {
				File sourceFile = new File(sourcesDir, getFileName());
				if (!sourceFile.exists()) {
					L.debug("Download source and write to: {}", sourceFile);
					try {
						String link = toTxtArchiveLink(getOriginalUrl());
						FileUtils.copyURLToFile(new URL(link), sourceFile, 10000, 10000);
					} catch (Exception e) {
						L.error(" - Can't download: ", e);
						throw e;
					}
				}
				L.debug("Read source from: {}", sourceFile);
				this.content = FileUtils.readFileToString(sourceFile, StandardCharsets.UTF_8);
				L.debug(" - Content legnth: {}", content.length());
			} catch (Exception e) {
				throw new IOException(e);
			}

		}

		@Override
		public boolean equals(Object obj) {
			if (obj instanceof Source && ((Source) obj).originalUrl.equals(this.originalUrl)) {
				return true;
			}
			return false;
		}

		@Override
		public int hashCode() {
			return this.hash;
		}

		public String getOriginalUrl() {
			return originalUrl;
		}

		public String getContent() {
			return this.content;
		}

		public String getFileName() throws Exception {
			try {
				URI u = new URI(this.originalUrl);
				StringBuilder sb = new StringBuilder();
				sb.append(extractArchiveId(u));
				sb.append(".txt");
				String target = sb.toString();
				L.debug(" - File: {}", target);
				return target;
			} catch (URISyntaxException e) {
				L.error(" - Not a URI: ", e.getMessage());
				throw e;
			} catch (Exception e) {
				L.error(" - Unexpected: ", e.getMessage());
				throw e;
			}
		}
	};

	public class Excerpt {

		private static final long serialVersionUID = 1L;

		private Source source;
		private Integer[] bookmark;
		private String excerpt;
		private String key;

		public Excerpt(String key, Source source, String excerpt) throws IOException {
			this.key = key;
			this.source = source;
			this.excerpt = excerpt;
			init();
			;
		}

		private void init() throws IOException {

			try {
				File sourceFile = new File(sourcesDir, getFileName());
				if (!sourceFile.exists()) {
					L.debug("Find excerpt and write to: {}", sourceFile);
					try {
						Bookmark bookmark = ExcerptFinder.find(excerpt, source.getContent());
						FileOutputStream fileOutputStream = new FileOutputStream(sourceFile);
						ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);
						objectOutputStream.writeObject(
								new Integer[] { bookmark.getFrom(), bookmark.getTo(), bookmark.getScore() });
						objectOutputStream.flush();
						objectOutputStream.close();
					} catch (Exception e) {
						L.error(" - Can't write excerpt file: ", e);
						throw e;
					}
				}
				L.debug("Read excerpt from: {}", sourceFile);
				FileInputStream fileInputStream = new FileInputStream(sourceFile);
				ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);
				Object object = objectInputStream.readObject();
				objectInputStream.close();
				this.bookmark = (Integer[]) object;
			} catch (Exception e) {
				throw new IOException(e);
			}
		}

		public String found() {
			return source.getContent().substring(getBookmark()[0], getBookmark()[1]);
		}

		public Integer[] getBookmark() {
			return bookmark;
		}

		public Source getSource() {
			return source;
		}

		public String toString() {

			return excerpt.substring(0, 200);
		}

		public String getFileName() throws Exception {
			try {
				StringBuilder sb = new StringBuilder();
				sb.append(key);
				sb.append(".excerpt");
				String target = sb.toString();
				L.debug(" - File: {}", target);
				return target;
			} catch (Exception e) {
				L.error(" - Unexpected: ", e.getMessage());
				throw e;
			}
		}
	}
}