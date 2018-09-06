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
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FalseNegativesSnippetsTable {
	private static final Logger L = LoggerFactory.getLogger(FalseNegativesSnippetsTable.class);
	private File dataDir;
	private File experimentsDir;
	private List<String> experiments;
	private File output;

	public FalseNegativesSnippetsTable(String[] args) {
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
		Map<String, String> experiences = new HashMap<String, String>();
		Map<String, Set<String>> falseNegatives = new HashMap<String, Set<String>>();
		for (String exp : experiments) {
			File experimentOutput = new File(experimentsDir, exp + ".output.csv");
			try (CSVParser reader = new CSVParser(new FileReader(experimentOutput), CSVFormat.DEFAULT)) {
				Iterator<CSVRecord> it = reader.iterator();
				while (it.hasNext()) {
					CSVRecord r = it.next();
					String experienceId = r.get(7);
					if (!experienceId.equals("-")) {
						if (r.get(5).equals("N") && r.get(6).equals("Y")) {
							File ef = new File(dataDir, "experiences/" + experienceId + ".txt");
							if (!falseNegatives.containsKey(exp)) {
								falseNegatives.put(exp, new HashSet<String>());
							}
							if (ef.exists()) {
								if (!experiences.containsKey(experienceId)) {
									experiences.put(experienceId, IOUtils.toString(new FileReader(ef)));
								}
								falseNegatives.get(exp).add(experiences.get(experienceId));
							} else {
								// XXX Use source id as this comes from -gs experiments
								falseNegatives.get(exp).add(r.get(0));
							}
						}
					}
				}
			}
		}
		try (FileWriter fw = new FileWriter(output,false);
				CSVPrinter csvPrinter = new CSVPrinter(fw, CSVFormat.DEFAULT)) {
			for (Entry<String, Set<String>> en : falseNegatives.entrySet()) {
				for (String txt : en.getValue()) {
					csvPrinter.printRecord(en.getKey(), txt);
				}

			}
		}
	}

	private void _clean() throws IOException {
		if (output.exists()) {
			output.delete();
		}
		L.info("output: {} ({})", output, output.exists());
		output.createNewFile();
	}

	public static final void main(String[] args) throws IOException {
		new FalseNegativesSnippetsTable(args).run();
	}
}
