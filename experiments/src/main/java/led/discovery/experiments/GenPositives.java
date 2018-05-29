package led.discovery.experiments;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.util.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GenPositives {
	private static final Logger L = LoggerFactory.getLogger(GenPositives.class);
	private File dataDir;
	private File experiencesDir;
	private File training;
	private File outputDir;

	public GenPositives(String[] args) {
		dataDir = new File(args[0]);
		experiencesDir = new File(dataDir, "experiences");
		outputDir = new File(dataDir, "positives");
		training = new File(dataDir, "evaluation/for-training.csv");
	}

	private void _clean() {
		if (outputDir.exists())
			outputDir.delete();
		outputDir.mkdirs();
	}

	public void run() throws IOException {
		_clean();
		List<String> positives = new ArrayList<String>();
		try (BufferedReader r = new BufferedReader(new FileReader(training))) {
			for (String line; (line = r.readLine()) != null;) {
				positives.add(line);
			}
		}
		for (String p : positives) {
			File source = new File(experiencesDir, p + ".txt");
			File target = new File(outputDir, p + ".txt");
			L.info("Copy {} to {}", source, target);
			IOUtils.copy(source, target);
		}
	}

	public static void main(String[] args) throws IOException {
		new GenPositives(args).run();
	}
}
