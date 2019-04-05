package led.discovery.annotator.evaluators;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Arrays;
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
	private String[] skippos;
	public HeatEvaluator(Properties properties, LemmaCleaner cleaner, Set<String> stopwords) {
		super(cleaner, stopwords);
//		dictionary = new HashMap<String, Double>();
		String _heatThreshold = properties.getProperty("custom.led.heat.threshold");
		String _skippos = properties.getProperty("custom.led.heat.skippos");
		if (_heatThreshold == null) {
			threshold = DEFAULT_THRESHOLD;
		} else {
			threshold = Double.valueOf(_heatThreshold);
		}
		if (_skippos == null) {
			skippos = new String[0];
		} else {
			skippos = _skippos.split(",");
		}
		log.info("threshold: {}", threshold);
		log.info("skippos: {}", skippos);
	}

	public double getMaxValueMet() {
		return maxValueMet;
	}

	public double getMinValueMet() {
		return minValueMet;
	}

	@Override
	protected Double computeScore(TextWindow w) {
		double score = 0.0;
		int tokens = 0;
		for (CoreMap cm : w.sentences()) {
			for (CoreLabel to : cm.get(CoreAnnotations.TokensAnnotation.class)) {
				if (skip(to)) {
					continue;
				}
				if (!Arrays.asList(skippos).contains(to.tag().toLowerCase())
						&& !Arrays.asList(skippos).contains(to.tag().substring(0, 1).toLowerCase())) {
					score += to.get(MusicalHeatScoreAnnotation.class);
				}
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