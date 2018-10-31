package led.discovery.annotator.evaluators;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.semgraph.SemanticGraphCoreAnnotations.BasicDependenciesAnnotation;
import edu.stanford.nlp.trees.TypedDependency;
import edu.stanford.nlp.util.CoreMap;
import led.discovery.annotator.window.TextWindow;
import led.discovery.nlp.LemmaCleaner;
import led.discovery.nlp.Term;

public class SentiMusDepEvaluator extends AbstractTextWindowEvaluator {
	private HashMap<String, Double> sentiment;
	private HashMap<String, Double> music;
	private Logger log = LoggerFactory.getLogger(SentiMusDepEvaluator.class);
	private Double musicThreshold;
	private Double sentimentThreshold;

	private String[] DEFAULT_DEPS = new String[] { "nsubj", "dobj", "nsubjpass", "iobj" };
	private String[] deps;

	public SentiMusDepEvaluator(Properties properties, LemmaCleaner cleaner, Set<String> stopwords) {
		super(cleaner, stopwords);

		String _deps = properties.getProperty("custom.led.sentimus.deps");
		if (_deps == null) {
			deps = DEFAULT_DEPS;
		} else {
			deps = _deps.split(",");
		}
		String _musicThreshold = properties.getProperty("custom.led.sentimus.music.threshold");
		if (_musicThreshold == null) {
			throw new RuntimeException("Missing property: custom.led.sentimus.music.threshold");
		} else {
			musicThreshold = Double.valueOf(_musicThreshold);
		}

		String _sentimentThreshold = properties.getProperty("custom.led.sentimus.sentiment.threshold");
		if (_sentimentThreshold == null) {
			throw new RuntimeException("Missing property: custom.led.sentimus.sentiment.threshold");
		} else {
			sentimentThreshold = Double.valueOf(_sentimentThreshold);
		}
		music = new HashMap<String, Double>();
		String musicDictionarySource = properties.getProperty("custom.led.sentimus.music.dictionary");
		try (Reader reader = new FileReader(new File(musicDictionarySource))) {
			log.info("Using dictionary at location {}", musicDictionarySource);
			CSVFormat format = CSVFormat.DEFAULT;
			try (CSVParser parser = new CSVParser(reader, format)) {
				Iterator<CSVRecord> records = parser.iterator();
				while (records.hasNext()) {
					CSVRecord record = records.next();
					try {
						Double val = Double.valueOf(record.get(1));
						music.put(record.get(0), val);
					} catch (Exception ex) {
						log.warn("Corrupted record: {} ({})", record, ex.getMessage());
						// log.error("", ex);
					}
				}
			}
		} catch (IOException e) {
			log.warn("Cannot find dictionary at location {}", musicDictionarySource);
			throw new RuntimeException(e);
		}
		sentiment = new HashMap<String, Double>();
		String sentimentDictionarySource = properties.getProperty("custom.led.sentimus.sentiment.dictionary");
		try (Reader reader = new FileReader(new File(sentimentDictionarySource))) {
			log.info("Using dictionary at location {}", sentimentDictionarySource);
			CSVFormat format = CSVFormat.DEFAULT;
			try (CSVParser parser = new CSVParser(reader, format)) {
				Iterator<CSVRecord> records = parser.iterator();
				while (records.hasNext()) {
					CSVRecord record = records.next();
					try {
						Double val = Double.valueOf(record.get(1));
						sentiment.put(record.get(0), val);
					} catch (Exception ex) {
						log.warn("Corrupted record: {} ({})", record, ex.getMessage());
						// log.error("", ex);
					}
				}
			}
		} catch (IOException e) {
			log.warn("Cannot find dictionary at location {}", sentimentDictionarySource);
			throw new RuntimeException(e);
		}

	}

	@Override
	protected Double computeScore(TextWindow w) {
		// Types
		List<String> dept = Arrays.asList(deps);
		List<String> tags = Arrays.asList(new String[] {"J", "N", "V"});
		for (CoreMap cm : w.sentences()) {
			SemanticGraph tree = cm.get(BasicDependenciesAnnotation.class);
			for (TypedDependency co : tree.typedDependencies()) {
				if ((dept.contains("all") || dept.contains(co.reln().getShortName())) && co.dep() != null
						&& co.gov() != null && co.gov().tag() != null && co.dep().tag() != null
						&& tags.contains(co.gov().tag().substring(0, 1)) && tags.contains(co.dep().tag().substring(0, 1)) 
						) {
					Term gov = Term.build(co.gov().lemma(), co.gov().tag());
					Term dep = Term.build(co.dep().lemma(), co.dep().tag());
					Double govSentScore = sentiment.get(gov.toAString().toLowerCase());
					Double govMusScore = music.get(gov.toAString().toLowerCase());
					Double depSentScore = sentiment.get(dep.toAString().toLowerCase());
					Double depMusScore = music.get(dep.toAString().toLowerCase());
					if (govSentScore != null && depMusScore != null && govSentScore >= sentimentThreshold
							&& depMusScore >= musicThreshold) {
						// YES
						log.trace("YES: {}>{} (s>m) [{} {}]", new String[] { gov.toAString(), dep.toAString() , Double.toString(govSentScore), Double.toString(depMusScore)});
						return (double) 1.0;
					}
					if (govMusScore != null && depSentScore != null && govMusScore >= musicThreshold
							&& depSentScore >= sentimentThreshold) {
						// YES
						log.trace("YES: {}>{} (m>s) [{} {}]", new String[] { gov.toAString(), dep.toAString(), Double.toString(govMusScore), Double.toString(depSentScore) });
						return (double) 1.0;
					}
					// No
					log.trace("NO: {}>{} ", new String[] { gov.toAString(), dep.toAString() });
				}
			}
		}
		return 0.0;
	}

	@Override
	protected boolean isScoreEnough(Double score) {
		return score > 0;
	}
	
	
}
