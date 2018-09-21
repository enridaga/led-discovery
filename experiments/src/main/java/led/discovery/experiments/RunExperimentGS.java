package led.discovery.experiments;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.jsoup.Jsoup;
import org.jsoup.safety.Whitelist;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import led.discovery.annotator.ListeningExperienceAnnotator.ListeningExperienceAnnotation;
import led.discovery.annotator.ListeningExperienceAnnotator.NotListeningExperienceAnnotation;
import led.discovery.annotator.window.TextWindow;

public class RunExperimentGS {

	private static final Logger L = LoggerFactory.getLogger(RunExperimentGS.class);

	private File dataDir;
	private File goldStandard;
	// private File sourcesDir;
	private File output;
	private Properties properties;
	private String experiment;
//	private File[] onSources;

	public RunExperimentGS(String[] args) throws IOException {
		L.info("Experiment launched with args: {}", args);
		dataDir = new File(args[0]);
		experiment = args[1];
		String experimentProperties = null;
		if (args.length > 2) {
			experimentProperties = args[2];
		}
		goldStandard = new File(dataDir, "evaluation/gold-standard.csv");
		// sourcesDir = new File(dataDir, "benchmark/sources");
		output = new File(dataDir, "experiments/" + experiment + "-gs.output.csv");
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
		output.delete();
		output.createNewFile();
	}

	void run() throws IOException {
		L.info("Running experiment {}", experiment);
		L.info("Gold standard: {}", goldStandard);
		StringWriter writer = new StringWriter();
		properties.list(new PrintWriter(writer));
		L.info("Properties: \n{}", writer.getBuffer().toString());
		L.info("Output to: {}, {}", output);
		clean();

		StanfordCoreNLP pipeline = new StanfordCoreNLP(properties);

		try (CSVParser reading = new CSVParser(new FileReader(goldStandard), CSVFormat.DEFAULT);
				FileWriter fw = new FileWriter(output, true)) {
			Iterator<CSVRecord> iter = reading.getRecords().iterator();
			while (iter.hasNext()) {
				CSVRecord row = iter.next();
				boolean expected = row.get(0).equals("1.0");
				String source = row.get(1);
				String text = row.get(2);
				//L.trace("BEFORE: {}", text);
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
				fw.write(source);
				fw.write(",");
				fw.write(Integer.toString(0));
				fw.write(",");
				fw.write(Integer.toString(text.length()));
				fw.write(",");
				fw.write(Integer.toString(text.length()));
				fw.write(",");
				fw.write(Integer.toString(tw.size()));
				fw.write(",");
				if (passed.contains(tw)) {
					fw.write("Y");
				} else if (notPassed.contains(tw)) {
					fw.write("N");
				} else {
					// This should never happen!
					fw.write("E");
				}
				L.info("Source: {} :: {} > {}", new Object[] {source, expected, passed.contains(tw)});
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

				fw.write("\n");
			}
		}
	}

	public static final void main(String[] args) throws IOException {
		new RunExperimentGS(args).run();
	}
}
