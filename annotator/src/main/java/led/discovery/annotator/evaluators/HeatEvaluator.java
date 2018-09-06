package led.discovery.annotator.evaluators;

import java.util.Properties;

import com.esotericsoftware.minlog.Log;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.util.CoreMap;
import led.discovery.annotator.MusicalHeatAnnotator.MusicalHeatScoreAnnotation;
import led.discovery.annotator.window.TextWindow;
import led.discovery.annotator.window.TextWindowEvaluator;

public class HeatEvaluator implements TextWindowEvaluator {
	private Double threshold;
	private double maxValueMet = 0.0;
	private double minValueMet = 100.0;
	public final static Double DEFAULT_THRESHOLD = 0.00043;

	public HeatEvaluator(Properties properties) {
		String _heatThreshold = properties.getProperty("custom.led.heat.threshold");
		if (_heatThreshold == null) {
			threshold = DEFAULT_THRESHOLD;
		} else {
			threshold = Double.valueOf(_heatThreshold);
		}
	}

	public double getMaxValueMet() {
		return maxValueMet;
	}

	public double getMinValueMet() {
		return minValueMet;
	}

	@Override
	public boolean pass(TextWindow w) {
		double score = 0;
		int tokens = 0;

		for (CoreMap cm : w.sentences()) {
			for (CoreLabel to : cm.get(CoreAnnotations.TokensAnnotation.class)) {
				score += to.get(MusicalHeatScoreAnnotation.class);
				tokens++;
			}
		}
		double relativeScore = score / (double) tokens;

//		double heat = 0;
//		int sentences = 0;
//		// If average heat is above 1
//		for (CoreMap cm : w.sentences()) {
//			heat += cm.get(MusicalHeatScoreAnnotation.class);
//			sentences++;
//		}
//		// log.debug("{}", heat);
//		double relativeScore = heat / (double) sentences;
		// log.trace("{} {} {}", new Object[] {heat, sentences, relativeScore});
		Log.trace("score {}", Double.toString(relativeScore));
		if (relativeScore > maxValueMet) {
			maxValueMet = relativeScore;
		}
		if (relativeScore < minValueMet) {
			minValueMet = relativeScore;
		}
		if (relativeScore > threshold) {
			return true;
		}
		return false;
	}
}