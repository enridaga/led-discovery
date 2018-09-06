package led.discovery.annotator;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.stanford.nlp.ling.CoreAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreAnnotations.LemmaAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.PartOfSpeechAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.Annotator;
import edu.stanford.nlp.util.ArraySet;
import edu.stanford.nlp.util.CoreMap;
import led.discovery.nlp.Term;

public class LedComponentsAnnotator implements Annotator {
	private Logger log = LoggerFactory.getLogger(LedComponentsAnnotator.class);
	private Map<String, Map<String, Double>> dictionaries;
	public static final List<String> componentNames = Arrays.asList(new String[] { "LedEventAnnotation",
			"LedSoundAnnotation", "LedPerformerAnnotation", "LedListenerAnnotation", "LedSentimentAnnotation" });
	public final static Map<String, Class<? extends CoreAnnotation<Double>>> annotations = new HashMap<String, Class<? extends CoreAnnotation<Double>>>();
	static {
		annotations.put("LedEventAnnotation", LedEventAnnotation.class);
		annotations.put("LedSoundAnnotation", LedSoundAnnotation.class);
		annotations.put("LedPerformerAnnotation", LedPerformerAnnotation.class);
		annotations.put("LedListenerAnnotation", LedListenerAnnotation.class);
		annotations.put("LedSentimentAnnotation", LedSentimentAnnotation.class);
	}

	public LedComponentsAnnotator(String name, Properties propes) {
		// Load musical dictionary
		dictionaries = new HashMap<String, Map<String, Double>>();
		CSVFormat format = CSVFormat.DEFAULT;
		String dictionarySources = propes.getProperty("custom.led.components");
		log.info("Dictionaries sources: {}", dictionarySources);
		// For each component
		String[] components = dictionarySources.split(",");
		for (int x = 0; x < components.length; x += 2) {
			String componentName = components[x];
			if (!componentNames.contains(componentName))
				throw new RuntimeException("Unsupported component name: " + componentName);
			String dictionarySource = components[x + 1];
			Map<String, Double> dictionary = new HashMap<String, Double>();
			try (Reader reader = new FileReader(new File(dictionarySource));) {
				log.info("Using dictionary {} at location {}", componentName, dictionarySource);
				try (CSVParser parser = new CSVParser(reader, format)) {
					Iterator<CSVRecord> records = parser.iterator();
					while (records.hasNext()) {
						CSVRecord record = records.next();
						try {
							Double val = Double.valueOf(record.get(1));
							dictionary.put(record.get(0), val);
						} catch (Exception ex) {
							log.warn("Corrupted record: {}", record);
						}
					}
				} catch (IOException e) {
					log.error("cannot load dictionary", e);
				}
			} catch (IOException e) {
				log.warn("Cannot find dictionary at location {}", dictionarySource);
				throw new RuntimeException(e);
			}
			dictionaries.put(componentName, dictionary);
		}
	}

	public final class LedEventAnnotation implements CoreAnnotation<Double> {
		@Override
		public Class<Double> getType() {
			return Double.class;
		}
	}

	public final class LedSoundAnnotation implements CoreAnnotation<Double> {
		@Override
		public Class<Double> getType() {
			return Double.class;
		}
	}

	public final class LedPerformerAnnotation implements CoreAnnotation<Double> {
		@Override
		public Class<Double> getType() {
			return Double.class;
		}
	}

	public final class LedListenerAnnotation implements CoreAnnotation<Double> {
		@Override
		public Class<Double> getType() {
			return Double.class;
		}
	}

	public final class LedSentimentAnnotation implements CoreAnnotation<Double> {
		@Override
		public Class<Double> getType() {
			return Double.class;
		}
	}

	@Override
	public void annotate(Annotation annotation) {
		List<CoreMap> sentences = annotation.get(SentencesAnnotation.class);
		for (CoreMap sentence : sentences) {
			StringBuilder tsb = null; // For trace logs
			for (CoreLabel token : sentence.get(CoreAnnotations.TokensAnnotation.class)) {
				String lemma = token.getString(LemmaAnnotation.class);
				String pos = token.getString(PartOfSpeechAnnotation.class);
				Term term = Term.build(lemma, pos);
				for (String compo : componentNames) {
					Double tfidf = dictionaries.get(compo).get(term.toAString().toLowerCase());
					token.set(annotations.get(compo), tfidf);
					// Below only for trace logs
					if (tfidf != null && log.isTraceEnabled()) {
						if (tsb == null) {
							tsb = new StringBuilder();
						}
						tsb.append(compo).append("/").append(term.toAString().toLowerCase()).append("/").append(tfidf).append(" ");
					}
				}
			}
			if (log.isTraceEnabled() && tsb != null) {
				log.trace("{}", tsb.toString());
				tsb = null;
			}
		}
	}

	@Override
	public Set<Class<? extends CoreAnnotation>> requirementsSatisfied() {
		return Collections.unmodifiableSet(new ArraySet<>(LedEventAnnotation.class, LedPerformerAnnotation.class,
				LedSoundAnnotation.class, LedListenerAnnotation.class, LedSentimentAnnotation.class));
	}

	@Override
	public Set<Class<? extends CoreAnnotation>> requires() {
		return Collections.unmodifiableSet(new ArraySet<>());
	}

}
