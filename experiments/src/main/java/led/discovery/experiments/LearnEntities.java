package led.discovery.experiments;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import led.discovery.benchmark.MapUtil;

public class LearnEntities {
	private static final Logger L = LoggerFactory.getLogger(LearnEntities.class);
	private File input;
	private File output;

	public LearnEntities(String[] args) {
		// File of experience entities
		input = new File(args[0]);
		output = new File(args[1]);
	}

	public static final void main(String[] args) throws IOException {
		new LearnEntities(args).run();
	}

	private void run() throws IOException {
		_clean();

		L.info("Loading entities");
		Map<String, Set<String>> entitySourceMap = new HashMap<String, Set<String>>();
		Map<String, Set<String>> sourceEntityMap = new HashMap<String, Set<String>>();
		try (CSVParser parser = new CSVParser(new FileReader(input), CSVFormat.DEFAULT);) {
			Iterator<CSVRecord> it = parser.iterator();
			while (it.hasNext()) {
				CSVRecord r = it.next();
				String entity = r.get(1);
				String source = r.get(0);
				if (!entitySourceMap.containsKey(entity)) {
					entitySourceMap.put(entity, new HashSet<String>());
				}
				entitySourceMap.get(entity).add(source);
				if (!sourceEntityMap.containsKey(source)) {
					sourceEntityMap.put(source, new HashSet<String>());
				}
				sourceEntityMap.get(source).add(entity);
			}
			L.info("{} entities loaded", entitySourceMap.size());
		}
		L.info("Preparing features");
		Map<String, Integer> entityCounts = new HashMap<String, Integer>();
		
		for (Entry<String, Set<String>> n : entitySourceMap.entrySet()) {
			L.info("entity {}", n.getKey());
			entityCounts.put(n.getKey(), n.getValue().size());
			
		}
		entityCounts = MapUtil.sortByValueDesc(entityCounts);
	
		L.info("Writing {} terms", entityCounts.size());
		try (FileWriter fw = new FileWriter(output, true)) {
			for (Entry<String, Integer> entry : entityCounts.entrySet()) {
				fw.write(entry.getKey());
				fw.write(",");
				fw.write(Integer.toString(entry.getValue()));
				fw.write(",");
				fw.write(Integer.toString(entitySourceMap.get(entry.getKey()).size()));
				fw.write("\n");
			}
		}
	}

	private void _clean() {
		if (output != null && output.exists()) {
			output.delete();
		}
	}
}
