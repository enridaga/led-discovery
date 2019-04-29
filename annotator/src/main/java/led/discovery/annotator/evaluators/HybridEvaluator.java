package led.discovery.annotator.evaluators;

import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.stanford.nlp.ling.CoreAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.util.CoreMap;
import edu.stanford.nlp.util.ErasureUtils;
import led.discovery.annotator.DBpediaSpotlightAnnotator.DBpediaEntityAnnotation;
import led.discovery.annotator.DBpediaSpotlightAnnotator.EntityLabel;
import led.discovery.annotator.FilteredEntityAnnotator.FilteredEntityAnnotation;
import led.discovery.annotator.HeatAnnotator.HeatScoreAnnotation;
import led.discovery.annotator.window.TextWindow;
import led.discovery.nlp.LemmaCleaner;

public class HybridEvaluator extends AbstractTextWindowEvaluator {
	private Double threshold;
	private double maxValueMet = 0.0;
	private double minValueMet = 100.0;
	private Double multiplier;
	private Double standard_score;
	public final static String DEFAULT_THRESHOLD = "0.00043";
	public final static String DEFAULT_STANDARD_SCORE = "1.078270082";
	public final static String DEFAULT_BOOST_MULTIPLIER = "2.0";
	private String[] skippos;
	private Logger log = LoggerFactory.getLogger(HybridEvaluator.class);

	public HybridEvaluator(Properties properties, LemmaCleaner cleaner, Set<String> stopwords) {
		super(cleaner, stopwords);
		
		threshold = Double.parseDouble(properties.getProperty("custom.led.heat.threshold", DEFAULT_THRESHOLD));
		standard_score = Double.parseDouble(properties.getProperty("custom.led.hybrid.standard", DEFAULT_STANDARD_SCORE));
		multiplier = Double.parseDouble(properties.getProperty("custom.led.hybrid.multiplier", DEFAULT_BOOST_MULTIPLIER));
		
		String _skippos = properties.getProperty("custom.led.heat.skippos");
		if (_skippos == null) {
			skippos = new String[0];
		} else {
			skippos = _skippos.split(",");
		}
		log.debug("skippos: {}", skippos);
		log.debug("standard_score: {}", standard_score);
		log.debug("multiplier: {}", multiplier);
		log.debug("threshold: {}", threshold);
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
			Set<String> filteredEntities = cm.get(FilteredEntityAnnotation.class);
			for (CoreLabel to : cm.get(CoreAnnotations.TokensAnnotation.class)) {
				if (skip(to)) {
					continue;
				}
				if (!Arrays.asList(skippos).contains(to.tag().toLowerCase())
						&& !Arrays.asList(skippos).contains(to.tag().substring(0, 1).toLowerCase())) {
					double ts = to.get(HeatScoreAnnotation.class);
					log.trace("{} [{}] = {}", new Object[] { to, to.tag(), ts });
					entity = findEntity(to, entities, filteredEntities);
					if (entity != null) { // ENTITY BOOST
						to.set(FilteredEntityUriAnnotation.class, entity);
						if (ts > 0.0)
							ts *= multiplier;
						else
							ts += standard_score;
						log.trace("{} ====> {}", entity, ts);
					}
					to.set(HybridScoreAnnotation.class, ts);
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
	public final class HybridScoreAnnotation implements CoreAnnotation<Double> {
		@Override
		public Class<Double> getType() {
			return ErasureUtils.uncheckedCast(Double.class);
		}
	}
	public final class FilteredEntityUriAnnotation implements CoreAnnotation<String> {
		@Override
		public Class<String> getType() {
			return ErasureUtils.uncheckedCast(String.class);
		}
	}
}
