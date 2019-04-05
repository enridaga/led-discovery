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

public class GoldStandardPrepareForEvaluation {

	private static final Logger L = LoggerFactory.getLogger(GoldStandardPrepareForEvaluation.class);

	private File dataDir;
	private File goldStandard;

	public GoldStandardPrepareForEvaluation(String[] args) throws IOException {
		L.info("GoldStandardPrepareForEvaluation launched with args: {}", args);
		dataDir = new File(args[0]);
		goldStandard = new File(dataDir, "evaluation/gold-standard.csv");
	}

	void clean() throws IOException {
		new File(dataDir, "analysis/gold-standard-for-evaluation.txt").delete();
	}

	void run() throws IOException {
		L.info("Gold standard: {}", goldStandard);
		clean();

		L.info("Loading Gold Standard");
		File saveInFile = new File(dataDir, "analysis/gold-standard-for-evaluation.txt");
		try (FileWriter fw = new FileWriter(saveInFile);	
		CSVParser reading = new CSVParser(new FileReader(goldStandard), CSVFormat.DEFAULT);) {
			Iterator<CSVRecord> iter = reading.getRecords().iterator();
			while (iter.hasNext()) {
				CSVRecord row = iter.next();
				String source = row.get(1);
				String text = row.get(2);
				text = Jsoup.clean(text, Whitelist.none());
				text = text.replaceAll("\\|", " ");
				fw.write(source);
				fw.write("|");
				fw.write(text);
				fw.write("|");
				fw.write(Integer.toString(text.length()));
				fw.write("\n");
			}
		}

	}

	public static final void main(String[] args) throws IOException {
		new GoldStandardPrepareForEvaluation(args).run();
	}
}
