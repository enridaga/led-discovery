package led.discovery.annotator.evaluators;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.util.CoreMap;
import led.discovery.annotator.MusicalHeatAnnotator.MusicalHeatScoreAnnotation;
import led.discovery.annotator.window.TextWindow;
import led.discovery.nlp.LemmaCleaner;

public class HeatEvaluator extends AbstractTextWindowEvaluator {
	private Double threshold;
	private double maxValueMet = 0.0;
	private double minValueMet = 100.0;
	public final static Double DEFAULT_THRESHOLD = 0.00043;
	private Logger log = LoggerFactory.getLogger(HeatEvaluator.class);
//	private HashMap<String, Double> dictionary;

	public HeatEvaluator(Properties properties, LemmaCleaner cleaner, Set<String> stopwords) {
		super(cleaner, stopwords);
//		dictionary = new HashMap<String, Double>();
		String _heatThreshold = properties.getProperty("custom.led.heat.threshold");
		if (_heatThreshold == null) {
			threshold = DEFAULT_THRESHOLD;
		} else {
			threshold = Double.valueOf(_heatThreshold);
		}
		log.info("threshold: {}", threshold);
//
//		String dictionarySource = properties.getProperty("custom.led.heat.dictionary");
//		Reader reader = null;
//
//		if (dictionarySource == null) {
//			log.info("Using embedded dictionary V1");
//			reader = new InputStreamReader(getClass().getResourceAsStream("dictionary.csv"));
//		} else {
//			try {
//				log.debug("Trying embedded dictionary {}", dictionarySource);
//				reader = new InputStreamReader(
//						getClass().getResourceAsStream("/led/discovery/annotator/" + dictionarySource));
//				log.info("Using embedded dictionary {}", dictionarySource);
//			} catch (NullPointerException npe) {
//				log.debug("Not found as embedded: {}", dictionarySource);
//				try {
//					reader = new FileReader(new File(dictionarySource));
//					log.info("Using dictionary at location {}", dictionarySource);
//				} catch (FileNotFoundException e) {
//					log.warn("Cannot find dictionary at location {}", dictionarySource);
//					throw new RuntimeException(e);
//				}
//			}
//		}
//		CSVFormat format = CSVFormat.DEFAULT;
//		try (CSVParser parser = new CSVParser(reader, format)) {
//			Iterator<CSVRecord> records = parser.iterator();
//			while (records.hasNext()) {
//				CSVRecord record = records.next();
//				try {
//					Double val = Double.valueOf(record.get(1));
//					dictionary.put(record.get(0), val);
//				} catch (Exception ex) {
//					log.warn("Corrupted record: {}", record);
//				}
//			}
//		} catch (IOException e) {
//			log.error("cannot load dictionary", e);
//		} finally {
//			try {
//				reader.close();
//			} catch (IOException e) {
//				log.warn("Cannot close reader");
//			}
//		}
	}

	public double getMaxValueMet() {
		return maxValueMet;
	}

	public double getMinValueMet() {
		return minValueMet;
	}

//	@Override
//	public boolean pass(TextWindow w) {
//		double score = 0.0;
//		int tokens = 0;
//		for (CoreMap cm : w.sentences()) {
//			for (CoreLabel to : cm.get(CoreAnnotations.TokensAnnotation.class)) {
//				if (skip(to)) {
//					continue;
//				}
//				score += to.get(MusicalHeatScoreAnnotation.class);
//				tokens++;
//			}
//		}
//		double relativeScore = score / (double) tokens;
//		log.trace("score {}", Double.toString(relativeScore));
//		if (relativeScore > maxValueMet) {
//			maxValueMet = relativeScore;
//		}
//		if (relativeScore < minValueMet) {
//			minValueMet = relativeScore;
//		}
//		if (relativeScore > threshold) {
//			return true;
//		}
//		return false;
//	}

	@Override
	protected Double computeScore(TextWindow w) {
		double score = 0.0;
		int tokens = 0;
		for (CoreMap cm : w.sentences()) {
			for (CoreLabel to : cm.get(CoreAnnotations.TokensAnnotation.class)) {
				if (skip(to)) {
					continue;
				}
				score += to.get(MusicalHeatScoreAnnotation.class);
				tokens++;
			}
		}
		double relativeScore = score / (double) tokens;
		log.trace("score {}", Double.toString(relativeScore));
		if (relativeScore > maxValueMet) {
			maxValueMet = relativeScore;
		}
		if (relativeScore < minValueMet) {
			minValueMet = relativeScore;
		}
		return relativeScore;
	}

	@Override
	protected boolean isScoreEnough(Double score) {
		if (score > threshold) {
			return true;
		}
		return false;
	}
}