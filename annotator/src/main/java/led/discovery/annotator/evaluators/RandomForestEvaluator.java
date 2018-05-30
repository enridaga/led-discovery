package led.discovery.annotator.evaluators;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

import led.discovery.annotator.window.TextWindow;
import led.discovery.annotator.window.TextWindowEvaluator;
import led.discovery.spark.Predictor;

/**
 * Properties:
 * 
 * @author enridaga
 *
 */
public class RandomForestEvaluator implements TextWindowEvaluator {
	private Predictor predictor;

	public RandomForestEvaluator(Properties properties) throws IOException {
		String modelPath = properties.getProperty("custom.led.forest.model");
		String vocabularyPath = properties.getProperty("custom.led.forest.vocabulary");
		predictor = new Predictor(modelPath, vocabularyPath);
	}

	public RandomForestEvaluator(String modelPath, String vocabularyPath) throws IOException {
		predictor = new Predictor(modelPath, vocabularyPath);
	}

	@Override
	public boolean pass(TextWindow w) {
		boolean p = predictor.isLED(w.toText());
		return p;
	}
}
