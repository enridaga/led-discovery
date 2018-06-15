package led.discovery.annotator.window;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.stanford.nlp.util.CoreMap;

public class MovingWindowTest {
	private Logger log = LoggerFactory.getLogger(MovingWindowTest.class);
	@Rule
	public TestName name = new TestName();

	@Before
	public void before() {
		log.info("{}", name.getMethodName());
	}

	@Test
	public void test() {
		MovingWindow mw = new MovingWindow(1, 1);
		mw.move(TestUtil.sentence(0, 1000));
		mw.move(TestUtil.sentence(1001, 2000));
		mw.move(TestUtil.sentence(2001, 3000));
		mw.move(TestUtil.sentence(3001, 4000));
		mw.move(TestUtil.sentence(4001, 5000));
		mw.move(TestUtil.sentence(5001, 6000));
		mw.move(TestUtil.sentence(6001, 7000));
		mw.move(TestUtil.sentence(7001, 8000));
		mw.move(TestUtil.sentence(8001, 9000));
		mw.move(TestUtil.sentence(10001, 11000));
		mw.move(TestUtil.sentence(12001, 13000));
		mw.move(TestUtil.sentence(13001, 14000));
		mw.move(TestUtil.sentence(15001, 16000));
		mw.move(TestUtil.sentence(17001, 18000));
		mw.move(TestUtil.sentence(18001, 19000));
	}

	@Test
	public void test5() {
		MovingWindow mw = new MovingWindow(1, 5);
		mw.move(TestUtil.sentence(0, 1000));
		mw.move(TestUtil.sentence(1001, 2000));
		mw.move(TestUtil.sentence(2001, 3000));
		mw.move(TestUtil.sentence(3001, 4000));
		mw.move(TestUtil.sentence(4001, 5000));
		mw.move(TestUtil.sentence(5001, 6000));
		mw.move(TestUtil.sentence(6001, 7000));
		mw.move(TestUtil.sentence(7001, 8000));
		mw.move(TestUtil.sentence(8001, 9000));
		mw.move(TestUtil.sentence(10001, 11000));
		mw.move(TestUtil.sentence(12001, 13000));
		mw.move(TestUtil.sentence(13001, 14000));
		mw.move(TestUtil.sentence(15001, 16000));
		mw.move(TestUtil.sentence(17001, 18000));
		mw.move(TestUtil.sentence(18001, 19000));
	}

	@Test
	public void collected() {
		MovingWindow mw = new MovingWindow(1, 5);
		mw.addEvaluator(TestUtil.PassAll);
		mw.move(TestUtil.S1);
		mw.move(TestUtil.S2);
		mw.move(TestUtil.S3);
		mw.move(TestUtil.S4);
		mw.move(TestUtil.S5);
		// Window size 5 includes all windows
		Assert.assertTrue(mw.passed().size() == 1);
		log.info("ADD S6");
		mw.move(TestUtil.S6);
		Assert.assertTrue(mw.passed().size() == 2);
	}

	@Test
	public void testGenerateWindows() {
		windowsOfSize(1, _queue(3), 3);
		windowsOfSize(1, _queue(4), 4);
		windowsOfSize(1, _queue(30), 30);

		windowsOfSize(3, _queue(3), 1);
		windowsOfSize(4, _queue(4), 1);
		windowsOfSize(30, _queue(30), 1);
		//
		//
		windowsOfSize(2, _queue(3), 2);
		windowsOfSize(2, _queue(4), 3);
		windowsOfSize(2, _queue(5), 4);
		windowsOfSize(2, _queue(6), 5);
		windowsOfSize(5, _queue(10), 6);
		windowsOfSize(30, _queue(30), 1);
		windowsOfSize(15, _queue(30), 16);
	}

	@Test
	public void testMoving() {
		MovingWindow mw2;
		mw2 = new MovingWindow(1, 2);
		mw2.addEvaluator(TestUtil.PassAll);
		mw2.move(TestUtil.S1);
		mw2.move(TestUtil.S2);
		Assert.assertTrue(mw2.passed().size() == 1);
		//

		mw2 = new MovingWindow(1, 2, 1);
		mw2.addEvaluator(TestUtil.PassAll);
		mw2.move(TestUtil.S1);
		mw2.move(TestUtil.S2);
		mw2.move(TestUtil.S3);
		mw2.move(TestUtil.S4);
		mw2.move(TestUtil.S5);
		log.info(" ------------ {}", mw2.generated());
		Assert.assertTrue(mw2.generated() == 9);
		Assert.assertTrue(mw2.passed().size() == 4);
		//
		mw2 = new MovingWindow(1, 10);
		mw2.addEvaluator(TestUtil.PassAll);
		List<CoreMap> text = _queue(100);
		int expected = 0;
		for (int q = 1; q <= 10; q++) {
			expected += 100 - q + 1;
		}
		for (CoreMap s : text)
			mw2.move(s);
		Assert.assertTrue(mw2.generated() == expected);

	}

	private List<CoreMap> _queue(int queueSize) {
		List<CoreMap> fifo = new ArrayList<CoreMap>();
		int step = 1000;
		for (int u = 0; u < queueSize * step; u += step + 1) {
			fifo.add(TestUtil.sentence(u, u + step));
		}
		return fifo;
	}

