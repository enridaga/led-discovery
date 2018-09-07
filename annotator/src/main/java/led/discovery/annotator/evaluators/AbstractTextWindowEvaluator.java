package led.discovery.annotator.evaluators;

import java.util.Set;

import edu.stanford.nlp.ling.CoreAnnotations.LemmaAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.PartOfSpeechAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import led.discovery.annotator.window.TextWindow;
import led.discovery.annotator.window.TextWindowEvaluator;
import led.discovery.nlp.LemmaCleaner;
import led.discovery.nlp.StandardLemmaCleaner;

public abstract class AbstractTextWindowEvaluator implements TextWindowEvaluator {
	private Set<String> stopwords;
	private LemmaCleaner cleaner = new StandardLemmaCleaner();
	
	public AbstractTextWindowEvaluator(LemmaCleaner cleaner, Set<String> stopwords) {
		this.cleaner = cleaner;
		this.stopwords = stopwords;
		
	}
	
	protected boolean skip(CoreLabel token) {
		String lemma = token.getString(LemmaAnnotation.class);
		String pos = token.getString(PartOfSpeechAnnotation.class);
		if (stopwords.contains(lemma)) {
			return true;
		}
		if ((lemma = cleaner.clean(lemma)) != null) {
			return false;
		}
		return true;
	}
	
	@Override
	public abstract boolean pass(TextWindow w);

}
