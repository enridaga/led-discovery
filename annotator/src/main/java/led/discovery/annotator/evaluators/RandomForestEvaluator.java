package led.discovery.annotator.evaluators;

import java.io.IOException;
import java.util.Arrays;
import java.util.Properties;

import edu.stanford.nlp.util.StringUtils;
import led.discovery.analysis.entities.spot.SpotlightClient;
import led.discovery.annotator.window.TextWindow;
import led.discovery.annotator.window.TextWindowEvaluator;
import led.discovery.spark.FeaturesFactory;
import led.discovery.nlp.StanfordNLPProvider;
import led.discovery.spark.RandomForestPredictor;

/**
 * Properties:
 * 
 * @author enridaga
 *
 */
public class RandomForestEvaluator implements TextWindowEvaluator {
	private RandomForestPredictor predictor;
	private FeaturesFactory f = new FeaturesFactory();
	private SpotlightClient client = null;
	private String features;

	public RandomForestEvaluator(Properties properties, StanfordNLPProvider provider, SpotlightClient spotlight) throws IOException {
		String modelPath = properties.getProperty("custom.led.forest.model");
		features = properties.getProperty("custom.led.forest.features");
		client = spotlight;
		if (features == null) {
			features = "aterms";
		} else if (!Arrays.asList("aterms", "entities", "terms").contains(features)) {
			throw new IOException("Invalid features: " + features);
		}
		String vocabularyPath = properties.getProperty("custom.led.forest.vocabulary");
		predictor = new RandomForestPredictor(modelPath, vocabularyPath);
	}

	public RandomForestEvaluator(String modelPath, String vocabularyPath) throws IOException {
		predictor = new RandomForestPredictor(modelPath, vocabularyPath);
	}

	public String prepare(String text) {
		return text;
	}

	@Override
	public boolean pass(TextWindow w) {
		String[] te;
		if (features.equals("terms")) {
			te = f.terms(w.toText());
		}else if (features.equals("entities")) {
			te = f.entities(w.toText(), client);
		}else {
			te = f.aterms(w.toText());
		}
		String text = StringUtils.join(te, " ");
		boolean p = predictor.isLED(text);
		return p;
	}
}
