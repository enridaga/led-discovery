package led.discovery.annotator.window;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
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
	private List<TextWindow> passed;
	private List<TextWindow> notPassed;
	private int min;
	private int max;
	private int step = 1;
	private int fifoSize;
	private int generatedCount;

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
		passed = new ArrayList<TextWindow>();
		notPassed = new ArrayList<TextWindow>();
	}

	public void addEvaluator(TextWindowEvaluator observer) {
		observers.add(observer);
	}

	public int generated() {
		return this.generatedCount;
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
				boolean _passed = true;
				for (TextWindowEvaluator wo : observers) {
					if (!wo.pass(w)) {
						_passed = false;
						break;
					}
				}
				if (_passed) {
					log.debug(" - Passed");
					if (passed.size() > 0) {
						// Remove previously added windows included in this one
						Set<TextWindow> remove = new HashSet<TextWindow>();
						for (TextWindow ww : passed) {
							if (w.includes(ww)) {
								remove.add(ww);
							}
						}
						log.trace(" - passed to remove: {}", remove);
						passed.removeAll(remove);
					}
					passed.add(w);
					log.trace(" - collected: {} ", passed);
				} else {
					log.debug(" - Not Passed");
					if (notPassed.size() > 0) {
						// Remove previously added windows included in this one
						Set<TextWindow> remove = new HashSet<TextWindow>();
						for (TextWindow ww : notPassed) {
							if (w.includes(ww)) {
								remove.add(ww);
							}
						}
						log.trace(" - not passed to remove: {}", remove);
						notPassed.removeAll(remove);
					}
					notPassed.add(w);
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

	public List<TextWindow> passed() {
		return Collections.unmodifiableList(passed);
	}

	public List<TextWindow> notPassed() {
		return Collections.unmodifiableList(notPassed);
	}

	public List<TextWindow> allSorted() {
		List<TextWindow> allSorted = new ArrayList<TextWindow>();
		allSorted.addAll(passed);
		allSorted.addAll(notPassed);
		allSorted.sort(TextWindow.Sorter);

		return Collections.unmodifiableList(allSorted);
	}
	
}
