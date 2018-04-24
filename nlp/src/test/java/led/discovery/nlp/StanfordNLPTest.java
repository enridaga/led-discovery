package led.discovery.nlp;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Properties;

import org.apache.commons.io.IOUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;

public class StanfordNLPTest {
	Logger log = LoggerFactory.getLogger(StanfordNLPTest.class);
	
	@Rule
	public TemporaryFolder folder = new TemporaryFolder();

	@Test
	public void generateAndSaveAnnotations() throws IOException, URISyntaxException {
		// creates a StanfordCoreNLP object, with POS tagging, lemmatization,
		// NER, parsing, and coreference resolution
		Properties props = new Properties();
		props.setProperty("annotators", "tokenize, ssplit, pos, lemma, ner, parse, dcoref");
		StanfordCoreNLP pipeline = new StanfordCoreNLP(props);

		// Generates annotations
		URL inDirectoryUrl = getClass().getClassLoader().getResource("./experiences");

		File outDirectory = folder.newFolder();
		
		if (!outDirectory.exists()) {
			outDirectory.mkdirs();
		}
		for (File file : new File(inDirectoryUrl.toURI()).listFiles()) {
			// read some text in the text variable
			String text = IOUtils.toString(new FileInputStream(file), "UTF-8");
			log.trace("Reading from {}", file);
			// create an empty Annotation just with the given text
			Annotation document = new Annotation(text);

			// run all Annotators on this text
			pipeline.annotate(document);
			String fileName = file.getName();
			File out = new File(outDirectory, fileName);
			log.trace("Writing to {}", out);
			PrintStream ps = new PrintStream(new FileOutputStream(out, true));
			pipeline.prettyPrint(document, ps);
		}
	}
	//
}
