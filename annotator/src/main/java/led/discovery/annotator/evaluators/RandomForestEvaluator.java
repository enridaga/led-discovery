package led.discovery.annotator.evaluators;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import edu.stanford.nlp.util.StringUtils;
import led.discovery.annotator.window.TextWindow;
import led.discovery.annotator.window.TextWindowEvaluator;
import led.discovery.nlp.StanfordNLPProvider;
import led.discovery.nlp.Term;
import led.discovery.spark.Predictor;

/**
 * Properties:
 * 
 * @author enridaga
 *
 */
public class RandomForestEvaluator implements TextWindowEvaluator {
	private Predictor predictor;
	private StanfordNLPProvider provider;

	public RandomForestEvaluator(Properties properties, StanfordNLPProvider provider) throws IOException {
		String modelPath = properties.getProperty("custom.led.forest.model");
		String vocabularyPath = properties.getProperty("custom.led.forest.vocabulary");
		predictor = new Predictor(modelPath, vocabularyPath);
		this.provider = provider;
	}

	public RandomForestEvaluator(String modelPath, String vocabularyPath) throws IOException {
		predictor = new Predictor(modelPath, vocabularyPath);
	}

	@Override
	public boolean pass(TextWindow w) {
		List<String> tl = new ArrayList<String>();
		List<Term> terms = provider.terms(w.sentences());
		for (Term t : terms)
			tl.add(t.toString());
		String text = StringUtils.join(tl.toArray(new String[tl.size()]), " ");
		boolean p = predictor.isLED(text);
		return p;
	}
}
