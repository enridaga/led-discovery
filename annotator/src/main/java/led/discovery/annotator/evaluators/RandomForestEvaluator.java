package led.discovery.annotator.evaluators;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import edu.stanford.nlp.util.StringUtils;
import led.discovery.analysis.entities.DBPediaTypes;
import led.discovery.analysis.entities.spot.SpotlightClient;
import led.discovery.annotator.window.TextWindow;
import led.discovery.annotator.window.TextWindowEvaluator;
import led.discovery.nlp.StanfordNLPProvider;
import led.discovery.spark.FeaturesFactory;
import led.discovery.spark.Predictor;
import led.discovery.spark.RandomForestPredictorCV;
import led.discovery.spark.RandomForestPredictorEmb;

/**
 * Properties:
 * 
 * @author enridaga
 *
 */
public class RandomForestEvaluator implements TextWindowEvaluator {
	private Predictor predictor;
	private FeaturesFactory f = new FeaturesFactory();
	private SpotlightClient client = null;
	private String features;
	private DBPediaTypes types = null;
	public RandomForestEvaluator(Properties properties, StanfordNLPProvider provider, SpotlightClient spotlight) throws IOException {
		String modelPath = properties.getProperty("custom.led.forest.model");
		features = properties.getProperty("custom.led.forest.features");
		client = spotlight;
		if (features == null) {
			features = "aterms";
		} else if (!Arrays.asList("aterms", "entities", "terms", "types").contains(features)) {
			throw new IOException("Invalid features: " + features);
		}
		
		if(features.equals("types")) {
			String typesFile = properties.getProperty("custom.led.forest.features.types");
			types = new DBPediaTypes(new File(typesFile));
		}
		String vocabularyPath = properties.getProperty("custom.led.forest.vocabulary");
		if(vocabularyPath == null) {
			String word2vecPath = properties.getProperty("custom.led.forest.word2vec");
			if(word2vecPath == null) throw new IOException("vocabulary / word2vec model not found");
			predictor = new RandomForestPredictorEmb(modelPath, word2vecPath);
		}else {
			predictor = new RandomForestPredictorCV(modelPath, vocabularyPath);
		}
	}
//
//	public RandomForestEvaluator(String modelPath, String vocabularyPath) throws IOException {
//		predictor = new RandomForestPredictor(modelPath, vocabularyPath);
//	}

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
		}else if (features.equals("types")) {
			String[] en = f.entities(w.toText(), client);
			List<String> et = new ArrayList<String>();
			for(String e:en) {
				et.addAll(types.types(e));
			}
			te = et.toArray(new String[et.size()]);
		}else {
			te = f.aterms(w.toText());
		}
		String text = StringUtils.join(te, " ");
		boolean p = predictor.isLED(text);
		return p;
	}
}
