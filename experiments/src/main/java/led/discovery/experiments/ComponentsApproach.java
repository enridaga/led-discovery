package led.discovery.experiments;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.stanford.nlp.util.StringUtils;
import led.discovery.annotator.window.TextWindow;
import led.discovery.spark.FeaturesFactory;

public class ComponentsApproach {

	private static final Logger L = LoggerFactory.getLogger(RunExperimentGS.class);

	private File dataDir;
	private File goldStandard;
	private File output;
	private String experiment;

	public ComponentsApproach(String[] args) throws IOException {
		L.info("Experiment launched with args: {}", args);
		dataDir = new File(args[0]);
		experiment = args[1];
		goldStandard = new File(dataDir, "evaluation/gold-standard.csv");
		// sourcesDir = new File(dataDir, "benchmark/sources");
		output = new File(dataDir, "experiments/" + experiment + "-ca.output.csv");
	}

	void clean() throws IOException {
		output.delete();
		output.createNewFile();
	}

	void run() throws IOException {
		L.info("Running experiment {}", experiment);
		L.info("Gold standard: {}", goldStandard);
		clean();

		Properties properties = new Properties();
		properties.load(new FileReader(new File(dataDir, "experiments/componentsApproach.properties")));
		FeaturesFactory factory = new FeaturesFactory();
		try (CSVParser reading = new CSVParser(new FileReader(goldStandard), CSVFormat.DEFAULT);
				FileWriter fw = new FileWriter(output, true)) {
			Iterator<CSVRecord> iter = reading.getRecords().iterator();
			while (iter.hasNext()) {
				CSVRecord row = iter.next();
				boolean expected = row.get(0).equals("1.0");
				String source = row.get(1);
				String text = row.get(2);

				String at = StringUtils.join(factory.aterms(text), " ");
				L.info("{}", at);

				boolean passed = true;
				// TODO Implement approach
				
				fw.write(source);
				fw.write(",");
				fw.write(Integer.toString(0));
				fw.write(",");
				fw.write(Integer.toString(text.length()));
				fw.write(",");
				fw.write(Integer.toString(text.length()));
				fw.write(",");
				fw.write(Integer.toString(text.length()));
				fw.write(",");
				if (passed) {
					fw.write("Y");
				} else {
					fw.write("N");
				} 

				L.info("Source: {} :: {} > {}", new Object[] { source, expected, passed });
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

}
