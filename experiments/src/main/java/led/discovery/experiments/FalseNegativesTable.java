package led.discovery.experiments;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FalseNegativesTable {
	private static final Logger L = LoggerFactory.getLogger(FalseNegativesTable.class);
	private File dataDir;
	private File experimentsDir;
	private List<String> experiments;
	private File output;

	public FalseNegativesTable(String[] args) {
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

		Map<String, Set<String>> foundInExperiment = new HashMap<String, Set<String>>();
		for (String exp : experiments) {
			File experimentOutput = new File(experimentsDir, exp + ".output.csv");
			try (CSVParser reader = new CSVParser(new FileReader(experimentOutput), CSVFormat.DEFAULT)) {
				Iterator<CSVRecord> it = reader.iterator();
				while (it.hasNext()) {
					CSVRecord r = it.next();
					String experienceId = r.get(7);
					if (!experienceId.equals("-")) {
						if (r.get(5).equals("N") && r.get(6).equals("Y")) {
							if (!foundInExperiment.containsKey(experienceId)) {
								foundInExperiment.put(experienceId, new HashSet<String>());
							}
							foundInExperiment.get(experienceId).add(exp);
						}
					}
				}
			}
		}
		try (FileWriter fw = new FileWriter(output, true)) {
			for (Entry<String, Set<String>> en : foundInExperiment.entrySet()) {
				StringBuilder sb = new StringBuilder();
				for (String ex : experiments) {
					if (!en.getValue().contains(ex)) {
						// Only write the missing experiments
						sb.append(en.getKey()).append(",").append(ex).append(",").append("1").append("\n");
					}
				}
				fw.write(sb.toString());
			}
		}
	}

	private void _clean() throws IOException {
		if (output.exists()) {
			output.delete();
			output.createNewFile();
		}
	}

	public static final void main(String[] args) throws IOException {
		new FalseNegativesTable(args).run();
	}
}
