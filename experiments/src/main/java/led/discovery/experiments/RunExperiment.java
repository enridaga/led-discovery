package led.discovery.experiments;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RunExperiment {

	private static final Logger L = LoggerFactory.getLogger(RunExperiment.class);

	private File dataDir;
	private File benchmark;
	private File output;
	private Properties properties;
	private String experiment;

	public RunExperiment(String[] args) throws IOException {
		dataDir = new File(args[0]);
		experiment = args[1];
		benchmark = new File(dataDir, "evaluation/benchmark.csv");
		output = new File(dataDir, "experiments/" + experiment + ".csv");
		properties = new Properties();
		properties.load(getClass().getResourceAsStream(experiment +
			".properties"));
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
		L.info("Output: {}", output);
	}

	public static final void main(String[] args) throws IOException {
		new RunExperiment(args).run();
	}
}
