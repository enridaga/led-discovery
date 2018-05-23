package led.discovery.annotator.evaluators;

import edu.stanford.nlp.util.CoreMap;
import led.discovery.annotator.MusicalHeatAnnotator;
import led.discovery.annotator.MusicalHeatAnnotator.MusicalHeatScoreAnnotation;
import led.discovery.annotator.window.TextWindow;
import led.discovery.annotator.window.TextWindowEvaluator;

public class HeatEvaluator implements TextWindowEvaluator {
	private Double threshold;

	public HeatEvaluator(Double threshold) {
		this.threshold = threshold;
	}

	@Override
	public boolean pass(TextWindow w) {
		double heat = 0;
		int sentences = 0;
		// If average heat is above 1
		for (CoreMap cm : w.sentences()) {
			heat += cm.get(MusicalHeatScoreAnnotation.class);
			sentences++;
		}
		// log.debug("{}", heat);
		double relativeScore = heat / (double) sentences;
		//log.trace("{} {} {}", new Object[] {heat, sentences, relativeScore});
		if ( relativeScore > threshold) {
			return true;
		}
		return false;
	}
}