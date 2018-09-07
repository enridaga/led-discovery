package led.discovery.annotator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.stanford.nlp.ling.CoreAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.CharacterOffsetBeginAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.Annotator;
import edu.stanford.nlp.util.ArraySet;
import edu.stanford.nlp.util.CoreMap;
import edu.stanford.nlp.util.ErasureUtils;
import led.discovery.analysis.entities.spot.SpotlightClient;
import led.discovery.annotator.MusicalHeatAnnotator.MusicalHeatAnnotation;
import led.discovery.annotator.MusicalHeatAnnotator.MusicalHeatScoreAnnotation;
import led.discovery.annotator.evaluators.CascadingEvaluator;
//import led.discovery.annotator.evaluators.EntitiesRandomForestEvaluator;
import led.discovery.annotator.evaluators.HeatEvaluator;
import led.discovery.annotator.evaluators.LedComponentsEvaluator;
import led.discovery.annotator.evaluators.RandomForestEvaluator;
import led.discovery.annotator.window.FixedWindow;
import led.discovery.annotator.window.MovingWindow;
import led.discovery.annotator.window.TextWindow;
import led.discovery.nlp.LemmaCleaner;
import led.discovery.nlp.StandardLemmaCleaner;
import led.discovery.nlp.StanfordNLPProvider;

public class ListeningExperienceAnnotator implements Annotator {
	private Logger log = LoggerFactory.getLogger(ListeningExperienceAnnotator.class);

	private int MinWindowLength = 5;
	private int MaxWindowLength = 5;
	private int Step = 5;
	private Properties properties;
	// private List<String> Evaluators = Arrays.asList(new String[] { "heat" });
	CascadingEvaluator Evaluators;
	// private List<TextWindowEvaluator> _E = new ArrayList<TextWindowEvaluator>();
	private HeatEvaluator heat = null;
	private LedComponentsEvaluator compo = null;
	private StanfordNLPProvider provider;
	private SpotlightClient spotlight = null;

	/**
	 * Get all the detected listening experiences
	 */
	public final class ListeningExperienceAnnotation implements CoreAnnotation<List<TextWindow>> {
		@Override
		public Class<List<TextWindow>> getType() {
			return ErasureUtils.uncheckedCast(List.class);
		}
	}

	public final class NotListeningExperienceAnnotation implements CoreAnnotation<List<TextWindow>> {
		@Override
		public Class<List<TextWindow>> getType() {
			return ErasureUtils.uncheckedCast(List.class);
		}
	}

	public final class ListeningExperienceStartAnnotation implements CoreAnnotation<List<TextWindow>> {
		@Override
		public Class<List<TextWindow>> getType() {
			return ErasureUtils.uncheckedCast(List.class);
		}
	}

	public final class ListeningExperienceEndAnnotation implements CoreAnnotation<List<TextWindow>> {
		@Override
		public Class<List<TextWindow>> getType() {
			return ErasureUtils.uncheckedCast(List.class);
		}
	}

	public final class ListeningExperienceWithinAnnotation implements CoreAnnotation<List<TextWindow>> {
		@Override
		public Class<List<TextWindow>> getType() {
			return ErasureUtils.uncheckedCast(List.class);
		}
	}

	public final class HeatMaxValueMetAnnotation implements CoreAnnotation<Double> {
		@Override
		public Class<Double> getType() {
			return Double.class;
		}
	}

	public final class HeatMinValueMetAnnotation implements CoreAnnotation<Double> {
		@Override
		public Class<Double> getType() {
			return Double.class;
		}
	}

	public ListeningExperienceAnnotator(String name, Properties propes) {
		properties = propes;
		provider = new StanfordNLPProvider();
		// Spotlight
		String _spotlight = properties.getProperty("custom.led.spotlight");
		if (_spotlight != null) {
			spotlight = new SpotlightClient(_spotlight);
		}
		// Window
		String _windowMin = properties.getProperty("custom.led.window.min");
		String _windowMax = properties.getProperty("custom.led.window.max");
		String _windowStep = properties.getProperty("custom.led.window.step");
		if (_windowMin != null) {
			MinWindowLength = Integer.parseInt(_windowMin);
		}
		if (_windowMax != null) {
			MaxWindowLength = Integer.parseInt(_windowMax);
		}
		if (_windowStep != null) {
			Step = Integer.parseInt(_windowStep);
		}
		log.info("max:{} min:{} step:{}", new Object[] { MaxWindowLength, MinWindowLength, Step });

		// Evaluators
		LemmaCleaner cleaner = new StandardLemmaCleaner();
		Set<String> stopwords;
		try {
			stopwords = new HashSet<String>(
					IOUtils.readLines(getClass().getResourceAsStream("/led/discovery/nlp/stopwords.txt"), "UTF-8"));
		} catch (IOException e) {
			log.error("Cannot load stopwords.", e);
			throw new RuntimeException(e);
		}
		Evaluators = new CascadingEvaluator();
		String _evaluators = properties.getProperty("custom.led.evaluators");
		if (_evaluators != null) {
			String[] evals = _evaluators.split(",");
			for (String ev : evals) {

				if (ev.trim().toLowerCase().equals("heat")) {
					log.info("heat evaluator");
					heat = new HeatEvaluator(properties, cleaner, stopwords);
					Evaluators.add(heat);
				}

				if (ev.trim().toLowerCase().equals("compo")) {
					log.info("compo evaluator");
					compo = new LedComponentsEvaluator(properties, cleaner, stopwords);
					Evaluators.add(compo);
				}

				if (ev.trim().toLowerCase().equals("forest")) {
					log.info("forest evaluator");
					try {
						Evaluators.add(new RandomForestEvaluator(properties, provider, spotlight));
					} catch (IOException e) {
						log.error("Cannot craete forest evaluator", e);
					}
				}

			}
			// Evaluators = Collections.unmodifiableList(Evaluators);
		} else {
			// Set default

		}

//		if (Evaluators.contains("heat")) {
//			log.info("heat evaluator");
//			heat = new HeatEvaluator(properties);
//			_E.add(heat);
//		}
//		if (Evaluators.contains("compo")) {
//			log.info("compo evaluator");
//			compo = new LedComponentsEvaluator(properties);
//			_E.add(compo);
//		}
//		if (Evaluators.contains("forest")) {
//			log.info("forest evaluator");
//			try {
//				_E.add(new RandomForestEvaluator(properties, provider, spotlight));
//			} catch (IOException e) {
//				log.error("Cannot craete forest evaluator", e);
//			}
//		}
//		if (Evaluators.contains("entities")) {
//			log.info("entities evaluator");
//			try {
//				_E.add(new EntitiesRandomForestEvaluator(properties, spotlight));
//			} catch (Exception e) {
//				log.error("Cannot craete entities evaluator", e);
//			}
//		}
	}

