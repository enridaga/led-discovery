package led.discovery.annotator.evaluators;

import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.util.CoreMap;
import led.discovery.annotator.DBpediaSpotlightAnnotator.DBpediaEntityAnnotation;
import led.discovery.annotator.DBpediaSpotlightAnnotator.EntityLabel;
import led.discovery.annotator.MusicalEntityAnnotator.MusicalEntityAnnotation;
import led.discovery.annotator.MusicalHeatAnnotator.MusicalHeatScoreAnnotation;
import led.discovery.annotator.window.TextWindow;
import led.discovery.nlp.LemmaCleaner;

public class HeatEntityEvaluator extends AbstractTextWindowEvaluator {
	private Double threshold;
	private double maxValueMet = 0.0;
	private double minValueMet = 100.0;
	public final static Double DEFAULT_THRESHOLD = 0.00043;
	private String[] skippos;
	private Logger log = LoggerFactory.getLogger(HeatEvaluator.class);

	public HeatEntityEvaluator(Properties properties, LemmaCleaner cleaner, Set<String> stopwords) {
		super(cleaner, stopwords);
		String _heatThreshold = properties.getProperty("custom.led.heat.threshold");
		String _skippos = properties.getProperty("custom.led.heat.skippos");
		if (_heatThreshold == null) {
			threshold = DEFAULT_THRESHOLD;
		} else {
			threshold = Double.valueOf(_heatThreshold);
		}
		if (_skippos == null) {
			skippos = new String[0];
		} else {
			skippos = _skippos.split(",");
		}

		log.info("threshold: {}", threshold);
	}

	public double getMaxValueMet() {
		return maxValueMet;
	}

	public double getMinValueMet() {
		return minValueMet;
	}

	@Override
	protected Double computeScore(TextWindow w) {
		double score = 0.0;
		String entity;
		int tokens = 0;
		for (CoreMap cm : w.sentences()) {
			List<EntityLabel> entities = cm.get(DBpediaEntityAnnotation.class);
			Set<String> musicEntities = cm.get(MusicalEntityAnnotation.class);
			for (CoreLabel to : cm.get(CoreAnnotations.TokensAnnotation.class)) {
				if (skip(to)) {
					continue;
				}
				if (!Arrays.asList(skippos).contains(to.tag().toLowerCase())
						&& !Arrays.asList(skippos).contains(to.tag().substring(0, 1).toLowerCase())) {
					double ts = to.get(MusicalHeatScoreAnnotation.class);
					log.trace("{} [{}] = {}", new Object[] { to, to.tag(), ts });
					entity = findEntity(to, entities, musicEntities);
					if (entity != null) { // ENTITY BOOST
						if (ts > 0.0)
							ts *= 2.0;
						else
							ts += 1.078270082; // Standard average score from training
						log.trace("{} ====> {}", entity, ts);
					}
					score += ts;
				} // ONLY SOME POS CONTRIBUTE TO INCREASE THE SCORE BUT NON RELEVANT TAGS CONTRIBUTE TO DOWNSIZE THE SCORE
				tokens++;
			}
		}

		double relativeScore = score / (double) tokens;
		log.trace("score {}", Double.toString(relativeScore));
		if (relativeScore > maxValueMet) {
			maxValueMet = relativeScore;
		}
		if (relativeScore < minValueMet) {
			minValueMet = relativeScore;
		}
		return relativeScore;
	}

	@Override
	protected boolean isScoreEnough(Double score) {
		if (score > threshold) {
			return true;
		}
		return false;
	}

	public static final String findEntity(CoreLabel iw, List<EntityLabel> entities, Set<String> musicEntities) {
		int beginPosition = iw.beginPosition();
		for (EntityLabel entity : entities) {
			if (beginPosition == entity.beginPosition() && musicEntities.contains(entity.getUri())) {
				return entity.getUri();
			}
		}
		return null;
	}
}
