package led.discovery.io;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.AnnotationPipeline;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;
import led.discovery.annotator.DBpediaSpotlightAnnotator.DBpediaEntityAnnotation;
import led.discovery.annotator.DBpediaSpotlightAnnotator.EntityLabel;
import led.discovery.annotator.ListeningExperienceAnnotator.ListeningExperienceAnnotation;
import led.discovery.annotator.MusicalHeatAnnotator.MusicalHeatScoreAnnotation;
import led.discovery.annotator.evaluators.HeatEntityEvaluator.HeatEntityScoreAnnotation;
import led.discovery.annotator.evaluators.HeatEntityEvaluator.MusicalEntityUriAnnotation;
import led.discovery.annotator.window.TextWindow;

public class RunAnnotator {
	private Logger L = LoggerFactory.getLogger(RunAnnotator.class);
	Properties method;
	String text;
	Annotation annotation;
	AnnotationPipeline pipeline;

	public RunAnnotator(String text) throws IOException {
		Properties properties = new Properties();
		properties.load(getClass().getClassLoader().getResource("heat-entity-music-filtered.properties").openStream());
		this.method = properties;
		this.text = text;
		if (L.isDebugEnabled()) {
			StringWriter writer = new StringWriter();
			method.list(new PrintWriter(writer));
			L.debug("Annotate with properties: \n{}", writer.getBuffer().toString());
		}
		StanfordCoreNLP.clearAnnotatorPool();
		pipeline = new StanfordCoreNLP(method);
	}

	public void annotate() {
		Annotation annotation = new Annotation(text);
		pipeline.annotate(annotation);
		List<TextWindow> twl = annotation.get(ListeningExperienceAnnotation.class);
		for (TextWindow tw : twl) {
			int from = tw.offsetStart();
			int to = tw.offsetEnd();
			double score = tw.getScore(HeatEntityScoreAnnotation.class);
//			Map<String,Double> 
			for (CoreMap cm : tw.sentences()) {
				List<EntityLabel> entities = cm.get(DBpediaEntityAnnotation.class);
				for (CoreLabel tok : cm.get(CoreAnnotations.TokensAnnotation.class)) {

					System.out.print(tok.lemma());
					System.out.print("[");
					System.out.print(tok.tag().toLowerCase().substring(0, 1));
					System.out.print("]");
					if (tok.containsKey(MusicalEntityUriAnnotation.class)) {
						System.out.print("->");
						System.out.print(tok.get(MusicalEntityUriAnnotation.class));
					}
					System.out.print(":");
					Double val = tok.get(HeatEntityScoreAnnotation.class);
					if (val == null)
						val = 0.0;
					
					double ts = tok.get(MusicalHeatScoreAnnotation.class);
					System.out.print(Double.toString(ts));
					System.out.print(":");
					System.out.print(Double.toString(val));
					System.out.print(" ");
				}
			}
		}
	}

}
