package led.discovery.experiments;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.TreeMap;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.jsoup.Jsoup;
import org.jsoup.safety.Whitelist;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.esotericsoftware.minlog.Log;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;
import led.discovery.annotator.DBpediaSpotlightAnnotator.DBpediaEntityAnnotation;
import led.discovery.annotator.DBpediaSpotlightAnnotator.EntityLabel;
import led.discovery.annotator.ListeningExperienceAnnotator.ListeningExperienceAnnotation;
import led.discovery.annotator.ListeningExperienceAnnotator.NotListeningExperienceAnnotation;
import led.discovery.annotator.MusicalHeatAnnotator.MusicalHeatScoreAnnotation;
import led.discovery.annotator.evaluators.HeatEntityEvaluator.HeatEntityScoreAnnotation;
import led.discovery.annotator.evaluators.HeatEntityEvaluator.MusicalEntityUriAnnotation;
import led.discovery.annotator.window.TextWindow;

/**
 * Run Experiments using the Gold Standard.
 * 
 *
 */
public class RunExperimentRED {

	private static final Logger L = LoggerFactory.getLogger(RunExperimentRED.class);

	private File dataDir;
	private File goldStandard;
	// private File sourcesDir;
	private File output;
	private Properties properties;
	private String experiment;
	private String lookupItem;

	public RunExperimentRED(String[] args) throws IOException {
		L.info("Experiment launched with args: {}", args);
		dataDir = new File(args[0]);
		experiment = args[1];
		String experimentProperties = null;
		if (args.length > 2) {
			experimentProperties = args[2];
		}
		lookupItem = null;
		if (args.length > 3) {
			lookupItem = args[3];
		}
		goldStandard = new File(dataDir, "red/RED Gold Standard.csv");
		// sourcesDir = new File(dataDir, "benchmark/sources");
		output = new File(dataDir, "red/experiments/" + experiment + ".output.csv");
		properties = new Properties();
		L.warn("experimentProperties: {}", experimentProperties);
		if (experimentProperties != null && new File(experimentProperties).exists()) {
			try (FileReader r = new FileReader(new File(experimentProperties))) {
				properties.load(r);
			}
		} else {
			properties.load(getClass().getResourceAsStream(experiment + ".properties"));
		}

		L.info("Experiments for the Gold Standard apply the FixedWindow, overriding step, min, and max to -1");
		String[] keys = new String[] { "custom.led.window.min", "custom.led.window.max", "custom.led.window.step" };
		for (String k : keys) {
			properties.setProperty(k, "-1");
		}
	}

	void clean() throws IOException {
		L.info("Cleaning output file {}", output);
		output.delete();
		output.createNewFile();
	}

