package led.discovery.annotator.window;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreAnnotations.CharacterOffsetBeginAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.CharacterOffsetEndAnnotation;
import edu.stanford.nlp.util.CoreMap;

public class TextWindow {
	private int size;
	private List<CoreMap> sentences;
	private Map<Object, Double> scores = new HashMap<Object, Double>();
	public final static Comparator<TextWindow> Sorter = new Comparator<TextWindow>() {
		public int compare(TextWindow o1, TextWindow o2) {
			if (o1.equals(o2)) {
				return 0;
			}
			if (o1.offsetStart() < o2.offsetStart()) {
				return -1;
			} else if (o1.offsetStart() == o2.offsetStart()) {
				if (o1.offsetEnd() < o2.offsetEnd()) {
					return -1;
				}
			}
			return 1;
		};
	};

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
		int tstart = t.offsetStart();
		int start = offsetStart();
		return tstart >= start && t.offsetEnd() <= offsetEnd();
	}

	public String toText() {
		StringBuilder sb = new StringBuilder();
		for (CoreMap m : sentences) {
			String sentenceStr = m.get(CoreAnnotations.TextAnnotation.class);
			sb.append(sentenceStr);
			sb.append(" ");
		}
		return sb.toString();
	}

	public String toString() {
		return new StringBuilder().append(super.toString()).append("[").append(sentences.size()).append("/")
				.append(size).append("]").append(isFull() == true ? "!" : "*").toString();
	}

	public void setScore(Object key, Double score) {
		scores.put(key, score);
	}

	public Double getScore(Object key) {
		return scores.get(key);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof TextWindow) {
			TextWindow to = (TextWindow) obj;
			return to.size == size && to.sentences.equals(sentences);
		}
		return false;
	}
}