package led.discovery.benchmark;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.text.translate.CsvTranslators.CsvEscaper;
import org.apache.logging.log4j.core.util.IOUtils;

public class GenerateGoldStandard {
	private File dataFolder;
	private File experiencesFolder;
	private File goldStandardMakingFile;
	private File outputFile;

	public GenerateGoldStandard(String[] args) {
		dataFolder = new File(args[0]);
		experiencesFolder = new File(dataFolder, "experiences/");
		outputFile = new File(dataFolder, "evaluation/gold-standard.csv");
		goldStandardMakingFile = new File(dataFolder, "benchmark/gold-standard-making.csv");
	}

	void _clean() {
		if (outputFile.exists()) {
			outputFile.delete();
		}
	}

	void run() throws FileNotFoundException, IOException {

		try (CSVParser parser = new CSVParser(new FileReader(goldStandardMakingFile), CSVFormat.DEFAULT);
				FileWriter fw = new FileWriter(outputFile)) {
			Iterator<CSVRecord> iter = parser.iterator();
			int x = 0;
			while (iter.hasNext()) {
				CSVRecord r = iter.next();
				x++;
				// Positive example
				fw.write("1.0");
				fw.write(",");
				fw.write(r.get(0));
				fw.write("-");
				fw.write(Integer.toString(x));
				fw.write(",");
				String exId = r.get(1);
				File experienceContent = new File(experiencesFolder, exId + ".txt");
				fw.write(new CsvEscaper().translate(IOUtils.toString(new FileReader(experienceContent))));
				fw.write("\n");
				x++;
				// Negative example
				fw.write("0.0");
				fw.write(",");
				fw.write(r.get(7));
				fw.write("-");
				fw.write(Integer.toString(x));
				fw.write(",");
				String negContent = r.get(8);
				fw.write(new CsvEscaper().translate(negContent));
				fw.write("\n");

			}
		}
	}

	public static void main(String[] args) throws IOException {
		if (args.length < 1) {
			System.out.println("arguments:\n <data-folder>");
		}
		new GenerateGoldStandard(args).run();
	}
}
