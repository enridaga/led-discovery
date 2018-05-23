package led.discovery.annotator.window;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.stanford.nlp.util.CoreMap;

public class MovingWindow {
	private Logger log = LoggerFactory.getLogger(MovingWindow.class);
	private List<TextWindow> stack;
	private Set<TextWindowEvaluator> observers;
	private List<TextWindow> collected;

	public MovingWindow(int min, int max) {
		this.stack = new ArrayList<TextWindow>();
		this.collected = new ArrayList<TextWindow>();
		this.observers = new HashSet<TextWindowEvaluator>();
		for (int x = min; x <= max; x++) {
			stack.add(new TextWindow(x));
		}
	}

	public void addEvaluator(TextWindowEvaluator observer) {
		observers.add(observer);
	}

	public void move(CoreMap sentence) {
		Set<TextWindow> replace = new HashSet<TextWindow>();
		for (TextWindow w : stack) {
			w.add(sentence);
			if (w.isFull()) {
				log.trace("Window {} sentences", w.size());
				boolean passed = true;
				for (TextWindowEvaluator wo : observers) {
					if (!wo.pass(w)) {
						passed = false;
						break;
					}
				}
				if (passed) {
					collected.add(w);
				}
				replace.add(w);
			}
		}
		for (TextWindow r : replace) {
			stack.set(stack.indexOf(r), new TextWindow(r.size()));
		}
	}

	public List<TextWindow> collected() {
		return Collections.unmodifiableList(collected);
	}
}