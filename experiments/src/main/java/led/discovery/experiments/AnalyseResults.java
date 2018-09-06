package led.discovery.experiments;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AnalyseResults {
	private static final Logger L = LoggerFactory.getLogger(AnalyseResults.class);
	private File dataDir;
	private File experimentsDir;
	private List<String> experiments;
	private File output;

	public AnalyseResults(String[] args) {
		dataDir = new File(args[0]);
		experimentsDir = new File(dataDir, "experiments");
		output = new File(experimentsDir, args[1]);
		experiments = new ArrayList<String>();
		if (args.length > 2) {
			String[] _ex = args[2].split(",");
			for (String _e : _ex) {
				L.info("Experiment: {}", _e);
				experiments.add(_e.trim());
			}
		} else {
			// Generate all
			File[] outputs = experimentsDir.listFiles(new FilenameFilter() {
				@Override
				public boolean accept(File dir, String name) {
					return name.endsWith(".output.csv");
				}
			});
			for (File f : outputs) {
				String exp = f.getName().replaceAll(".output.csv", "");
				L.info("Experiment: {}", exp);
				experiments.add(exp);
			}
		}
	}

	public void run() throws IOException {

		_clean();

		try (FileWriter fw = new FileWriter(output, true)) {

			fw.write("experiment,population,relevant,retrieved,positives,precision,recall,fmeasure,accuracy,error\n");
			System.err.println(
					"experiment,population,relevant,retrieved,positives,precision,recall,fmeasure,accuracy,error");
			for (String exp : experiments) {
				File experimentOutput = new File(experimentsDir, exp + ".output.csv");
				// Count relevant elements
				int population = 0; // all texts
				int relevant = 0; // all LEDS in the text
				int retrieved = 0; // windows considered TRUE
				int positives = 0; // windows considered TRUE and VALID

				int truePositive = 0;
				int trueNegative = 0;
				int falsePositive = 0;
				int falseNegative = 0;
				try (CSVParser reader = new CSVParser(new FileReader(experimentOutput), CSVFormat.DEFAULT)) {
					Iterator<CSVRecord> it = reader.iterator();
					while (it.hasNext()) {
						population++;
						CSVRecord r = it.next();
						if (r.get(6).equals("Y")) {
							// This text window includes a listening experience
							relevant++;
						}
						if (r.get(5).equals("Y")) {
							// This text window has been found as listening
							// experience
							retrieved++;
						}
						if (r.get(5).equals("Y") && r.get(6).equals("Y")) {
							// This text window has been found as listening
							// experience
							positives++;
							truePositive++;
						}

						if (r.get(5).equals("N") && r.get(6).equals("N")) {
							trueNegative++;
						}
						if (r.get(5).equals("Y") && r.get(6).equals("N")) {
							falsePositive++;
						}
						if (r.get(5).equals("N") && r.get(6).equals("Y")) {
							falseNegative++;
						}
					}
				}
				// precision and accuracy are the same thing
				double precision = retrieved == 0 ? 0 : (double) positives / (double) retrieved;
				double recall = relevant == 0 ? 0 : (double) positives / (double) relevant;
				double fmeasure = 2 * (precision * recall) / (precision + recall);

				double accuracy = (double) (truePositive + trueNegative)
						/ (double) (truePositive + trueNegative + falsePositive + falseNegative);
				double error = 1.0 - accuracy;
				StringBuilder sb = new StringBuilder();
				sb.append(exp).append(",").append(Integer.toString(population)).append(",")
						.append(Integer.toString(relevant)).append(",").append(Integer.toString(retrieved)).append(",")
						.append(Integer.toString(positives)).append(",").append(Double.toString(precision)).append(",")
						.append(Double.toString(recall)).append(",").append(Double.toString(fmeasure)).append(",")
						.append(Double.toString(accuracy)).append(",").append(Double.toString(error)).append("\n");
				String print = sb.toString();
				System.err.println(print);
				fw.write(print);

			}

		}
	}

	private void _clean() throws IOException {
		output.delete();
		output.createNewFile();
	}

	public static final void main(String[] args) throws IOException {
		new AnalyseResults(args).run();
	}
}
