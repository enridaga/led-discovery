package led.discovery.experiments;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.esotericsoftware.minlog.Log;

import edu.stanford.nlp.util.StringUtils;
import led.discovery.spark.FeaturesFactory;

public class AnalyseComponentsCoverage {
	private static final Logger L = LoggerFactory.getLogger(AnalyseComponentsCoverage.class);
	private File dataDir;
	private File training;
	private File inputDir;
	private File output;
	private Properties properties;

	public AnalyseComponentsCoverage(String[] args) throws IOException {
		dataDir = new File(args[0]);
		inputDir = new File(args[1]);
		output = new File(args[2]);

		training = new File(dataDir, "evaluation/for-training.csv");
		properties = new Properties();
		properties.load(getClass().getResourceAsStream("LearnHeatThreshold.properties"));
	}

	private void _clean() throws IOException {
		output.delete();
		output.createNewFile();
	}

	private Map<String, Double> dictionary(File file)
			throws UnsupportedEncodingException, FileNotFoundException, IOException {
		Map<String, Double> dict = new HashMap<String, Double>();
		try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"))) {
			String line;
			while ((line = br.readLine()) != null) {
				String[] s = line.split(",");
				try {
					dict.put(s[0], Double.parseDouble(s[1]));
				} catch (NumberFormatException nfe) {
					Log.error("NumberFormatException: ignoring <{}>", line);
				}
			}
		}
		return dict;
	}

	/**
	 * Frequency of terms from the dictionary adjusted by terms length
	 * 
	 * @param dictionary
	 * @param terms
	 * @return
	 */
	private double hits(Map<String, Double> dictionary, String[] terms) {
		List<String> l = new ArrayList<String>(Arrays.asList(terms));
		l.retainAll(dictionary.keySet());
		return (double) l.size() / (double) terms.length;
	}

	/**
	 * Average score of terms from the dictionary (considering missing terms)
	 * 
	 * @param dictionary
	 * @param terms
	 * @return
	 */
	private double score(Map<String, Double> dictionary, String[] terms) {
		double sum = 0;
		for (String t : terms) {
			if (dictionary.containsKey(t)) {
				sum += dictionary.get(t);
			}
		}
		return (double) sum / (double) terms.length;
	}

	/**
	 * Average score of terms from the dictionary (excluding missing terms)
	 * Relevance score of the dictionary wrt the example
	 * 
	 * @param dictionary
	 * @param terms
	 * @return
	 */
	private double relevance(Map<String, Double> dictionary, String[] terms) {
		double sum = 0;
		int len = 0;
		for (String t : terms) {
			if (dictionary.containsKey(t)) {
				len++;
				sum += dictionary.get(t);
			}
		}
		if (len == 0)
			return 0.0;
		return (double) sum / (double) len;
	}

	void run() throws IOException {
		_clean();

		List<String> experiences = new ArrayList<String>();
		try (BufferedReader r = new BufferedReader(new FileReader(training))) {
			for (String line; (line = r.readLine()) != null;) {
				experiences.add(line);
			}
		}

		Map<String, Double> dictionaryEvent = dictionary(new File(dataDir, "le-components/dictionary-event-1.csv"));
		Map<String, Double> dictionarySound = dictionary(new File(dataDir, "le-components/dictionary-music-1.csv"));
		Map<String, Double> dictionaryListener = dictionary(
				new File(dataDir, "le-components/dictionary-listener-1.csv"));
		Map<String, Double> dictionaryPerformer = dictionary(
				new File(dataDir, "le-components/dictionary-performer-1.csv"));
		// Map<String, Double> dictionaryEnjoy = dictionary(new File(dataDir,
		// "le-components/dictionary-enjoy.csv"));
		// Map<String, Double> dictionaryDislike = dictionary(new File(dataDir,
		// "le-components/dictionary-dislike.csv"));
		Map<String, Double> dictionaryGutenberg = dictionary(new File(dataDir, "gut_dictionary_V2.csv"));
		FeaturesFactory factory = new FeaturesFactory();
//		Map<String, Double> dictionarySentiment = dictionaryEnjoy;
//		dictionarySentiment.putAll(dictionaryDislike);
		Map<String, Double> dictionarySentiment = dictionary(
				new File(dataDir, "le-components/dictionary-sentiment.csv"));
		try (FileWriter fw = new FileWriter(output, true)) {
			for (File f : inputDir.listFiles()) {
				if (f.getName().endsWith(".txt")) {
					if (inputDir.getName().equals("experiences")
							&& !experiences.contains(f.getName().replaceAll(".txt$", ""))) {
						L.info("Skipping {}", f);
						continue;
					}
					L.info("Processing {}", f);
					// If listening experiences, skip items not in the training set
					String text = IOUtils.toString(new FileInputStream(f), StandardCharsets.UTF_8);
					String[] terms = StringUtils.join(factory.aterms(text), " ").toLowerCase().split(" ");
					// A - Name
					fw.write(f.getName());
					fw.write(","); // B
					fw.write(Double.toString(hits(dictionaryEvent, terms)));
					fw.write(","); // C
					fw.write(Double.toString(score(dictionaryEvent, terms)));
					fw.write(","); // D
					fw.write(Double.toString(hits(dictionarySound, terms)));
					fw.write(","); // E
					fw.write(Double.toString(score(dictionarySound, terms)));
					fw.write(","); // F
					fw.write(Double.toString(hits(dictionaryListener, terms)));
					fw.write(","); // G
					fw.write(Double.toString(score(dictionaryListener, terms)));
					fw.write(","); // H
					fw.write(Double.toString(hits(dictionaryPerformer, terms)));
					fw.write(","); // I
					fw.write(Double.toString(score(dictionaryPerformer, terms)));
					fw.write(","); // J
					fw.write(Double.toString(hits(dictionarySentiment, terms)));
					fw.write(","); // K
					fw.write(Double.toString(score(dictionarySentiment, terms)));
					fw.write(","); // L
					fw.write(Double.toString(hits(dictionaryGutenberg, terms)));
					fw.write(","); // M
					fw.write(Double.toString(score(dictionaryGutenberg, terms)));
					// Relevance
					fw.write(","); // N
					fw.write(Double.toString(relevance(dictionaryEvent, terms)));
					fw.write(","); // O
					fw.write(Double.toString(relevance(dictionarySound, terms)));
					fw.write(","); // P
					fw.write(Double.toString(relevance(dictionaryListener, terms)));
					fw.write(","); // Q
					fw.write(Double.toString(relevance(dictionaryPerformer, terms)));
					fw.write(","); // R
					fw.write(Double.toString(relevance(dictionarySentiment, terms)));
					fw.write(","); // S
					fw.write(Double.toString(relevance(dictionaryGutenberg, terms)));
					fw.write("\n");
				}
			}
		}
	}

	public static final void main(String[] args) throws IOException {
		new AnalyseComponentsCoverage(args).run();
	}
}
