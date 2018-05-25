package led.discovery.analysis.entities;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import led.discovery.benchmark.MapUtil;

public class GenBagOfEntities {
	private static final Logger L = LoggerFactory.getLogger(GenBagOfEntities.class);
	private File dataDir;
	private File training;
	private File outputBagOfEntities;
	private File outputBagOfTypes;
	private File types;
	//private File ontology;
	private File experiencesEntities;

	public GenBagOfEntities(String[] args) {
		dataDir = new File(args[0]);
		types = new File(dataDir, "dbpedia/instance_types_en.ttl");
		//ontology = new File(dataDir, "dbpedia/dbpedia_2016-10.nt");
		training = new File(dataDir, "evaluation/for-training.csv");
		experiencesEntities = new File(dataDir, "analysis/experiences-entities.csv");
		outputBagOfEntities = new File(dataDir, "analysis/bag-of-entities.csv");
		outputBagOfTypes = new File(dataDir, "analysis/bag-of-types.csv");
	}

	private void _clean() throws IOException {
		outputBagOfEntities.delete();
		outputBagOfEntities.createNewFile();
		outputBagOfTypes.delete();
		outputBagOfTypes.createNewFile();
	}

	void run() throws FileNotFoundException, IOException {

		_clean();

		List<String> experiences = new ArrayList<String>();
		try (BufferedReader r = new BufferedReader(new FileReader(training))) {
			for (String line; (line = r.readLine()) != null;) {
				experiences.add(line);
			}
		}

		Map<String, Integer> entitiesBag = new HashMap<String, Integer>();
		try (BufferedReader r = new BufferedReader(new FileReader(experiencesEntities))) {
			for (String line; (line = r.readLine()) != null;) {
				String[] row = line.split(",");
				String exp = row[0].replaceAll(".txt$", "");
				if (!experiences.contains(exp)) {
					L.debug("Skipping {}", exp);
					// Only exp in training ...
					continue;
				}
				String ent = row[1];
				if (!entitiesBag.containsKey(ent)) {
					entitiesBag.put(ent, 0);
				}
				int increment = entitiesBag.get(ent);
				increment++;
				entitiesBag.put(ent, increment);
			}
		}
		entitiesBag = MapUtil.sortByValueDesc(entitiesBag);
		// Write Bag of Entities
		try (FileWriter fw = new FileWriter(outputBagOfEntities, true)) {
			for (Entry<String, Integer> entity : entitiesBag.entrySet()) {
				L.trace("writing {}", entity);
				fw.write(entity.getKey());
				fw.write(",");
				fw.write(Integer.toString(entity.getValue()));
				fw.write("\n");
			}
		}

		Map<String, Integer> typesBag = new HashMap<String, Integer>();
		try (BufferedReader r = new BufferedReader(new FileReader(types))) {
			for (String line; (line = r.readLine()) != null;) {
				if(line.trim().startsWith("#")) continue;
				//L.trace("{}", line);
				String[] row = line.split(" ");
				
				// Get Types
				String ent = row[0].substring(1, row[0].length() - 1);
				if (!entitiesBag.containsKey(ent)) {
					//L.trace("Skipping {}", ent);
					// Only exp in training ...
					continue;
				}
				String type = row[2].substring(1, row[2].length() - 1);
				if (!typesBag.containsKey(type)) {
					typesBag.put(type, 0);
				}
				int increment = typesBag.get(type);
				// How many experiences with that entity -> type?
				increment += entitiesBag.get(ent);
				typesBag.put(type, increment);
			}
		}
		typesBag = MapUtil.sortByValueDesc(typesBag);
		// Write Bag of Types
		try (FileWriter fw = new FileWriter(outputBagOfTypes, true)) {
			for (Entry<String, Integer> type : typesBag.entrySet()) {
				L.trace("writing {}", type);
				fw.write(type.getKey());
				fw.write(",");
				fw.write(Integer.toString(type.getValue()));
				fw.write("\n");
			}
		}

	}

	public static final void main(String[] args) throws IOException {
		new GenBagOfEntities(args).run();
	}
}
