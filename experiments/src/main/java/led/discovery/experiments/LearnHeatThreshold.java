package led.discovery.experiments;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import led.discovery.annotator.ListeningExperienceAnnotator.HeatMaxValueMetAnnotation;
import led.discovery.annotator.ListeningExperienceAnnotator.HeatMinValueMetAnnotation;

public class LearnHeatThreshold {
	private static final Logger L = LoggerFactory.getLogger(LearnHeatThreshold.class);
	private File dataDir;
	private File training;
	private File negativesDir;
	private File positivesDir;
	private File output;
	private Properties properties;

	public LearnHeatThreshold(String[] args) throws IOException {
		dataDir = new File(args[0]);
		output = new File(dataDir, "experiments/learnt-heat-threshold.txt");
		negativesDir = new File(dataDir, "negatives");
		positivesDir = new File(dataDir, "experiences");
		training = new File(dataDir, "evaluation/for-training.csv");
		properties = new Properties();
		properties.load(getClass().getResourceAsStream("LearnHeatThreshold.properties"));
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

		
		Double max = 0.0;
		for (File f : negativesDir.listFiles()) {
			if (f.getName().endsWith(".txt")) {
				L.info("Processing {}", f);
				StanfordCoreNLP pipeline = new StanfordCoreNLP(properties);
				Annotation a = new Annotation(IOUtils.toString(new FileInputStream(f), StandardCharsets.UTF_8));
				pipeline.annotate(a);
				Double maxValue = a.get(HeatMaxValueMetAnnotation.class);
				if (max < maxValue) {
					max = maxValue;
				}
			}
		}
		
		Double LEDmin = 100.0;
		Double LEDmax = 0.0;
		for (File f : positivesDir.listFiles()) {
			if (f.getName().endsWith(".txt")) {
				if (!experiences.contains(f.getName().replaceAll(".txt$", ""))) {
					continue;
				}
				L.info("Processing {}", f);
				// Skip items not in the training set
				StanfordCoreNLP pipeline = new StanfordCoreNLP(properties);
				Annotation a = new Annotation(IOUtils.toString(new FileInputStream(f), StandardCharsets.UTF_8));
				pipeline.annotate(a);
				Double maxValue = a.get(HeatMaxValueMetAnnotation.class);
				Double minValue = a.get(HeatMinValueMetAnnotation.class);
				if (LEDmax < maxValue) {
					LEDmax = maxValue;
				}
				if (LEDmin > minValue) {
					LEDmin = minValue;
				}
			}
		}
		
		try (FileWriter fw = new FileWriter(output, true)) {
			fw.write("Reuters Max: ");
			fw.write(Double.toString(max));
			fw.write("\n");
			fw.write("LEDs Max: ");
			fw.write(Double.toString(LEDmax));
			fw.write("\n");
			fw.write("LEDs Min: ");
			fw.write(Double.toString(LEDmin));
			fw.write("\n");
			
		}
	}

	public static final void main(String[] args) throws IOException {
		new LearnHeatThreshold(args).run();
	}
}
