package led.discovery.annotator.evaluators;

import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.stanford.nlp.ling.CoreAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.util.CoreMap;
import led.discovery.annotator.LedComponentsAnnotator;
import led.discovery.annotator.window.TextWindow;
import led.discovery.annotator.window.TextWindowEvaluator;

public class LedComponentsEvaluator implements TextWindowEvaluator {
	private Logger log = LoggerFactory.getLogger(LedComponentsEvaluator.class);

	enum Method {
		ALL, MIN3, MIN4;
	}

	Method method;

	double thhEvent = 0.0;
	double thhSound = 0.0;
	double thhListener = 0.0;
	double thhPerformer = 0.0;
	double thhSentiment = 0.0;

	public LedComponentsEvaluator(Properties properties) {
		String methods = properties.getProperty("custom.led.components.method");
		method = Method.valueOf(methods.toUpperCase());

		if (properties.containsKey("custom.led.components.thh.event")) {
			thhEvent = Double.parseDouble(properties.getProperty("custom.led.components.thh.event"));
		}
		if (properties.containsKey("custom.led.components.thh.sound")) {
			thhSound = Double.parseDouble(properties.getProperty("custom.led.components.thh.sound"));
		}
		if (properties.containsKey("custom.led.components.thh.sound")) {
			thhListener = Double.parseDouble(properties.getProperty("custom.led.components.thh.listener"));
		}
		if (properties.containsKey("custom.led.components.thh.performer")) {
			thhPerformer = Double.parseDouble(properties.getProperty("custom.led.components.thh.performer"));
		}
		if (properties.containsKey("custom.led.components.thh.sentiment")) {
			thhSentiment = Double.parseDouble(properties.getProperty("custom.led.components.thh.sentiment"));
		}
	}

	@Override
	public boolean pass(TextWindow w) {
		int amount = 0;
		for (Class<? extends CoreAnnotation<Double>> cls : LedComponentsAnnotator.annotations.values()) {
			double thh;
			if (cls.equals(LedComponentsAnnotator.LedEventAnnotation.class)) {
				thh = thhEvent;
			} else if (cls.equals(LedComponentsAnnotator.LedSoundAnnotation.class)) {
				thh = thhSound;
			} else if (cls.equals(LedComponentsAnnotator.LedListenerAnnotation.class)) {
				thh = thhListener;
			} else if (cls.equals(LedComponentsAnnotator.LedPerformerAnnotation.class)) {
				thh = thhPerformer;
			} else if (cls.equals(LedComponentsAnnotator.LedSentimentAnnotation.class)) {
				thh = thhSentiment;
			} else
				throw new RuntimeException("Unknown annotator: " + cls.getName());

			// If any sentence has some components
			int tokens = 0;
			double scoresum = 0.0;
			for (CoreMap cm : w.sentences()) {
				for (CoreLabel token : cm.get(CoreAnnotations.TokensAnnotation.class)) {
					tokens++;
					Double score = token.get(cls);
					if (score != null) {
						scoresum += score;
					}
				}
			}
			double thescore = scoresum / (double) tokens;
			if (thescore > thh) {
				log.trace("has component: {}", cls);
				amount++;
			}
		}
		log.debug("amount: {}", amount);
		if (method.equals(Method.ALL)) {
			return amount == 5;
		} else if (method.equals(Method.MIN4)) {
			return amount >= 4;
		} else if (method.equals(Method.MIN3)) {
			return amount >= 3;
		}
		return false;
	}
}
