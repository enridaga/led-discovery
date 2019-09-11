package led.discovery.experiments;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.stanford.nlp.util.StringUtils;
import led.discovery.benchmark.MapUtil;
import led.discovery.spark.FeaturesFactory;

/**
 * 
 * @author ed4565
 *
 */
public class REDDataset {
	private static final Logger L = LoggerFactory.getLogger(AnalyseComponentsCoverage.class);

	public REDDataset() throws IOException {

	}

	/**
	 * 
	 * @param inputFolderLocation
	 * @param outputFileLocation
	 * @throws IOException
	 */
	public void countWords(String inputFolderLocation, String outputFileLocation) throws IOException {
		File outputFile = new File(outputFileLocation);
		if (outputFile.exists()) {
			outputFile.delete();
		} else {
			outputFile.getParentFile().mkdirs();
		}

		File dir = new File(inputFolderLocation);

		Map<String, Integer> lenmap = new HashMap<String, Integer>();
		int sumlength = 0;
		int items = 0;
		for (File f : dir.listFiles()) {
			L.info("Processing {}", f);
			String text = IOUtils.toString(new FileInputStream(f), StandardCharsets.UTF_8);
			// Get list of terms
			int count = text.split(" ").length;
			items++;
			sumlength += count;
			lenmap.put(f.getName(), count);
		}

		double mean = ((double) sumlength / (double) items);
		lenmap = MapUtil.sortByValue(lenmap);
		int q = items / 4;
		int _q = q;
		int __q = 1;
		int c = 0;
		try (FileWriter fw = new FileWriter(outputFile, true)) {
			for (Entry<String, Integer> i : lenmap.entrySet()) {
				if (c > _q) {
					_q += q;
					__q += 1;
				}
				c++;
				fw.write(i.getKey());
				fw.write(","); // B
				fw.write(Integer.toString(i.getValue()));
				fw.write(","); // C
				fw.write("Q");
				fw.write(Integer.toString(__q));
				fw.write(","); // D mean
				fw.write(Double.toString(mean));
				fw.write(","); // E mean deviation
				fw.write(Double.toString(mean - i.getValue()));
				fw.write("\n");
			}
		}
	}

	/**
	 * 
	 * @param countWordsFile
	 * @param trainingFileList
	 * @param testFileList
	 * @throws IOException
	 */
	public void generateTrainingAndTest(String countWordsFile, String trainingFileList, String testFileList)
			throws IOException {

		if (new File(trainingFileList).exists()) {
			new File(trainingFileList).delete();
		}
		if (new File(testFileList).exists()) {
			new File(testFileList).delete();
		}

		// We keep 500 items for testing and leave the rest for training
		List<String> q1 = new LinkedList<String>();
		List<String> q2 = new LinkedList<String>();
		List<String> q3 = new LinkedList<String>();
		List<String> q4 = new LinkedList<String>();
		try (BufferedReader r = new BufferedReader(new FileReader(countWordsFile))) {
			for (String line; (line = r.readLine()) != null;) {
				String[] d = line.split(",");
				switch (d[2]) {
				case "Q1":
					q1.add(d[0]);
					break;
				case "Q2":
					q2.add(d[0]);
					break;
				case "Q3":
					q3.add(d[0]);
					break;
				default:
					q4.add(d[0]);
					break;
				}
			}
		}
		L.info("{} {} {} {}", new Object[] { q1.size(), q2.size(), q3.size(), q4.size() });
		Collections.shuffle(q1);
		Collections.shuffle(q2);
		Collections.shuffle(q3);
		Collections.shuffle(q4);

		Object[] cs = new Object[] { q1, q2, q3, q4 };

		// Take 125 from each
		try (FileWriter fw = new FileWriter(testFileList, true);
				FileWriter fw2 = new FileWriter(trainingFileList, true)) {
			for (Object o : cs) {
				List<String> l = (List<String>) o;
				L.info("{}", l.size());
				// Write first 125 to test, the rest to training
				int c = 0;
				for (String i : l) {
					if (c < 125) {
						fw.write(i);
						fw.write("\n");
					} else {
						fw2.write(i);
						fw2.write("\n");
					}
					c++;
				}
			}
		}

	}

	public void createTestDataset(String filesDir, String testFileListLocation, String outputFileLocation)
			throws FileNotFoundException, IOException {
		try (BufferedReader r = new BufferedReader(new FileReader(testFileListLocation));
				FileWriter fw = new FileWriter(outputFileLocation, true);
				CSVPrinter writer = new CSVPrinter(fw, CSVFormat.DEFAULT);) {
			for (String line; (line = r.readLine()) != null;) {
				File f = new File(filesDir, line);
				String text = IOUtils.toString(new FileReader(f));
				writer.printRecord("1.0", line, text);
			}
		}
	}

	/**
	 * 
	 * @param inputFileLocation
	 * @param outputFileLocation
	 * @throws IOException
	 */
	public void generateMetadata(String inputFileLocation, String outputFileLocation) throws IOException {

	}

	/**
	 * 
	 * @param trainingSetList
	 * @param dictionary
	 */
	public void analyseDistribution(String filesDir, String trainingSetList, String dictionaryFileLocation,
			String outputFileLocation) throws IOException {
		FeaturesFactory factory = new FeaturesFactory();
		Map<String, Double> dictionary = AnalyseComponentsCoverage.dictionary(new File(dictionaryFileLocation));

		try (BufferedReader r = new BufferedReader(new FileReader(trainingSetList));
				FileWriter fw = new FileWriter(outputFileLocation, true);) {
			for (String line; (line = r.readLine()) != null;) {
				File f = new File(filesDir, line);
				// If listening experiences, skip items not in the training set
				String text = IOUtils.toString(new FileInputStream(f), StandardCharsets.UTF_8);
				String[] terms = StringUtils.join(factory.aterms(text), " ").toLowerCase().split(" ");
				// A - Name
				fw.write(f.getName());
				fw.write(","); // B
				fw.write(Double.toString(AnalyseComponentsCoverage.hits(dictionary, terms)));
				fw.write(","); // C
				fw.write(Double.toString(AnalyseComponentsCoverage.score(dictionary, terms)));
				fw.write(","); // D
				fw.write(Double.toString(AnalyseComponentsCoverage.relevance(dictionary, terms)));
				fw.write("\n"); //
			}
		}
	}

	public static class CountWords {
		public static void main(String[] args) throws IOException {
			new REDDataset().countWords(args[0], args[1]);
		}
	}

	public static class GenerateTrainingAndTest {
		public static void main(String[] args) throws IOException {
			new REDDataset().generateTrainingAndTest(args[0], args[1], args[2]);
		}
	}

	public static class AnalyseDistribution {
		public static void main(String[] args) throws IOException {
			new REDDataset().analyseDistribution(args[0], args[1], args[2], args[3]);
		}
	}

	public static class CreateTestDataset {
		public static void main(String[] args) throws IOException {
			new REDDataset().createTestDataset(args[0], args[1], args[2]);
		}
	}
}
