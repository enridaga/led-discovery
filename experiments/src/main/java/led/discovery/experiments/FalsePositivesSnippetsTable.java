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

public class FalsePositivesSnippetsTable {
	private static final Logger L = LoggerFactory.getLogger(FalsePositivesSnippetsTable.class);
	private File dataDir;
	private File experimentsDir;
	private List<String> experiments;
	private File output;

	public FalsePositivesSnippetsTable(String[] args) {
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
		Map<String,String> sources = new HashMap<String,String>();
		Map<String, Set<String>> falsePositives = new HashMap<String, Set<String>>();
		for (String exp : experiments) {
			File experimentOutput = new File(experimentsDir, exp + ".output.csv");
			try (CSVParser reader = new CSVParser(new FileReader(experimentOutput), CSVFormat.DEFAULT)) {
				Iterator<CSVRecord> it = reader.iterator();
				while (it.hasNext()) {
					CSVRecord r = it.next();
					String source = r.get(0);
					if (!sources.containsKey(exp)) {
						sources.put(source, IOUtils.toString(new FileReader(new File(dataDir, "benchmark/sources/" + source))));
					}
					if (r.get(5).equals("Y") && r.get(6).equals("N")) {
						if (!falsePositives.containsKey(exp)) {
							falsePositives.put(exp, new HashSet<String>());
						}
						int from = Integer.parseInt(r.get(1));
						int to = Integer.parseInt(r.get(2));
						String txt = sources.get(source).substring(from , to);
						falsePositives.get(exp).add(txt);
					}
				}
			}
		}
		try (FileWriter fw = new FileWriter(output, true); CSVPrinter csvPrinter = new CSVPrinter(fw, CSVFormat.DEFAULT)) {
			for (Entry<String, Set<String>> en : falsePositives.entrySet()) {
				for (String txt : en.getValue()) {
					csvPrinter.printRecord(en.getKey(), txt);
				}
				
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
		new FalsePositivesSnippetsTable(args).run();
	}
}
