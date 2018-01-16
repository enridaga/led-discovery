package led.discovery.tests;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Properties;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;

public class StanfordNLPTest {
	Logger log = LoggerFactory.getLogger(StanfordNLPTest.class);
	//@Test
	public void generateAndSaveAnnotations() throws IOException, URISyntaxException {
		// creates a StanfordCoreNLP object, with POS tagging, lemmatization,
		// NER, parsing, and coreference resolution
		Properties props = new Properties();
		props.setProperty("annotators", "tokenize, ssplit, pos, lemma, ner, parse, dcoref");
		StanfordCoreNLP pipeline = new StanfordCoreNLP(props);
		
		// Generates annotations
		URL inDirectoryUrl = getClass().getClassLoader().getResource("./experiences");
		
		File outDirectory = new File("nlp");
		if(!outDirectory.exists()) {
			outDirectory.mkdirs();
		}
		for(File file : new File(inDirectoryUrl.toURI()).listFiles()) {			
			// read some text in the text variable
			String text = IOUtils.toString(new FileInputStream(file), "UTF-8");
			log.info("Reading from {}", file);
			// create an empty Annotation just with the given text
			Annotation document = new Annotation(text);

			// run all Annotators on this text
			pipeline.annotate(document);
			String fileName = file.getName();
			File out = new File(outDirectory, fileName);
			log.info("Writing to {}", out);
			PrintStream ps = new PrintStream(new FileOutputStream(out, true));
			pipeline.prettyPrint(document, ps);
		}
	}
//	
//	@Test
//	public void wordList() throws IOException {
//		Properties props = new Properties();
//		props.setProperty("annotators", "tokenize, ssplit, pos, lemma");
//		StanfordCoreNLP pipeline = new StanfordCoreNLP(props);
//		InputStream file = getClass().getClassLoader().getResourceAsStream("./28219.txt");
//		// read some text in the text variable
//		String text = IOUtils.toString(file, "UTF-8");
//		log.info("Reading from {} ({})", file, text.length());
//		// create an empty Annotation just with the given text
//		Annotation document = new Annotation(text);
//
//		// run all Annotators on this text
//		pipeline.annotate(document);
//		 // a CoreMap is essentially a Map that uses class objects as keys and has values with custom types
//		
//		List<CoreMap> sentences = document.get(SentencesAnnotation.class);
//		List<String> lemmas = new ArrayList();
//	    for(CoreMap sentence: sentences) {
//	      // traversing the words in the current sentence
//	      // a CoreLabel is a CoreMap with additional token-specific methods
//	      for (CoreLabel token: sentence.get(TokensAnnotation.class)) {
//	        // this is the text of the token
////	        String word = token.get(TextAnnotation.class);
//	        String lemma = token.getString(LemmaAnnotation.class);
////	        String pos = token.get(PartOfSpeechAnnotation.class);
//	        log.info("{}", lemma);
////	        // this is the NER label of the token
////	        String ne = token.get(NamedEntityTagAnnotation.class);
//	      }
//
////	      // this is the parse tree of the current sentence
////	      Tree tree = sentence.get(TreeAnnotation.class);
////
////	      // this is the Stanford dependency graph of the current sentence
////	      SemanticGraph dependencies = sentence.get(CollapsedCCProcessedDependenciesAnnotation.class);
//	      
//	      
//	    }
////		pipeline.prettyPrint(document, System.err);
//	}
}