	private void windowsOfSize(int windowSize, List<CoreMap> fifo, int expectedWindows) {
		List<TextWindow> windows = MovingWindow.generateWindows(windowSize, fifo, false);
		log.info("{} windows. {} expected", windows.size(), expectedWindows);
		Assert.assertTrue(windows.size() == expectedWindows);
	}

	@Test
	public void testMovingFixedSize() {
		MovingWindow mw2;
		mw2 = new MovingWindow(1, 1);
		mw2.addEvaluator(TestUtil.PassAll);
		mw2.move(TestUtil.S1);
		mw2.move(TestUtil.S2);
		Assert.assertTrue(mw2.passed().size() == 2);
		//

		mw2 = new MovingWindow(10, 10);
		mw2.addEvaluator(TestUtil.PassAll);
		List<CoreMap> text = _queue(100);
		for (CoreMap s : text)
			mw2.move(s);
		log.info("{}", mw2.generated());
		Assert.assertTrue(mw2.passed().size() == 91);
		//

		mw2 = new MovingWindow(5, 5);
		text = _queue(5);
		mw2.addEvaluator(TestUtil.PassAll);
		for (CoreMap s : text)
			mw2.move(s);
		log.info("{}", mw2.generated());
		Assert.assertTrue(mw2.passed().size() == 1);

		mw2 = new MovingWindow(5, 5);
		text = _queue(6);
		mw2.addEvaluator(TestUtil.PassAll);
		for (CoreMap s : text)
			mw2.move(s);
		log.info("{}", mw2.generated());
		Assert.assertTrue(mw2.passed().size() == 2);
		//
	}

	@Test
	public void testStepFixedSize() {
		MovingWindow mw2;
		mw2 = new MovingWindow(1, 1, 1);
		mw2.addEvaluator(TestUtil.PassAll);
		mw2.move(TestUtil.S1);
		mw2.move(TestUtil.S2);
		Assert.assertTrue(mw2.passed().size() == 2);
		//

		mw2 = new MovingWindow(10, 10, 10);
		mw2.addEvaluator(TestUtil.PassAll);
		List<CoreMap> text = _queue(100);
		for (CoreMap s : text)
			mw2.move(s);
		log.info("{}", mw2.generated());
		Assert.assertTrue(mw2.passed().size() == 10);
		//

		mw2 = new MovingWindow(5, 5, 1);
		text = _queue(5);
		mw2.addEvaluator(TestUtil.PassAll);
		for (CoreMap s : text)
			mw2.move(s);
		log.info("generatedCount {}", mw2.generated());
		Assert.assertTrue(mw2.passed().size() == 1);

		mw2 = new MovingWindow(5, 5, 5);
		text = _queue(6);
		mw2.addEvaluator(TestUtil.PassAll);
		for (CoreMap s : text)
			mw2.move(s);
		log.info("generatedCount {}", mw2.generated());
		Assert.assertTrue(mw2.passed().size() == 1);

		mw2 = new MovingWindow(20, 20, 20);
		text = _queue(20000);
		mw2.addEvaluator(TestUtil.PassAll);
		for (CoreMap s : text)
			mw2.move(s);
		log.info("generatedCount {}", mw2.generated());
		Assert.assertTrue(mw2.passed().size() == 999); // XXX Why?
		//
	}

	@Test
	public void testAllSorted() {
		MovingWindow mw2;
		mw2 = new MovingWindow(1, 1, 1);
		final List<CoreMap> pass = Arrays.asList(new CoreMap[] { TestUtil.S1, TestUtil.S3, TestUtil.S5 });
		mw2.addEvaluator(new TextWindowEvaluator() {
			@Override
			public boolean pass(TextWindow w) {
				return pass.contains(w.firstSentence());
			}
		});
		mw2.move(TestUtil.S1);
		mw2.move(TestUtil.S2);
		mw2.move(TestUtil.S3);
		mw2.move(TestUtil.S4);
		mw2.move(TestUtil.S5);
		mw2.move(TestUtil.S6);
		mw2.move(TestUtil.S7);
		
		Assert.assertTrue(mw2.passed().size() == 3);
		Assert.assertTrue(mw2.notPassed().size() == 4);
		Assert.assertTrue(mw2.allSorted().size() == 7);
		Assert.assertTrue(mw2.generated() == 7);
		
		List<TextWindow> as = mw2.allSorted();
		Assert.assertTrue(as.get(0).firstSentence().equals(TestUtil.S1));
		Assert.assertTrue(as.get(1).firstSentence().equals(TestUtil.S2));
		Assert.assertTrue(as.get(2).firstSentence().equals(TestUtil.S3));
		Assert.assertTrue(as.get(3).firstSentence().equals(TestUtil.S4));
		Assert.assertTrue(as.get(4).firstSentence().equals(TestUtil.S5));
		Assert.assertTrue(as.get(5).firstSentence().equals(TestUtil.S6));
		Assert.assertTrue(as.get(6).firstSentence().equals(TestUtil.S7));
	}
}
