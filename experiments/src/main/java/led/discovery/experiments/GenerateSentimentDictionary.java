package led.discovery.experiments;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GenerateSentimentDictionary {
	private static final Logger L = LoggerFactory.getLogger(GenerateSentimentDictionary.class);
	private File dataDir;
	private File sentiWordNetFile;
	private File outputFile;

	public GenerateSentimentDictionary(String[] args) {
		dataDir = new File(args[0]);
		sentiWordNetFile = new File(dataDir, "sentiment/SentiWordNet_3.0.0_20130122.txt");
		outputFile = new File(dataDir, "le-components/dictionary-sentiment.csv");
	}

	private void _clean() throws IOException {
		if (outputFile.exists())
			outputFile.delete();
		outputFile.createNewFile();
	}

	public void run() throws IOException {
		L.info("starting");
		_clean();
		Map<String, Double> dictionary;
		dictionary = new HashMap<String, Double>();
		HashMap<String, HashMap<Integer, Double>> termRankedScores = new HashMap<String, HashMap<Integer, Double>>();
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(sentiWordNetFile));
			int row = 0;
			String line;
			while ((line = reader.readLine()) != null) {
				row++;
				if (!line.trim().startsWith("#")) {
					String[] lineArray = line.split("\t");
					String pos = lineArray[0];
					if (lineArray.length != 6) {
						throw new RuntimeException("Wrong line: " + row);
					}

					// Calculate Sentiment Degree of the Synset by summing Pos + Neg
					Double synsetSentimentDegree = Double.parseDouble(lineArray[2]) + Double.parseDouble(lineArray[3]);

					String[] synsetTerms = lineArray[4].split(" ");
					for (String term : synsetTerms) {
						// Get term rank
						String[] termAndRank = term.split("#");
						// XXX Adjust to our POS change a to j
						if(pos.equals("a")) {
							pos = "j";
						}
						String synsetTerm = termAndRank[0] + "[" + pos + "]";

						int synTermRank = Integer.parseInt(termAndRank[1]);
						// Collect synsets scores ordered by rank
						if (!termRankedScores.containsKey(synsetTerm)) {
							termRankedScores.put(synsetTerm, new HashMap<Integer, Double>());
						}

						// Add synset link to synterm
						termRankedScores.get(synsetTerm).put(synTermRank, synsetSentimentDegree);
					}
				}
			}

			// Go through all the terms.
			for (Map.Entry<String, HashMap<Integer, Double>> entry : termRankedScores.entrySet()) {
				String word = entry.getKey();
				Map<Integer, Double> synSetScoreMap = entry.getValue();

				// Calculate weighted average.
				// Score= 1/2*first + 1/3*second + 1/4*third ..... etc.
				// Sum = 1/1 + 1/2 + 1/3 ...
				double score = 0.0;
				double sum = 0.0;
				for (Map.Entry<Integer, Double> setScore : synSetScoreMap.entrySet()) {
					score += setScore.getValue() / (double) setScore.getKey();
					sum += 1.0 / (double) setScore.getKey();
				}
				score /= sum;
				dictionary.put(word, score);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (reader != null) {
				reader.close();
			}
		}
		L.info("Dicitonary size: {}", dictionary.size());
		try (FileWriter fw = new FileWriter(outputFile, true)) {
			for (Entry<String, Double> te : dictionary.entrySet()) {
				fw.write(te.getKey());
				fw.write(",");
				fw.write(Double.toString(te.getValue()));
				fw.write("\n");
			}
		}
	}

	public static void main(String[] args) throws IOException {
		new GenerateSentimentDictionary(args).run();
	}
}
