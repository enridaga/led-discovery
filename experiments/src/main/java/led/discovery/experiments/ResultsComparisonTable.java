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

import led.discovery.benchmark.ExperiencesInSources;

public class ResultsComparisonTable {
	private static final Logger L = LoggerFactory.getLogger(ResultsComparisonTable.class);
	private File dataDir;
	private File experimentsDir;
	private List<String> experiments;
	private File output;

	public ResultsComparisonTable(String[] args) {
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
			//fw.write("experiment,experience,found\n");
			// System.err.println("experiment,population,relevant,retrieved,positives,precision,recall,fmeasure,accuracy,error");
			for (String exp : experiments) {
				File experimentOutput = new File(experimentsDir, exp + ".output.csv");
				try (CSVParser reader = new CSVParser(new FileReader(experimentOutput), CSVFormat.DEFAULT)) {
					Iterator<CSVRecord> it = reader.iterator();
					while (it.hasNext()) {
						CSVRecord r = it.next();
						String experienceId = r.get(7);
						if (!experienceId.equals("-")) {
							StringBuilder sb = new StringBuilder();
							sb.append(exp).append(",").append(experienceId).append(",").append("1").append("\n");
							fw.write(sb.toString());
						}
					}
				}
			}
		}
	}

	private void _clean() throws IOException {
		output.delete();
		output.createNewFile();
	}

	public static final void main(String[] args) throws IOException {
		new ResultsComparisonTable(args).run();
	}
}