	void run() throws IOException {
		L.info("Running experiment {}", experiment);
		L.info("Gold standard: {}", goldStandard);
		StringWriter writer = new StringWriter();
		properties.list(new PrintWriter(writer));
		L.info("Properties: \n{}", writer.getBuffer().toString());

		clean();

		StanfordCoreNLP pipeline = new StanfordCoreNLP(properties);

		if (lookupItem != null) {
			List<String> sources = Arrays.asList(lookupItem.split(","));
			L.info("Lookup Item No: {}", lookupItem);
			try (CSVParser reading = new CSVParser(new FileReader(goldStandard), CSVFormat.DEFAULT);) {
				Iterator<CSVRecord> iter = reading.getRecords().iterator();
				while (iter.hasNext()) {
					CSVRecord row = iter.next();
					boolean expected = row.get(0).equals("1.0");
					String source = row.get(1);
					if (sources.contains(source)) {
						L.info("Source: {} [{}]", source, expected);

						String text = row.get(2);
						// L.trace("BEFORE: {}", text);
						text = Jsoup.clean(text, Whitelist.simpleText());
						L.info("Text: {}", text);
						Annotation annotation = new Annotation(text);
						pipeline.annotate(annotation);

						List<TextWindow> passed = annotation.get(ListeningExperienceAnnotation.class);
						List<TextWindow> notPassed = annotation.get(NotListeningExperienceAnnotation.class);

						TextWindow tw;
						if (passed.size() == 1) {
							L.info("PASSED");
							tw = passed.get(0);
						} else {
							L.info("NOT PASSED");
							tw = notPassed.get(0);
						}

						Double tot = 0.0;
						int len = 0;
						for (CoreMap cm : tw.sentences()) {
							List<EntityLabel> entities = cm.get(DBpediaEntityAnnotation.class);
							System.out.println();
							L.info("> {}", entities);
							System.out.println();
							for (CoreLabel to : cm.get(CoreAnnotations.TokensAnnotation.class)) {
//								if (!to.containsKey(HeatEntityScoreAnnotation.class))
//									continue;
								len++;

								System.out.print(to.lemma());
								System.out.print("[");
								System.out.print(to.tag().toLowerCase().substring(0, 1));
								System.out.print("]");
								if (to.containsKey(MusicalEntityUriAnnotation.class)) {
									System.out.print("->");
									System.out.print(to.get(MusicalEntityUriAnnotation.class));
								}
								System.out.print(":");
								Double val = to.get(HeatEntityScoreAnnotation.class);
								if (val == null)
									val = 0.0;
								tot += val;
								double ts = to.get(MusicalHeatScoreAnnotation.class);
								System.out.print(Double.toString(ts));
								System.out.print(":");
								System.out.print(Double.toString(val));
								System.out.print(" ");
							}
						}
						Double mean = tot / len;
						System.out.println();
						System.out.print("Mean: ");
						System.out.print(Double.toString(mean));
						System.out.println("");
					}
				}
			}
		} else {
			L.info("Output to: {}, {}", output);

			try (CSVParser reading = new CSVParser(new FileReader(goldStandard), CSVFormat.DEFAULT);
					FileWriter fw = new FileWriter(output, true)) {
				Iterator<CSVRecord> iter = reading.getRecords().iterator();
				while (iter.hasNext()) {
					CSVRecord row = iter.next();
					boolean expected = row.get(0).equals("1.0");
					String source = row.get(1);

					String text = row.get(2);
					// L.trace("BEFORE: {}", text);
					text = Jsoup.clean(text, Whitelist.simpleText());
					L.trace("Text: {}", text);
					Annotation annotation = new Annotation(text);
					pipeline.annotate(annotation);

					List<TextWindow> passed = annotation.get(ListeningExperienceAnnotation.class);
					List<TextWindow> notPassed = annotation.get(NotListeningExperienceAnnotation.class);
					TextWindow tw;
					if (passed.size() == 1) {
						tw = passed.get(0);
					} else {
						tw = notPassed.get(0);
					}
					fw.write(source); // filename
					fw.write(",");
					fw.write(Integer.toString(0)); // from
					fw.write(",");
					fw.write(Integer.toString(text.length())); // to
					fw.write(",");
					fw.write(Integer.toString(text.length())); // length
					fw.write(",");
					fw.write(Integer.toString(tw.size())); // number of sentences
					fw.write(",");
					if (passed.contains(tw)) {
						fw.write("Y");
					} else if (notPassed.contains(tw)) {
						fw.write("N");
					} else {
						// This should never happen!
						Log.error("text window lost");
						fw.write("E");
					}
					L.info("Source: {} :: {} > {}", new Object[] { source, expected, passed.contains(tw) });
					fw.write(",");
					if (expected == false) {
						fw.write("N");
						fw.write(",-");
						fw.write(",-1");
						fw.write(",-1");
					} else {
						fw.write("Y");
						fw.write(",");
						fw.write(row.get(0));
						fw.write(",");
						fw.write(Integer.toString(0));
						fw.write(",");
						fw.write(Integer.toString(text.length()));
					}

					// Add Column for Scores
					fw.write(",");
					Map<Object, Double> scores = tw.getScores();
					TreeMap<String, Double> scoresSorted = new TreeMap<String, Double>();
					for (Entry<Object, Double> en : scores.entrySet()) {

						String k;
						if (en.getKey() instanceof Class<?>) {
							k = ((Class<?>) en.getKey()).getSimpleName();
						} else {
							k = en.getKey().toString();
						}
						scoresSorted.put(k, en.getValue());
					}
					boolean first = true;
					for (Entry<String, Double> en : scoresSorted.entrySet()) {
						if (first) {
							first = false;
						} else {
							fw.write("&");
						}
						fw.write(en.getKey());
						fw.write("=");
						fw.write(Double.toString(en.getValue()));
					}
					fw.write("\n");
				}
			}
		}
	}

	public static final void main(String[] args) throws IOException {
		new RunExperimentRED(args).run();
	}
}
