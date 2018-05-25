package led.discovery.annotator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.stanford.nlp.ling.CoreAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.Annotator;
import edu.stanford.nlp.util.ArraySet;
import edu.stanford.nlp.util.CoreMap;
import edu.stanford.nlp.util.ErasureUtils;
import led.discovery.annotator.MusicalHeatAnnotator.MusicalHeatAnnotation;
import led.discovery.annotator.MusicalHeatAnnotator.MusicalHeatScoreAnnotation;
import led.discovery.annotator.evaluators.HeatEvaluator;
import led.discovery.annotator.window.MovingWindow;
import led.discovery.annotator.window.TextWindow;

public class ListeningExperienceAnnotator implements Annotator {
	private Logger log = LoggerFactory.getLogger(ListeningExperienceAnnotator.class);

	private int MinWindowLength = 1;
	private int MaxWindowLength = 5;
	private Double heatThreshold;
	private List<String> Evaluators = Arrays.asList(new String[] { "heat" });

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

	public ListeningExperienceAnnotator(String name, Properties props) {
		// Window
		String _windowMin = props.getProperty("custom.led.window.min");
		String _windowMax = props.getProperty("custom.led.window.max");
		if (_windowMin != null) {
			MinWindowLength = Integer.parseInt(_windowMin);
		}
		if (_windowMax != null) {
			MaxWindowLength = Integer.parseInt(_windowMax);
		}

		// Evaluators
		String _evaluators = props.getProperty("custom.led.evaluators");
		if (_evaluators != null) {
			Evaluators = new ArrayList<String>();
			String[] evals = _evaluators.split(",");
			for (String ev : evals) {
				Evaluators.add(ev.trim().toLowerCase());
			}
			Evaluators = Collections.unmodifiableList(Evaluators);
		}

		// Evaluator :: Heat
		String _heatThreshold = props.getProperty("custom.led.heat.threshold");
		if (_heatThreshold == null) {
			heatThreshold = 0.00043;
		} else {
			heatThreshold = Double.valueOf(_heatThreshold);
		}

	}

	@Override
	public void annotate(Annotation annotation) {
		// Get Sentences
		MovingWindow mv = new MovingWindow(MinWindowLength, MaxWindowLength);
		// Evaluators
		HeatEvaluator heat = null;
		if (Evaluators.contains("heat")) {
			// Heat collector
			heat = new HeatEvaluator(heatThreshold);
			mv.addEvaluator(heat);
		}
		log.debug("{} sentences", annotation.get(SentencesAnnotation.class));
		for (CoreMap sentence : annotation.get(SentencesAnnotation.class)) {
			mv.move(sentence);
		}
		log.debug("Annotations collected \n{}", mv.collected().size());
		// Link the windows to start/end sentences
		annotation.set(ListeningExperienceAnnotation.class, mv.collected());
		if (heat != null) {
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
