package led.discovery.annotator.window;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.stanford.nlp.ling.CoreLabel;
import led.discovery.annotator.window.MovingWindow;

public class MovingWindowTest {
	private Logger log = LoggerFactory.getLogger(MovingWindowTest.class);

	@Test
	public void test() {
		MovingWindow mw = new MovingWindow(1, 1);
		mw.move(new CoreLabel());
		mw.move(new CoreLabel());
		mw.move(new CoreLabel());
		mw.move(new CoreLabel());
		mw.move(new CoreLabel());
		mw.move(new CoreLabel());
		mw.move(new CoreLabel());
		mw.move(new CoreLabel());
		mw.move(new CoreLabel());
		mw.move(new CoreLabel());
		mw.move(new CoreLabel());
		mw.move(new CoreLabel());
		mw.move(new CoreLabel());
		mw.move(new CoreLabel());
		mw.move(new CoreLabel());
	}
	

	@Test
	public void test5() {
		MovingWindow mw = new MovingWindow(1, 5);
		mw.move(new CoreLabel());
		mw.move(new CoreLabel());
		mw.move(new CoreLabel());
		mw.move(new CoreLabel());
		mw.move(new CoreLabel());
		mw.move(new CoreLabel());
		mw.move(new CoreLabel());
		mw.move(new CoreLabel());
		mw.move(new CoreLabel());
		mw.move(new CoreLabel());
		mw.move(new CoreLabel());
		mw.move(new CoreLabel());
		mw.move(new CoreLabel());
		mw.move(new CoreLabel());
		mw.move(new CoreLabel());
	}
}
