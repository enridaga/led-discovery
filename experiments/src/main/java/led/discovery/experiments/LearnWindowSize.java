package led.discovery.experiments;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;
import led.discovery.benchmark.MapUtil;

public class LearnWindowSize {
	private static final Logger L = LoggerFactory.getLogger(LearnWindowSize.class);
	private File dataDir;
	private File experiencesDir;
	private File output;
	private File training;
	private Properties properties;

	public LearnWindowSize(String[] args) throws IOException {
		dataDir = new File(args[0]);
		output = new File(dataDir, "experiments/learnt-window-size.txt");
		experiencesDir = new File(dataDir, "experiences");
		training = new File(dataDir, "evaluation/for-training.csv");
		properties = new Properties();
		properties.load(getClass().getResourceAsStream("LearnWindowSize.properties"));
	}

	private void _clean() throws IOException {
		output.delete();
		output.createNewFile();
	}

	void run() throws IOException {
		_clean();
		List<String> experiences = new ArrayList<String>();
		try (BufferedReader r = new BufferedReader(new FileReader(training))) {
			for (String line; (line = r.readLine()) != null;) {
				experiences.add(line);
			}
		}

		int min = 100;
		int max = 0;
		int tmin = 100;
		int tmax = 0;

		Map<String, Integer> numberOfSentences = new HashMap<String, Integer>();
		Map<String, Integer> numberOfTokens = new HashMap<String, Integer>();
		for (File f : experiencesDir.listFiles()) {
			if (f.getName().endsWith(".txt")) {
				if (!experiences.contains(f.getName().replaceAll(".txt$", ""))) {
					continue;
				}
				L.info("Processing {}", f);
				StanfordCoreNLP pipeline = new StanfordCoreNLP(properties);
				Annotation a = new Annotation(IOUtils.toString(new FileInputStream(f), StandardCharsets.UTF_8));
				pipeline.annotate(a);
				List<CoreMap> sentences = a.get(CoreAnnotations.SentencesAnnotation.class);
				List<CoreLabel> tokens = a.get(CoreAnnotations.TokensAnnotation.class);
				if (sentences.size() < min) {
					min = sentences.size();
				}
				if (sentences.size() > max) {
					max = sentences.size();
				}
				if (tokens.size() < tmin) {
					tmin = tokens.size();
				}
				if (tokens.size() > tmax) {
					tmax = tokens.size();
				}
				numberOfSentences.put(f.getName(), sentences.size());
				numberOfTokens.put(f.getName(), tokens.size());
			}
		}
		numberOfSentences = MapUtil.sortByValueDesc(numberOfSentences);
		try (FileWriter fw = new FileWriter(output, true)) {
			for (Entry<String, Integer> en : numberOfSentences.entrySet()) {
				fw.write(en.getKey());
				fw.write(",");
				fw.write(Integer.toString(en.getValue()));
				fw.write(",");
				fw.write(Integer.toString(numberOfTokens.get(en.getKey())));
				fw.write("\n");
			}
		}
		L.info("Max number of sentences: {}", max);
		L.info("Min number of sentences: {}", min);
		L.info("Max number of tokens: {}", tmax);
		L.info("Min number of tokens: {}", tmin);
	}

	public static final void main(String[] args) throws IOException {
		new LearnWindowSize(args).run();
	}
}
