package led.discovery.experiments;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.io.IOUtils;
import org.jsoup.Jsoup;
import org.jsoup.safety.Whitelist;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import led.discovery.spark.FeaturesFactory;

public class GoldStandardMeasures {

	private static final Logger L = LoggerFactory.getLogger(GoldStandardMeasures.class);

	private File dataDir;
	private File goldStandard;
	private File redDirectory;
	private File reutersDirectory;

	public GoldStandardMeasures(String[] args) throws IOException {
		L.info("GoldStandardMeasures launched with args: {}", args);
		dataDir = new File(args[0]);
		goldStandard = new File(dataDir, "evaluation/gold-standard.csv");
		redDirectory = new File(dataDir, "negatives-red");
		reutersDirectory = new File(dataDir, "negatives-reuters");
	}

	void clean() throws IOException {
		new File(dataDir, "analysis/gs-positives-terms-freq.csv").delete();
		new File(dataDir, "analysis/gs-negatives-terms-freq.csv").delete();
		new File(dataDir, "analysis/gs-red-terms-freq.csv").delete();
		new File(dataDir, "analysis/gs-reu-terms-freq.csv").delete();
	}

	void run() throws IOException {
		L.info("Gold standard: {}", goldStandard);
		clean();
		Properties properties = new Properties();
		properties.load(new FileReader(new File(dataDir, "experiments/componentsApproach.properties")));
		FeaturesFactory factory = new FeaturesFactory();
		List<String> positiveWords = new ArrayList<String>();
		List<String> negativeWords = new ArrayList<String>();
		L.info("Loading Gold Standard");
		try (CSVParser reading = new CSVParser(new FileReader(goldStandard), CSVFormat.DEFAULT);) {
			Iterator<CSVRecord> iter = reading.getRecords().iterator();
			while (iter.hasNext()) {
				CSVRecord row = iter.next();
				boolean positive = row.get(0).equals("1.0");
				String text = row.get(2);
				// To Lower Case
				String[] at = factory.aterms(Jsoup.clean(text, Whitelist.none()).toLowerCase());
				if (positive) {
					L.trace("P {} + {}", positiveWords.size(), at.length);
					positiveWords.addAll(Arrays.asList(at));
				} else {
					L.trace("N {} + {}", positiveWords.size(), at.length);
					negativeWords.addAll(Arrays.asList(at));
				}
			}
		}

		// Load RED
		L.info("Loading RED");
		List<String> redWords = new ArrayList<String>();
		int redsamples=0;
		for (File f : redDirectory.listFiles()) {
			redsamples++;
			try (FileReader reader = new FileReader(f)) {
				// To Lower Case
				String[] at = factory.aterms(IOUtils.toString(reader).toLowerCase());
				redWords.addAll(Arrays.asList(at));
			}
			if (redWords.size() > positiveWords.size()) {
				break;
			}
		}
		L.info("red samples: {}", redsamples);
		int diff = redWords.size() - negativeWords.size();
		int limit = redWords.size() - diff;
		for (int d = redWords.size(); d > limit; d--) {
			redWords.remove(d - 1);
		}

		// Load Reuters
		L.info("Loading Reuters");
		List<String> reutersWords = new ArrayList<String>();
		int reusamples=0;
		for (File f : reutersDirectory.listFiles()) {
			reusamples++;
			try (FileReader reader = new FileReader(f)) {
				// To Lower Case
				String[] at = factory.aterms(IOUtils.toString(reader).toLowerCase());
				reutersWords.addAll(Arrays.asList(at));
			}
			if (reutersWords.size() > positiveWords.size()) {
				break;
			}
		}
		L.info("reu samples: {}", reusamples);
		diff = reutersWords.size() - negativeWords.size();
		limit = reutersWords.size() - diff;
		for (int d = reutersWords.size(); d > limit; d--) {
			reutersWords.remove(d - 1);
		}

		L.info("positives: {}, negatives: {}, red: {}, reuters: {}",
				new Object[] { positiveWords.size(), negativeWords.size(), redWords.size(), reutersWords.size() });
		L.info("distinct words: positive: {}, negatives: {}, red: {}, reuters: {}",
				new Object[] { setLen(positiveWords), setLen(negativeWords), setLen(redWords), setLen(reutersWords) });

		printTableOfSharedWords(positiveWords, negativeWords, redWords, reutersWords);

		Set<String> set = new HashSet<String>(positiveWords);
		set.addAll(negativeWords);
		set.addAll(redWords);
		List<String> dictionary = new ArrayList<String>();
		dictionary.addAll(set);
		Collections.sort(dictionary, new Comparator<String>() {
			@Override
			public int compare(String o1, String o2) {
				return o1.compareTo(o2);
			}
		});

		Double[] positiveV = termFrequency(positiveWords, dictionary,
				new File(dataDir, "analysis/gs-positives-terms-freq.csv"));
		Double[] negativeV = termFrequency(negativeWords, dictionary,
				new File(dataDir, "analysis/gs-negatives-terms-freq.csv"));
		Double[] redV = termFrequency(redWords, dictionary, new File(dataDir, "analysis/gs-red-terms-freq.csv"));
		Double[] reutersV = termFrequency(reutersWords, dictionary,
				new File(dataDir, "analysis/gs-reuters-terms-freq.csv"));

		L.info("Similarity between positive/red: {}", cosineSimilarity(positiveV, redV));
		L.info("Similarity between positive/reuters: {}", cosineSimilarity(positiveV, reutersV));
		L.info("Similarity between negative/red: {}", cosineSimilarity(negativeV, redV));
		L.info("Similarity between negative/reuters: {}", cosineSimilarity(negativeV, reutersV));
		L.info("Similarity between positive/negative: {}", cosineSimilarity(positiveV, negativeV));
	}

