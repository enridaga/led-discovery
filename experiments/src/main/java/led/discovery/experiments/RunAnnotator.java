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
import led.discovery.markup.HtmlMarkupMaker;

public class RunAnnotator {

	private static final Logger L = LoggerFactory.getLogger(RunAnnotator.class);

	private File dataDir;
	// private File benchmark;
	private File sourcesDir;
	private File target;
	private Properties properties;
	private String experiment;
	private File[] onSources;

	public RunAnnotator(String[] args) throws IOException {
		L.info("Experiment launched with args: {}", args);
		dataDir = new File(args[0]);
		experiment = args[1];
		String experimentProperties = null;
		experimentProperties = args[2];
		sourcesDir = new File(dataDir, "benchmark/sources");
		target = new File(dataDir, "annotations/" + experiment + "/");
		properties = new Properties();
		L.warn("experimentProperties: {}", experimentProperties);
		try (FileReader r = new FileReader(new File(experimentProperties))) {
			properties.load(r);
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
		if (target.exists() && target.isDirectory()) {
			FileUtils.deleteDirectory(target);
			target.mkdirs();
		}
	}

	void run() throws IOException {
		L.info("Running experiment {}", experiment);
		StringWriter writer = new StringWriter();
		properties.list(new PrintWriter(writer));
		L.info("Properties: \n{}", writer.getBuffer().toString());
		L.info("Output to: {}, {}", target);
		L.info("On Sources: \n{}", onSources);
		if (onSources.length == 0)
			throw new IOException("Sources empty");

		clean();

		StanfordCoreNLP pipeline = new StanfordCoreNLP(properties);
		HtmlMarkupMaker maker = new HtmlMarkupMaker();

		for (File f : onSources) {
			File output = new File(target, f.getName() + ".html");
//			output.mkdirs();
			output.createNewFile();
			try (FileWriter fw = new FileWriter(output)) {
				L.info("Source: {}", f.getName());
				String html = maker.html(FileUtils.readFileToString(f, StandardCharsets.UTF_8), pipeline);
				fw.write(html);
			}
		}
	}

	public static final void main(String[] args) throws IOException {
		new RunAnnotator(args).run();
	}
}
