package led.discovery.annotator;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;
import java.util.Properties;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;
import led.discovery.annotator.FilteredEntityAnnotator.FilteredEntityAnnotation;
import led.discovery.annotator.HeatAnnotator.HeatAnnotation;
import led.discovery.annotator.HeatAnnotator.HeatScoreAnnotation;
import led.discovery.annotator.HeatAnnotator.TermAnnotation;
import led.discovery.annotator.ListeningExperienceAnnotator.ListeningExperienceAnnotation;
import led.discovery.annotator.ListeningExperienceAnnotator.NotListeningExperienceAnnotation;
import led.discovery.annotator.evaluators.HybridEvaluator.FilteredEntityUriAnnotation;
import led.discovery.annotator.evaluators.HybridEvaluator.HybridScoreAnnotation;
import led.discovery.annotator.window.TextWindow;

public class FilteredEntityAnnotatorTest {
	public Logger L = LoggerFactory.getLogger(FilteredEntityAnnotatorTest.class);

	@Test
	public void test() throws IOException {

		String text = "June Badeni on readings by 13-year-old Alice Thompson, as recorded in her notebook: 'She has been reading more of Scott and Dickens, is plunging through the novels of George Eliot... has sampled Bulwer Lytton, Thackeray, and Nathaniel Hawthorne.'";

		Properties properties = new Properties();
		properties.load(getClass().getClassLoader().getResourceAsStream("hybrid-method-test.properties"));
		StringWriter writer = new StringWriter();
		properties.list(new PrintWriter(writer));
		L.info("Properties: \n{}", writer.getBuffer().toString());

		StanfordCoreNLP pipeline = new StanfordCoreNLP(properties);
		Annotation annotation = new Annotation(text);
		pipeline.annotate(annotation);

		List<CoreMap> cm = annotation.get(CoreAnnotations.SentencesAnnotation.class);
		for (CoreMap c : cm) {
			L.info("Text: {}", c.get(CoreAnnotations.TextAnnotation.class));
			List<CoreLabel> tokens = c.get(CoreAnnotations.TokensAnnotation.class);
			for (CoreLabel t : tokens) {
				L.info("Token: {}", t);				
				L.info(" - TermAnnotation: {}", t.get(TermAnnotation.class));
				L.info(" - HeatAnnotation: {}", t.get(HeatAnnotation.class));
				L.info(" - HeatScoreAnnotation: {}", t.get(HeatScoreAnnotation.class));
				L.info(" - FilteredEntityUriAnnotation: {}", t.get(FilteredEntityUriAnnotation.class));
				L.info(" - HybridScoreAnnotation: {}", t.get(HybridScoreAnnotation.class));
			}
			
			L.info("(s) HeatAnnotation: {}", c.get(HeatAnnotation.class));
			L.info("(s) HeatScoreAnnotation: {}", c.get(HeatScoreAnnotation.class));
			L.info("(s) HybridScoreAnnotation: {}", c.get(HybridScoreAnnotation.class));
			L.info("(s) FilteredEntityAnnotation: {}", c.get(FilteredEntityAnnotation.class));
		
		}
		
		List<TextWindow> passed = annotation.get(ListeningExperienceAnnotation.class);
		List<TextWindow> notPassed = annotation.get(NotListeningExperienceAnnotation.class);

		TextWindow tw;
		if (passed.size() == 1) {
			L.info("PASSED");
			tw = passed.get(0);
		} else {
			L.info("NOT PASSED");
			tw = notPassed.get(0);
		}
	}
}
