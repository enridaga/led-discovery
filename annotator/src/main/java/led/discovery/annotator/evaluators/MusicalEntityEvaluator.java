package led.discovery.annotator.evaluators;

import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.stanford.nlp.util.CoreMap;
import led.discovery.annotator.DBpediaSpotlightAnnotator.DBpediaEntityAnnotation;
import led.discovery.annotator.DBpediaSpotlightAnnotator.EntityLabel;
import led.discovery.annotator.MusicalEntityAnnotator.MusicalEntityAnnotation;
import led.discovery.annotator.window.TextWindow;
import led.discovery.annotator.window.TextWindowEvaluator;

public class MusicalEntityEvaluator implements TextWindowEvaluator {
	private Logger log = LoggerFactory.getLogger(MusicalEntityEvaluator.class);
	public MusicalEntityEvaluator(Properties props) {
		// TODO Auto-generated constructor stub
	}

	@Override
	public boolean pass(TextWindow w) {
		Set<String> musicalEntities = new HashSet<String>();
		int annotations = 0;
		for (CoreMap cm : w.sentences()) {
			Set<String> uris = (Set<String>) cm.get(MusicalEntityAnnotation.class);
			if(log.isDebugEnabled() && uris != null && uris.size() > 0)
				log.debug(" > uris: {}", uris);
			if (uris != null) {
				annotations += ((List<EntityLabel>) cm.get(DBpediaEntityAnnotation.class)).size();
				musicalEntities.addAll(uris);
			}
		}
		w.setScore("MusicalEntities", ((Integer) musicalEntities.size()).doubleValue());
		w.setScore("MusicalEntitiesSentenceNorm", ((Integer) musicalEntities.size()).doubleValue() / w.size());
		w.setScore("MusicalEntitiesAnnotationsNorm", ((Integer) musicalEntities.size()).doubleValue() / annotations);
		return (musicalEntities.size() > 0);
	}

}
