package led.discovery.analysis.entities;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DBPediaTypes {
	protected static final Logger L = LoggerFactory.getLogger(DBPediaTypes.class);
	private Map<String, Set<String>> dbpediaTypes = null;

	public DBPediaTypes(File instance_types_nt) throws FileNotFoundException, IOException {
		dbpediaTypes = new HashMap<String, Set<String>>();

		try (BufferedReader br = new BufferedReader(new FileReader(instance_types_nt))) {
			String line;
			while ((line = br.readLine()) != null) {
				// process the line.
				if (line.trim().startsWith("#"))
					continue;
				String[] l = line.replaceAll("<|>", "").split(" ");
				// L.trace("{} {} {}", l);
				if (!dbpediaTypes.containsKey(l[0])) {
					dbpediaTypes.put(l[0], new HashSet<String>());
				}
				dbpediaTypes.get(l[0]).add(l[2]);
			}
			L.debug("{} entities loaded");
		}
	}

	public Set<String> types(String entity) {
		return resolveTypes(dbpediaTypes.get(entity));
	}

	private Set<String> resolveTypes(Set<String> ts) {
		if (ts == null) {
			return Collections.emptySet();
		}
		int size = ts.size();
		for (String t : ts) {
			Set<String> _t = dbpediaTypes.get(t);
			if (_t != null)
				ts.addAll(_t);
		}
		if (size == ts.size()) {
			return ts;
		} else {
			return resolveTypes(ts);
		}
	}
}
