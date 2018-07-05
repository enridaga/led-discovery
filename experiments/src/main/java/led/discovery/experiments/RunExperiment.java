package led.discovery.experiments;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.StringUtils;
import led.discovery.annotator.ListeningExperienceAnnotator.ListeningExperienceAnnotation;
import led.discovery.annotator.ListeningExperienceAnnotator.NotListeningExperienceAnnotation;
import led.discovery.annotator.window.TextWindow;
import led.discovery.benchmark.Benchmark;

public class RunExperiment {

	private static final Logger L = LoggerFactory.getLogger(RunExperiment.class);

	private File dataDir;
	private File benchmark;
	private File sourcesDir;
	private File output;
	private Properties properties;
	private String experiment;
	private File[] onSources;

	public RunExperiment(String[] args) throws IOException {
		L.info("Experiment launched with args: {}", args);
		dataDir = new File(args[0]);
		experiment = args[1];
		String experimentProperties = null;
		if (args.length > 2) {
			experimentProperties = args[2];
		}
		benchmark = new File(dataDir, "evaluation/benchmark.csv");
		sourcesDir = new File(dataDir, "benchmark/sources");
		output = new File(dataDir, "experiments/" + experiment + ".output.csv");
		properties = new Properties();
		L.warn("experimentProperties: {}", experimentProperties);
		if (experimentProperties != null && new File(experimentProperties).exists()) {
			try (FileReader r = new FileReader(new File(experimentProperties))) {
				properties.load(r);
			}
		} else {
			properties.load(getClass().getResourceAsStream(experiment +
				".properties"));
		}
		String _onSources = properties.getProperty("led.experiment.sources");
		if (_onSources == null) {
			onSources = sourcesDir.listFiles();
		} else {
			onSources = sourcesDir.listFiles(new FilenameFilter() {
				List<String> names = Arrays.asList(StringUtils.splitOnChar(_onSources, ','));

				@Override
				public boolean accept(File dir, String name) {
					L.info("source {}", name);
					return names.contains(name);
				}

			});
		}
	}

	void clean() throws IOException {
		output.delete();
		output.createNewFile();
	}

	void run() throws IOException {
		L.info("Running experiment {}", experiment);
		L.info("Benchmark: {}", benchmark);
		StringWriter writer = new StringWriter();
		properties.list(new PrintWriter(writer));
		L.info("Properties: \n{}", writer.getBuffer().toString());
		L.info("Output to: {}, {}", output);
		L.info("On Sources: \n{}", onSources);
		if (onSources.length == 0)
			throw new IOException("Sources empty");

		clean();

		Benchmark bench = new Benchmark(benchmark);
		StanfordCoreNLP pipeline = new StanfordCoreNLP(properties);

		try (FileWriter fw = new FileWriter(output, true)) {
			for (File f : onSources) {
				L.info("Source: {}", f.getName());
				Annotation annotation = new Annotation(FileUtils.readFileToString(f, StandardCharsets.UTF_8));
				pipeline.annotate(annotation);
				String fname = f.getName();

				List<TextWindow> passed = annotation.get(ListeningExperienceAnnotation.class);
				List<TextWindow> notPassed = annotation.get(NotListeningExperienceAnnotation.class);
				List<TextWindow> allSorted = new ArrayList<TextWindow>();
				allSorted.addAll(passed);
				allSorted.addAll(notPassed);
				allSorted.sort(TextWindow.Sorter);

				L.info(" {} passed", passed.size());
				L.info(" {} not passed", notPassed.size());
				Iterator<TextWindow> it = allSorted.iterator();
				while (it.hasNext()) {
					TextWindow tw = it.next();
					int from = tw.offsetStart();
					int to = tw.offsetEnd();
					Object[] match = bench.matches(fname, from, to);
					fw.write(fname);
					fw.write(",");
					fw.write(Integer.toString(from));
					fw.write(",");
					fw.write(Integer.toString(to));
					fw.write(",");
					fw.write(Integer.toString(to - from)); 
					fw.write(",");
					fw.write(Integer.toString(tw.size())); 
					fw.write(",");
					if(passed.contains(tw)) {
						fw.write("Y");
					}else if(notPassed.contains(tw)) {
						fw.write("N");
					}else {
						// This should never happen!
						fw.write("E");
					}
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
						
					}
					
					fw.write("\n");
				}
			}
		}
	}

	public static final void main(String[] args) throws IOException {
		new RunExperiment(args).run();
	}
}
