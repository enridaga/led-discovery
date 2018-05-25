package led.discovery.experiments;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
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
		output = new File(experimentsDir, "results.csv");
		experiments = new ArrayList<String>();
		String[] _ex = args[1].split(",");
		for (String _e : _ex) {
			experiments.add(_e.trim());
		}
	}

	public void run() throws IOException {

		_clean();

		try (FileWriter fw = new FileWriter(output, true)) {
			fw.write("experiment,relevant,retrieved,positives,precision,recall,fmeasure\n");
			for (String exp : experiments) {
				File o2b = new File(experimentsDir, exp + ".o2b.csv");
				File b2o = new File(experimentsDir, exp + ".b2o.csv");
				// Count relevant elements
				int relevant = 0; // all experiences in the benchmark
				int retrieved = 0;
				// int falseNegatives = 0;
				int positives = 0;
				// int falsePositives = 0;

				try (CSVParser reader = new CSVParser(new FileReader(b2o), CSVFormat.DEFAULT)) {
					Iterator<CSVRecord> it = reader.iterator();
					while (it.hasNext()) {
						relevant++;
						CSVRecord r = it.next();
						if (r.get(4).equals("Y")) {
							positives++;
						}
					}
				}

				try (CSVParser reader = new CSVParser(new FileReader(o2b), CSVFormat.DEFAULT)) {
					Iterator<CSVRecord> it = reader.iterator();
					while (it.hasNext()) {
						retrieved++;
						CSVRecord r = it.next();
						if (r.get(4).equals("Y")) {
							// XXX already counted before. Here it would include
							// duplicate matches
						}
					}
				}
				double precision = (double) positives / (double) retrieved;
				double recall = (double) positives / (double) relevant;
				double fmeasure = (precision * recall) / (precision + recall);

				StringBuilder sb = new StringBuilder();
				sb.append(exp).append(",").append(Integer.toString(relevant)).append(",").append(Integer.toString(retrieved)).append(",").append(Integer.toString(positives)).append(",").append(Double.toString(precision)).append(",").append(Double.toString(recall)).append(",").append(Double.toString(fmeasure)).append("\n");
				fw.write(sb.toString());
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
