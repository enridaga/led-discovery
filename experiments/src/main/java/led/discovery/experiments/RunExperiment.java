package led.discovery.experiments;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import led.discovery.annotator.ListeningExperienceAnnotator.ListeningExperienceAnnotation;
import led.discovery.annotator.window.TextWindow;
import led.discovery.benchmark.Benchmark;

public class RunExperiment {

	private static final Logger L = LoggerFactory.getLogger(RunExperiment.class);

	private File dataDir;
	private File benchmark;
	private File sourcesDir;
	private File output2benchmark;
	private File benchmark2output;
	private Properties properties;
	private String experiment;

	public RunExperiment(String[] args) throws IOException {
		dataDir = new File(args[0]);
		experiment = args[1];
		benchmark = new File(dataDir, "evaluation/benchmark.csv");
		sourcesDir = new File(dataDir, "benchmark/sources");
		output2benchmark = new File(dataDir, "experiments/" + experiment + ".o2b.csv");
		benchmark2output = new File(dataDir, "experiments/" + experiment + ".b2o.csv");
		properties = new Properties();
		properties.load(getClass().getResourceAsStream(experiment +
			".properties"));
	}

	void clean() throws IOException {
		output2benchmark.delete();
		output2benchmark.createNewFile();
		benchmark2output.delete();
		benchmark2output.createNewFile();
	}

	void run() throws IOException {
		L.info("Running experiment {}", experiment);
		L.info("Benchmark: {}", benchmark);
		StringWriter writer = new StringWriter();
		properties.list(new PrintWriter(writer));
		L.info("Properties: \n{}", writer.getBuffer().toString());
		L.info("Output to: {}, {}", output2benchmark, benchmark2output);

		clean();

		Benchmark bench = new Benchmark(benchmark);
		StanfordCoreNLP pipeline = new StanfordCoreNLP(properties);
		Map<String, List<Integer[]>> matches = new HashMap<String, List<Integer[]>>();
		Map<String, List<Integer>> windowSizes = new HashMap<String, List<Integer>>();
		for (File f : sourcesDir.listFiles()) {
			L.info("Source: {}", f.getName());
			Annotation annotation = new Annotation(FileUtils.readFileToString(f, StandardCharsets.UTF_8));
			pipeline.annotate(annotation);
			String fname = f.getName();
			matches.put(fname, new ArrayList<Integer[]>());
			windowSizes.put(fname, new ArrayList<Integer>());
			List<TextWindow> found = annotation.get(ListeningExperienceAnnotation.class);
			L.info(" {} found", found.size());
			Iterator<TextWindow> it = found.iterator();
			while (it.hasNext()) {
				TextWindow tw = it.next();
				int from = tw.offsetStart();
				int to = tw.offsetEnd();
				matches.get(fname).add(new Integer[] { from, to });
				windowSizes.get(fname).add(tw.size());
			}
		}

		// Writing to the output2benchmark file
		Map<String, Integer[]> matchedExperience = new HashMap<String, Integer[]>();
		try (FileWriter fw = new FileWriter(output2benchmark, true)) {
			for (Entry<String, List<Integer[]>> en : matches.entrySet()) {
				int matched = 0;
				String fname = en.getKey();
				List<Integer[]> items =  en.getValue();
				for (int x = 0; x <items.size(); x++) {
					Integer[] i = items.get(x);
					fw.write(fname);
					fw.write(",");
					fw.write(Integer.toString(i[0]));
					fw.write(",");
					fw.write(Integer.toString(i[1]));
					fw.write(",");
					Object[] match = bench.matches(fname, i[0], i[1]);
					fw.write(Integer.toString(windowSizes.get(fname).get(x))); // add window size
					fw.write(",");
					
					if (match == null) {
						fw.write("N");
						fw.write(",-");
						fw.write(",-1");
						fw.write(",-1");
					} else {
						fw.write("Y");
						fw.write(",");
						fw.write((String) match[1]);
						fw.write(",");
						fw.write(Integer.toString((int) match[2]));
						fw.write(",");
						fw.write(Integer.toString((int) match[3]));
						matchedExperience.put((String) match[1], new Integer[] { (int) match[2], (int) match[3] });
						matched++;
					}
					fw.write("\n");
				}
				L.info("{}, {} O2B: {}/{}", new Object[] { experiment, fname, matched, en.getValue().size() });
			}
		}

		// Writing to the output2benchmark file
		try (FileWriter fw = new FileWriter(benchmark2output, true)) {
			int matched = 0;
			int all = 0;
			String fname = null;
			for (Object[] row : bench.records()) {
				all++;
				fname = (String) row[0];
				String exp = (String) row[1];
				Integer from = (Integer) row[2];
				Integer to = (Integer) row[3];

				fw.write(fname);
				fw.write(",");
				fw.write(exp);
				fw.write(",");
				fw.write(Integer.toString(from));
				fw.write(",");
				fw.write(Integer.toString(to));
				fw.write(",");

				if (matchedExperience.containsKey(exp)) {
					matched++;
					fw.write("Y");
					fw.write(",");
					Integer[] i = matchedExperience.get(exp);
					fw.write(Integer.toString(i[0]));
					fw.write(",");
					fw.write(Integer.toString(i[1]));
				} else {
					fw.write("N");
					fw.write(",-1");
					fw.write(",-1");
				}
				fw.write("\n");
			}
			L.info("B2O: {}/{}", matched, all);
		}
	}

	public static final void main(String[] args) throws IOException {
		new RunExperiment(args).run();
	}
}