	private void printTableOfSharedWords(List<String> positiveWords, List<String> negativeWords, List<String> redWords,
			List<String> reutersWords) {
		System.out.println("Table of shared words");
		System.out.println("positives\tnegatives\tred\treuters");
		System.out.print("positives\t*\t");
		System.out.print(Double.toString((double)shared(positiveWords, negativeWords) / (double) positiveWords.size()));
		System.out.print("\t");
		System.out.print(Double.toString((double)shared(positiveWords, redWords) / (double) positiveWords.size()));
		System.out.print("\t");
		System.out.print(Double.toString((double)shared(positiveWords, reutersWords) / (double) positiveWords.size()));
		System.out.print("\n");

		System.out.print("negatives\t");
		System.out.print(Double.toString((double)shared(positiveWords, negativeWords) / (double) negativeWords.size()));
		System.out.print("\t*\t");
		System.out.print(Double.toString((double)shared(negativeWords, redWords) / (double) negativeWords.size()));
		System.out.print("\t");
		System.out.print(Double.toString((double)shared(negativeWords, reutersWords) / (double) negativeWords.size()));
		System.out.print("\n");

		System.out.print("red\t");
		System.out.print(Double.toString((double)shared(redWords, positiveWords) / (double) redWords.size()));
		System.out.print("\t");
		System.out.print(Double.toString((double)shared(negativeWords, redWords) / (double) redWords.size()));
		System.out.print("\t*\t");
		System.out.print(Double.toString((double)shared(redWords, reutersWords) / (double) redWords.size()));
		System.out.print("\n");

		System.out.print("reuters\t");
		System.out.print(Double.toString((double)shared(reutersWords, positiveWords) / (double) reutersWords.size()));
		System.out.print("\t");
		System.out.print(Double.toString((double)shared(reutersWords, negativeWords) / (double) reutersWords.size()));
		System.out.print("\t");
		System.out.print(Double.toString((double)shared(reutersWords, redWords) / (double) reutersWords.size()));
		System.out.print("\t*");
		System.out.print("\n");

	}

	private int setLen(List<String> l) {
		return new HashSet<String>(l).size();
	}

	private int shared(List<String> l, List<String> r) {
		HashSet<String> sl = new HashSet<String>(l);
		sl.retainAll(new HashSet<String>(r));
		return sl.size();
	}

	private Double[] termFrequency(List<String> l, List<String> dictionary, File saveInFile) throws IOException {
		List<Double> freq = new ArrayList<Double>(dictionary.size());
		try (FileWriter fw = new FileWriter(saveInFile)) {
			for (String s : dictionary) {
				if (s.startsWith("<"))
					continue;
				int cnt = Collections.frequency(l, s);
				fw.write(Integer.toString(cnt));
				fw.write(",");
				double fre = (double) ((double) cnt) / (double) dictionary.size();
				fw.write(Double.toString(fre));
				freq.add(fre);
				fw.write(",");
				fw.write(s);
				fw.write("\n");
			}
		}
		return freq.toArray(new Double[freq.size()]);
	}

	public static double cosineSimilarity(Double[] vectorA, Double[] vectorB) {
		double dotProduct = 0.0;
		double normA = 0.0;
		double normB = 0.0;
		for (int i = 0; i < vectorA.length; i++) {
			dotProduct += vectorA[i] * vectorB[i];
			normA += Math.pow(vectorA[i], 2);
			normB += Math.pow(vectorB[i], 2);
		}
		return dotProduct / (double) (Math.sqrt(normA) * Math.sqrt(normB));
	}

	public static final void main(String[] args) throws IOException {
		new GoldStandardMeasures(args).run();
	}
}
