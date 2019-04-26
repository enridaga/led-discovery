package led.discovery.benchmark;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.jsoup.Jsoup;
import org.jsoup.safety.Whitelist;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HumanBenchmarkMaker {
	private Logger log = LoggerFactory.getLogger(HumanBenchmarkMaker.class);

	public HumanBenchmarkMaker() {
	}

	public void annotateAll(String dataDir, String bookmarksCSV) throws IOException {
		File target = new File(bookmarksCSV);
		if (target.exists()) {
			target.delete();
		} else {
			target.getParentFile().mkdirs();
		}
		target.createNewFile();
		// For each file in the sources directory
		File sources = new File(new File(dataDir), "benchmark/sources");
		File data = new File(new File(dataDir), "benchmark/data");
		if (!sources.exists())
			throw new IOException("sources folder does not exists");
		if (!data.exists())
			throw new IOException("data folder does not exists");
		File[] files = sources.listFiles();
		for (File file : files) {
			File dataFile = new File(data, file.getName().replace(".txt", ".csv"));
			if (file.exists() && data.exists()) {
				log.info("Processing {}", file);
				annotate(file.getAbsolutePath(), dataFile.getAbsolutePath(), dataDir, target.getAbsolutePath());
			} else {
				log.error("Failed: {} {}", file, data);
			}
		}

	}

	public void annotate(String source, String csvLEDs, String dataDir, String saveTo) throws IOException {
		// Collect related experiences
		List<String> list = new ArrayList<String>();
		try (BufferedReader br = new BufferedReader(new FileReader(csvLEDs))) {
			String line = "";
			boolean first = true;
			while ((line = br.readLine()) != null) {
				if (first) {
					first = false;
					continue;
				}
				list.add(line.split(",")[0].trim().replace("\"", ""));
			}
		}
		File target = new File(saveTo);
		File sourceFile = new File(source);
		CSVFormat format = CSVFormat.DEFAULT;
		try (FileWriter fw = new FileWriter(target, true); CSVPrinter printer = new CSVPrinter(fw, format)) {

			// Read file
			String content = new String(Files.readAllBytes(sourceFile.toPath()), "UTF-8");
			// For each experience get excerpt and search it in the whole file
			for (String experience : list) {
				List<String> row = new ArrayList<String>();
				log.info("{}: {}", sourceFile.getName(), experience);
				String id = experience.substring(experience.lastIndexOf("/") + 1);
				String excerpt = getExcerpt(new File(dataDir, "experiences/" + id +
					".txt"));
				log.trace("Excerpt:\n{}", excerpt);
				// Read experience
				Bookmark found = ExcerptFinder.find(excerpt, content);
				if (found.getFrom() != -1) {
					log.debug("Found {} at {}", id, found);
				} else {
					log.error("Not Found: {} \n{}", source, excerpt);
				}
				row.add(sourceFile.getName());
				row.add(id);
				row.add(found.toString());
				row.add(Integer.toString(found.getScore()));
				row.add(Double.toString(found.getNormalisedScore()));
				row.add(excerpt);
				row.add(content.substring(found.getFrom(), found.getTo()));
				printer.printRecord(row);
			}
		}
		log.debug("Exiting");
	}

	public String getExcerpt(File experienceFile) throws IOException {
		String excerpt;
		try {
			excerpt = new String(Files.readAllBytes(experienceFile.toPath()), "UTF-8");
		} catch (IOException e) {
			throw e;
		}
		excerpt = Jsoup.clean(excerpt, Whitelist.simpleText()); // ExcerptFinder.removeTags(excerpt);
		// String [] excerpts = excerpt.split("\\.\\.\\.");
		return excerpt;
	}

	public static void main(String[] args) throws IOException {
		if (args.length < 2) {
			System.out.println("arguments:\n <data-folder> <output-file-name>");
		}
		new HumanBenchmarkMaker().annotateAll(args[0], args[1]);
	}

}
