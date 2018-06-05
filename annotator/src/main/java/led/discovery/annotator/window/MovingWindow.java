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
	private static final Logger log = LoggerFactory.getLogger(MovingWindow.class);
	private List<CoreMap> fifo;
	private Set<TextWindowEvaluator> observers;
	private List<TextWindow> collected;
	private int min;
	private int max;
	private int step = 1;
	private int fifoSize;
	protected int generatedCount = 0;

	public MovingWindow(int min, int max) {
		this(min, max, 1);
	}

	public MovingWindow(int min, int max, int step) {
		this.min = min;
		this.max = max;
		this.step = step;
		this.fifoSize = max; // (max - min + 1);
		observers = new HashSet<TextWindowEvaluator>();
		fifo = new ArrayList<CoreMap>();
		collected = new ArrayList<TextWindow>();
	}

	public void addEvaluator(TextWindowEvaluator observer) {
		observers.add(observer);
	}

	public void move(CoreMap sentence) {
		if (fifo.size() == this.fifoSize) {
			// Shift
			for (int s = 0; s < step; s++) {
				this.fifo.remove(0);
			}
		}
		fifo.add(sentence);
		if (fifo.size() == this.fifoSize) {
			List<TextWindow> generated = new ArrayList<TextWindow>();
			for (int x = min; x <= max; x++) {
				generated.addAll(generateWindows(x, fifo, !_first));
			}
			if (_first) {
				_first = false;
			}
			this.generatedCount += generated.size();
			// Evaluate windows
			for (TextWindow w : generated) {
				boolean passed = true;
				for (TextWindowEvaluator wo : observers) {
					if (!wo.pass(w)) {
						passed = false;
						break;
					}
				}
				if (passed) {
					log.trace(" - Passed");
					if (collected.size() > 0) {
						// Remove previously added windows included in this one
						Set<TextWindow> remove = new HashSet<TextWindow>();
						for (TextWindow ww : collected) {
							if (w.includes(ww)) {
								remove.add(ww);
							}
						}
						log.trace(" - to remove: {}", remove);
						collected.removeAll(remove);
					}
					collected.add(w);
					log.trace(" - collected: {} ", collected);
				}
			}
		}
	}

	boolean _first = true;

	// After the first iteration ignore every window not including the last
	// element in the queue
	protected static final List<TextWindow> generateWindows(int windowSize, List<CoreMap> fifo, boolean onlyIncremented) {
		List<TextWindow> windows = new ArrayList<TextWindow>();
		log.trace("{} sentences in queue", fifo.size());
		// Generate all windows of size x
		for (int cursor = 0; cursor + windowSize <= fifo.size(); cursor++) {
			log.trace("cursor {}", cursor);
			if (onlyIncremented && cursor + windowSize != fifo.size())
				continue;
			// Generate windows starting from sentence at cursor until
			// fifo size
			TextWindow tw = new TextWindow(windowSize);
			for (int y = 0; y < windowSize; y++) {
				log.trace("add item at position {}", y + cursor);
				tw.add(fifo.get(y + cursor));
			}
			// Add this if it does not exist yet
			windows.add(tw);
		}
		log.trace("{} windows.", windows.size());
		return windows;
	}

	public List<TextWindow> collected() {
		return Collections.unmodifiableList(collected);
	}
}
