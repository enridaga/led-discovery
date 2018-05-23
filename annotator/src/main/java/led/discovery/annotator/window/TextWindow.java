package led.discovery.annotator.window;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import edu.stanford.nlp.ling.CoreAnnotations.CharacterOffsetBeginAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.CharacterOffsetEndAnnotation;
import edu.stanford.nlp.util.CoreMap;

public class TextWindow {
	private int size;
	private List<CoreMap> sentences;

	public TextWindow(int size) {
		this.size = size;
		this.sentences = new ArrayList<CoreMap>();
	}

	public void add(CoreMap sentence) throws IllegalStateException {
		if (isFull()) {
			throw new IllegalStateException();
		}
		sentences.add(sentence);
	}

	public List<CoreMap> sentences() {
		return Collections.unmodifiableList(sentences);
	}

	public boolean isFull() {
		return this.sentences.size() == this.size;
	}

	public CoreMap firstSentence() {
		return sentences.get(0);
	}

	public CoreMap lastSentence() {
		return sentences.get(sentences.size() - 1);
	}
	
	public int offsetStart() {
		return firstSentence().get(CharacterOffsetBeginAnnotation.class);
	}
	
	public int offsetEnd() {
		return lastSentence().get(CharacterOffsetEndAnnotation.class);
	}

	public int size() {
		return this.size;
	}
	
	public boolean includes(TextWindow t) {
		return t.offsetStart() >= offsetStart() && t.offsetEnd() <= offsetEnd();
	}
}