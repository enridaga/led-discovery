package led.discovery.benchmark;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

public class ExperiencesInSources {
	Map<String, List<Integer>> starts;
	Map<String, List<Integer>> ends;
	Map<String, List<String>> experiences;
	List<Object[]> records;

	public ExperiencesInSources(File data) throws IOException {
		_init(new FileReader(data));
	}

	public ExperiencesInSources(Reader data) throws IOException {
		_init(data);
	}

	private void _init(Reader read) throws IOException {
		CSVFormat format = CSVFormat.DEFAULT;
		experiences = new HashMap<String, List<String>>();
		records = new ArrayList<Object[]>();
		try (CSVParser reader = new CSVParser(read, format)) {
			Iterator<CSVRecord> it = reader.iterator();
			starts = new HashMap<String, List<Integer>>();
			ends = new HashMap<String, List<Integer>>();
			while (it.hasNext()) {
				CSVRecord r = it.next();
				String source = r.get(0);
				String experience = r.get(1);
				String[] bm = r.get(2).split(":");
				int from = Integer.parseInt(bm[0]);
				int to = Integer.parseInt(bm[1]);

				if (!starts.containsKey(source)) {
					starts.put(source, new ArrayList<Integer>());
				}
				if (!ends.containsKey(source)) {
					ends.put(source, new ArrayList<Integer>());
				}
				if (!experiences.containsKey(source)) {
					experiences.put(source, new ArrayList<String>());
				}
				if (!experiences.containsKey(source)) {
					experiences.put(source, new ArrayList<String>());
				}
				starts.get(source).add(from);
				ends.get(source).add(to);
				experiences.get(source).add(experience);
				records.add(new Object[] { source, experience, from, to });
			}
		}
	}

	public List<Object[]> records() {
		return Collections.unmodifiableList(records);
	}

	public Object[] matches(String source, int from, int to) {
		List<Integer> froms = starts.get(source);
		List<Integer> tos = ends.get(source);
		boolean found = false;
		for (int x = 0; x < froms.size(); x++) {
			if (from >= froms.get(x) && from < tos.get(x)) {
				found = true;
			} else if (to > froms.get(x) && to <= tos.get(x)) {
				found = true;
			} else if (from <= froms.get(x) && to >= tos.get(x)) {
				found = true;
			}
			
			if(found) {
				return new Object[] { source, experiences.get(source).get(x), froms.get(x), tos.get(x) };
			}
		}
		return null;
	}
}
