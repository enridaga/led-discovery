package led.discovery.experiments;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.jsoup.Jsoup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.semgraph.SemanticGraphCoreAnnotations.BasicDependenciesAnnotation;
import edu.stanford.nlp.trees.TypedDependency;
import edu.stanford.nlp.util.CoreMap;
import led.discovery.annotator.DBpediaSpotlightAnnotator.DBpediaEntityAnnotation;
import led.discovery.annotator.DBpediaSpotlightAnnotator.EntityLabel;
import led.discovery.annotator.MusicalEntityAnnotator.MusicalEntityAnnotation;
import led.discovery.annotator.MusicalHeatAnnotator.MusicalHeatScoreAnnotation;
import led.discovery.nlp.LemmaCleaner;
import led.discovery.nlp.StandardLemmaCleaner;

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
		properties.setProperty("custom.spotlight.service", "http://anne.kmi.open.ac.uk/rest/annotate");
		properties.setProperty("custom.dbpedia.service", "http://dbpedia.org/sparql");
		properties.setProperty("custom.cache", new File(dataDir, ".simple_cache").getAbsolutePath());
		properties.setProperty("customAnnotatorClass.led.spotlight",
				"led.discovery.annotator.DBpediaSpotlightAnnotator");
		properties.setProperty("customAnnotatorClass.led.musicentity",
				"led.discovery.annotator.MusicalEntityAnnotator");
		properties.setProperty("customAnnotatorClass.led.musicalheat", "led.discovery.annotator.MusicalHeatAnnotator");
		properties.setProperty("custom.led.heat.threshold", "0.507150");
//		properties.setProperty("custom.led.evaluators", "heat");
		properties.setProperty("annotators",
				"tokenize,ssplit,pos,parse,depparse,lemma,led.spotlight,led.musicalheat,led.musicentity"); // ,led.experiences
		properties.setProperty("custom.led.heat.dictionary",
				new File(dataDir, "/le-components/dictionary-music-simple.csv").getAbsolutePath());
		// new File(dataDir, "/gut_dictionary_V2.csv").getAbsolutePath());
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
					fw.write("\n");
					for (CoreMap cm : a.get(CoreAnnotations.SentencesAnnotation.class)) {
						fw.write(cm.toString());
						fw.write("\n");
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

						
						List<CoreLabel> tokens = cm.get(CoreAnnotations.TokensAnnotation.class);
						SemanticGraph tree = cm.get(BasicDependenciesAnnotation.class);
						List<EntityLabel> entities = cm.get(DBpediaEntityAnnotation.class);
						Set<String> musicEntities = cm.get(MusicalEntityAnnotation.class);
						L.trace("---------------------------------------------");
						L.trace("{}", cm);
						L.debug("graph: {}", tree.size());
						L.trace(">m {}<", musicEntities);
						L.trace(">e {}<", entities.size());
						// BasicDependenciesAnnotation
						// Types
						List<String> dept = Arrays.asList(new String[] { "nsubj", "dobj", "nsubjpass", "iobj" });
						SemanticGraph sg = new SemanticGraph();
						// Let's prune the graph and keep only useful items
//						sg.
						
						Map<Integer,String> representation = new HashMap<Integer,String>();
						for (TypedDependency co : tree.typedDependencies()) {
							if (true || dept.contains(co.reln().getShortName())) {
								if (first == false) {
									fw.write(" ");
								}
//								StringBuilder sb = new StringBuilder();

								//L.trace("{}", co);
//								if (co.reln() != null) {
//									sb.append(co.reln().getShortName());
//								} else {
//									sb.append("*");
//								}
//								sb.append(":");
//								sb.append(co.gov().lemma());
//								if(co.gov().tag()!=null) {
//									sb.append("[");
//									sb.append(co.gov().tag().substring(0, 1).toLowerCase());
//									sb.append("]");
//								}
								// Get Score From Token
								double gs = findScore(co.gov(), tokens);
								String gent = findEntity(co.gov(), entities, musicEntities);
//								sb.append("{");
//								sb.append(gs);
//								sb.append("}");
//								sb.append(">");
//								sb.append(co.dep().lemma());
//								sb.append("[");
//								sb.append(co.dep().tag().substring(0, 1).toLowerCase());
//								sb.append("]");
								double ds = findScore(co.dep(), tokens);
								String dent = findEntity(co.dep(), entities, musicEntities);
//								sb.append("{");
//								sb.append(ds);
//								sb.append("}");
//								String tos = sb.toString();
								StringBuilder sb = new StringBuilder();
								sb.append("- ");
								sb.append(co);
								sb.append(" > [");
								sb.append(gs);
								sb.append("] [");
								sb.append(gent);
								sb.append("] : [");
								sb.append(ds);
								sb.append("] [");
								sb.append(dent);
								sb.append("]");
								fw.write(sb.toString());
								fw.write("\n");
//								L.trace("{}", co);
//								L.trace(" > > {} {}", gs, gent);
//								L.trace(" > > {} {}", ds, dent);
//								if(ds > 0.0 || gs > 0.0) {
//									
////									DecimalFormat df = new DecimalFormat("#.#");
////									L.trace("PATTERN: {} {} {} {} {}", new Object[] {co.reln(), co.gov().tag().substring(0, 1).toLowerCase(), df.format(gs), co.dep().tag().substring(0, 1).toLowerCase(), df.format(ds)});
//								}
//								if(dent != null || gent != null) {									
//									L.trace("{} {}", co, tos);
//									
//								}
								first = false;
							}
						}
//						fw.write(". ");
					}
					fw.write("==================================================\n");
				}
			}

		}
	}

	public static final String representation(IndexedWord i, double score, String entity) {
		// If Entity, Show Entity
		if (entity != null) {
			return entity;
		} else if (score > 0) {
			DecimalFormat df = new DecimalFormat("#.#");
			return df.format(score);
		} else {
			return "";
		}
	}

	public static final Double findScore(IndexedWord iw, List<CoreLabel> tokens) {
		int beginPosition = iw.beginPosition();
		for (CoreLabel token : tokens) {
			if (beginPosition == token.beginPosition()) {
				// L.trace("({})", token);
				return token.get(MusicalHeatScoreAnnotation.class);
			}
		}
		return -1.0;
	}

	public static final String findEntity(IndexedWord iw, List<EntityLabel> entities, Set<String> musicEntities) {
		int beginPosition = iw.beginPosition();
		for (EntityLabel entity : entities) {
//			if(iw.lemma()!=null && iw.lemma().contains("composer")) {
////				musicEntities.iterator().next().substring(0, 1);
//				L.trace("{} {} -- {} {} / {} :: {} +++ {}",new Object[] {iw, beginPosition, entity.beginPosition(), entity.getUri(), musicEntities, musicEntities.contains(entity.getUri()), musicEntities.iterator().next().substring(0, 1)});
//			}
			if (beginPosition == entity.beginPosition() && musicEntities.contains(entity.getUri())) {
				L.trace(">{}<", entity);
				return entity.getUri();
			}
		}
		return null;
	}

	public static final void main(String[] args) throws IOException {
		new PatternsExtractor(args).run();
	}
}