	@Override
	public void annotate(Annotation annotation) {
		log.debug("annotate");
		MovingWindow mv;
		// MovingWindow
		if (Step == -1) {
			log.debug("Fixed window");
			mv = new FixedWindow();
		} else {
			log.debug("Moving window");
			mv = new MovingWindow(MinWindowLength, MaxWindowLength, Step);
		}
		// Evaluators
		mv.addEvaluator(Evaluators);
		// Execute
		log.trace("{} sentences", annotation.get(SentencesAnnotation.class));
		for (CoreMap sentence : annotation.get(SentencesAnnotation.class)) {
			log.trace("move to {}", sentence.get(CharacterOffsetBeginAnnotation.class));
			mv.move(sentence);
		}
		if (Step == -1) {
			log.trace("Fixed window : produce()");
			((FixedWindow) mv).produce();
		}
		log.debug("Text windows generated \n{}", mv.generated());
		log.debug("Text windows passed \n{}", mv.passed().size());
		log.debug("Text windows not passed \n{}", mv.notPassed().size());
		// Link the windows to start/end sentences
		annotation.set(ListeningExperienceAnnotation.class, mv.passed());
		annotation.set(NotListeningExperienceAnnotation.class, mv.notPassed());
		if (heat != null && Evaluators.contains(heat)) {
			// Heat max value met (used for training treshold)
			annotation.set(HeatMaxValueMetAnnotation.class, heat.getMaxValueMet());
			annotation.set(HeatMinValueMetAnnotation.class, heat.getMinValueMet());
		}
		for (TextWindow tw : mv.passed()) {

			if (tw.firstSentence().get(ListeningExperienceStartAnnotation.class) == null) {
				tw.firstSentence().set(ListeningExperienceStartAnnotation.class, new ArrayList<TextWindow>());
			}
			List<TextWindow> ltw = new ArrayList<TextWindow>(
					tw.firstSentence().get(ListeningExperienceStartAnnotation.class));
			ltw.add(tw);
			tw.firstSentence().set(ListeningExperienceStartAnnotation.class, ltw);

			if (tw.lastSentence().get(ListeningExperienceEndAnnotation.class) == null) {
				tw.lastSentence().set(ListeningExperienceEndAnnotation.class, new ArrayList<TextWindow>());
			}
			ltw = new ArrayList<TextWindow>(tw.lastSentence().get(ListeningExperienceEndAnnotation.class));
			ltw.add(tw);
			tw.lastSentence().set(ListeningExperienceEndAnnotation.class, ltw);

			for (CoreMap cm : tw.sentences()) {
				if (cm.get(ListeningExperienceWithinAnnotation.class) == null) {
					cm.set(ListeningExperienceWithinAnnotation.class, new ArrayList<TextWindow>());
				}
				ltw = new ArrayList<TextWindow>(cm.get(ListeningExperienceWithinAnnotation.class));
				ltw.add(tw);
				cm.set(ListeningExperienceWithinAnnotation.class, ltw);
			}
		}
	}

	@Override
	public Set<Class<? extends CoreAnnotation>> requirementsSatisfied() {
		return Collections.unmodifiableSet(new ArraySet<>(ListeningExperienceAnnotation.class,
				NotListeningExperienceAnnotation.class, ListeningExperienceStartAnnotation.class,
				ListeningExperienceEndAnnotation.class, ListeningExperienceWithinAnnotation.class));
	}

	@Override
	public Set<Class<? extends CoreAnnotation>> requires() {
		// Requirements depend on evaluators
		ArraySet<Class<? extends CoreAnnotation<?>>> set = new ArraySet<Class<? extends CoreAnnotation<?>>>();
		if (heat != null && Evaluators.contains(heat)) {
			set.add(MusicalHeatScoreAnnotation.class);
			set.add(MusicalHeatAnnotation.class);
		}
		return Collections.unmodifiableSet(set);
	}
}
