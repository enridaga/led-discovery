package led.discovery.experiments;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.jsoup.Jsoup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreAnnotations.LemmaAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.PartOfSpeechAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.Label;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.semgraph.SemanticGraphCoreAnnotations.BasicDependenciesAnnotation;
import edu.stanford.nlp.trees.Dependency;
import edu.stanford.nlp.trees.TypedDependency;
import edu.stanford.nlp.util.CoreMap;
import led.discovery.annotator.MusicalHeatAnnotator.MusicalHeatScoreAnnotation;
import led.discovery.nlp.LemmaCleaner;
import led.discovery.nlp.StandardLemmaCleaner;
import led.discovery.nlp.Term;

public class PatternsExtractor {
	private static final Logger L = LoggerFactory.getLogger(PatternsExtractor.class);
	private File dataDir;
	private File training;
//	private File negativesDir;
	private File positivesDir;
	private File output;
	private Properties properties;
	private HashMap<String, Double> dictionary;
	private final double TH = 0.000112;

	public PatternsExtractor(String[] args) throws IOException {
		dataDir = new File(args[0]);
		output = new File(dataDir, "analysis/led-patterns.csv");
//		negativesDir = new File(dataDir, "negatives");
		positivesDir = new File(dataDir, "experiences");
		training = new File(dataDir, "evaluation/for-training.csv");
		properties = new Properties();
		properties.setProperty("annotators", "tokenize,ssplit,pos,parse,depparse,lemma,led.musicalheat");
		properties.setProperty("customAnnotatorClass.led.musicalheat", "led.discovery.annotator.MusicalHeatAnnotator");
		properties.setProperty("custom.led.heat.threshold", "0.000112");
		properties.setProperty("custom.led.heat.dictionary",
				new File(dataDir, "/gut_dictionary_V2.csv").getAbsolutePath());
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

//		Double max = 0.0;
//		for (File f : negativesDir.listFiles()) {
//			if (f.getName().endsWith(".txt")) {
//				L.info("Processing {}", f);
//				StanfordCoreNLP pipeline = new StanfordCoreNLP(properties);
//				Annotation a = new Annotation(IOUtils.toString(new FileInputStream(f), StandardCharsets.UTF_8));
//				pipeline.annotate(a);
//				Double maxValue = a.get(HeatMaxValueMetAnnotation.class);
//				if (max < maxValue) {
//					max = maxValue;
//				}
//			}
//		}
		LemmaCleaner cleaner = new StandardLemmaCleaner();
		Set<String> stopwords;
		try {
			stopwords = new HashSet<String>(
					IOUtils.readLines(getClass().getResourceAsStream("/led/discovery/nlp/stopwords.txt"), "UTF-8"));
		} catch (IOException e) {
			L.error("Cannot load stopwords.", e);
			throw new RuntimeException(e);
		}

		try (FileWriter fw = new FileWriter(output, true)) {

			for (File f : positivesDir.listFiles()) {
				if (f.getName().endsWith(".txt")) {
					if (!experiences.contains(f.getName().replaceAll(".txt$", ""))) {
						continue;
					}
					boolean first = true;
					L.info("Processing {}", f);
					// Skip items not in the training set
					StanfordCoreNLP pipeline = new StanfordCoreNLP(properties);
					Annotation a = new Annotation(
							Jsoup.parse(IOUtils.toString(new FileInputStream(f), StandardCharsets.UTF_8)).text());
					pipeline.annotate(a);

					// Iterate over each sentence. If the token as a heat score over the threshold,
					// then keep the word, otherwise it is its POS tag
					// Separate sentences with pipe
					fw.write(f.getName());
					fw.write(",");
					for (CoreMap cm : a.get(CoreAnnotations.SentencesAnnotation.class)) {
//						for (CoreLabel to : cm.get(CoreAnnotations.TokensAnnotation.class)) {
//							String lemma = to.getString(LemmaAnnotation.class);
//							String pos = to.getString(PartOfSpeechAnnotation.class);
//
//							if (stopwords.contains(lemma)) {
//								continue;
//							}
//							if ((lemma = cleaner.clean(lemma)) == null) {
//								continue;
//							}
//							Term t = Term.build(lemma, pos);
//							double score = to.get(MusicalHeatScoreAnnotation.class);
//							L.info("{} {}", t.toAString(), score);
//							if (score > TH) {
//								fw.write(t.toAString().toLowerCase());
//								fw.write(" ");
//							} else {
//								fw.write(t.getAPOS().toLowerCase());
//								fw.write(" ");
//							}
//						}
						SemanticGraph tree = cm.get(BasicDependenciesAnnotation.class);
						L.debug("graph: {}", tree.size());
						// BasicDependenciesAnnotation
						// Types
						List<String> dept = Arrays.asList(new String[] { "nsubj", "dobj", "nsubjpass", "iobj" });
						for (TypedDependency co : tree.typedDependencies()) {
							if (dept.contains(co.reln().getShortName())) {
								if (first == false) {
									fw.write(" ");
								}
								fw.write(co.reln().getShortName());
								fw.write(":");
								fw.write(co.gov().lemma());
								fw.write("[");
								fw.write(co.gov().tag().substring(0, 1).toLowerCase());
								fw.write("]");
								fw.write(">");
								fw.write(co.dep().lemma());
								fw.write("[");
								fw.write(co.dep().tag().substring(0, 1).toLowerCase());
								fw.write("]");
								L.trace("{}:{}[{}]>{}[{}]", new String[] { co.reln().getShortName(), co.gov().lemma(),
										co.gov().tag().substring(0, 1).toLowerCase(), co.dep().lemma(), co.dep().tag().substring(0, 1).toLowerCase() });
								first = false;
							}
						}
//						fw.write(". ");
					}
					fw.write("\n");

				}
			}

		}
	}

	public static final void main(String[] args) throws IOException {
		new PatternsExtractor(args).run();
	}
}
