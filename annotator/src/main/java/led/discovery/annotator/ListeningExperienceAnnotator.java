package led.discovery.annotator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.Set;

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
import led.discovery.annotator.MusicalHeatAnnotator.MusicalHeatAnnotation;
import led.discovery.annotator.MusicalHeatAnnotator.MusicalHeatScoreAnnotation;
import led.discovery.annotator.evaluators.HeatEvaluator;
import led.discovery.annotator.evaluators.RandomForestEvaluator;
import led.discovery.annotator.window.MovingWindow;
import led.discovery.annotator.window.TextWindow;
import led.discovery.annotator.window.TextWindowEvaluator;
import led.discovery.nlp.StanfordNLPProvider;

public class ListeningExperienceAnnotator implements Annotator {
	private Logger log = LoggerFactory.getLogger(ListeningExperienceAnnotator.class);

	private int MinWindowLength = 5;
	private int MaxWindowLength = 5;
	private int Step = 5;
	private Properties properties;
	private List<String> Evaluators = Arrays.asList(new String[] { "heat" });
	private List<TextWindowEvaluator> _E = new ArrayList<TextWindowEvaluator>();
	private HeatEvaluator heat = null;
	private StanfordNLPProvider provider;
	/**
	 * Get all the detected listening experiences
	 */
	public final class ListeningExperienceAnnotation implements CoreAnnotation<List<TextWindow>> {
		@Override
		public Class<List<TextWindow>> getType() {
			return ErasureUtils.uncheckedCast(List.class);
		}
	}

	public final class ListeningExperienceStartAnnotation
			implements CoreAnnotation<List<TextWindow>> {
		@Override
		public Class<List<TextWindow>> getType() {
			return ErasureUtils.uncheckedCast(List.class);
		}
	}

	public final class ListeningExperienceEndAnnotation
			implements CoreAnnotation<List<TextWindow>> {
		@Override
		public Class<List<TextWindow>> getType() {
			return ErasureUtils.uncheckedCast(List.class);
		}
	}

	public final class ListeningExperienceWithinAnnotation
			implements CoreAnnotation<List<TextWindow>> {
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

		// Evaluators
		String _evaluators = properties.getProperty("custom.led.evaluators");
		if (_evaluators != null) {
			Evaluators = new ArrayList<String>();
			String[] evals = _evaluators.split(",");
			for (String ev : evals) {
				Evaluators.add(ev.trim().toLowerCase());
			}
			Evaluators = Collections.unmodifiableList(Evaluators);
		}
		
		if (Evaluators.contains("heat")) {
			log.info("heat evaluator");
			heat = new HeatEvaluator(properties);
			_E.add(heat);
		}
		if (Evaluators.contains("forest")) {
			log.info("forest evaluator");
			try {
				_E.add(new RandomForestEvaluator(properties, provider));
			} catch (IOException e) {
				log.error("Cannot craete forest evaluator", e);
			}
		}
	}

	@Override
	public void annotate(Annotation annotation) {
		log.info("annotate");
		// MovingWindow
		MovingWindow mv = new MovingWindow(MinWindowLength, MaxWindowLength, Step);
		// Evaluators
		
		for(TextWindowEvaluator twe:_E) {
			mv.addEvaluator(twe);
		}
		log.info("Moving window starting");
		// Execute
		log.debug("{} sentences", annotation.get(SentencesAnnotation.class));
		for (CoreMap sentence : annotation.get(SentencesAnnotation.class)) {
			log.info("move to {}", sentence.get(CharacterOffsetBeginAnnotation.class));
			mv.move(sentence);
		}
		log.debug("Annotations collected \n{}", mv.collected().size());
		// Link the windows to start/end sentences
		annotation.set(ListeningExperienceAnnotation.class, mv.collected());
		if (Evaluators.contains("heat")) {
			// Heat max value met (used for training treshold)
			annotation.set(HeatMaxValueMetAnnotation.class, heat.getMaxValueMet());
			annotation.set(HeatMinValueMetAnnotation.class, heat.getMinValueMet());
		}
		for (TextWindow tw : mv.collected()) {

			if (tw.firstSentence().get(ListeningExperienceStartAnnotation.class) == null) {
				tw.firstSentence().set(ListeningExperienceStartAnnotation.class, new ArrayList<TextWindow>());
			}
			List<TextWindow> ltw = new ArrayList<TextWindow>(tw.firstSentence().get(ListeningExperienceStartAnnotation.class));
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
		return Collections.unmodifiableSet(new ArraySet<>(ListeningExperienceAnnotation.class, ListeningExperienceStartAnnotation.class, ListeningExperienceEndAnnotation.class, ListeningExperienceWithinAnnotation.class));
	}

	@Override
	public Set<Class<? extends CoreAnnotation>> requires() {
		// Requirements depend on evaluators
		ArraySet<Class<? extends CoreAnnotation<?>>> set = new ArraySet<Class<? extends CoreAnnotation<?>>>();
		if (Evaluators.contains("heat")) {
			set.add(MusicalHeatScoreAnnotation.class);
			set.add(MusicalHeatAnnotation.class);
		}
		return Collections.unmodifiableSet(set);
	};
}
