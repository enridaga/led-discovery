package led.discovery.annotator.evaluators;

import java.util.ArrayList;
import java.util.List;

import led.discovery.annotator.window.TextWindow;
import led.discovery.annotator.window.TextWindowEvaluator;

public class CascadingEvaluator implements TextWindowEvaluator {

	List<TextWindowEvaluator> stack;

	public CascadingEvaluator() {
		stack = new ArrayList<TextWindowEvaluator>();
	}

	public void add(TextWindowEvaluator ev) {
		stack.add(ev);
	}

	@Override
	public boolean pass(TextWindow w) {
		for (TextWindowEvaluator e : stack) {
			if (e.pass(w))
				return true;
		}
		return false;
	}

	public boolean contains(TextWindowEvaluator eval) {
		return stack.contains(eval);
	}
}
