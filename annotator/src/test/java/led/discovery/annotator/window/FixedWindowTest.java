package led.discovery.annotator.window;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FixedWindowTest {
	private Logger log = LoggerFactory.getLogger(FixedWindowTest.class);
	@Rule
	public TestName name = new TestName();

	@Before
	public void before() {
		log.info("{}", name.getMethodName());
	}

	@Test
	public void test() {
		FixedWindow mw = new FixedWindow();
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
		
		log.info(" is 0 : {}", mw.generated());
		mw.flush();
		log.info(" is 1 : {}", mw.generated());
	}
	
	public void collected() {
		FixedWindow mw = new FixedWindow();
		mw.addEvaluator(TestUtil.PassAll);
		mw.move(TestUtil.S1);
		mw.move(TestUtil.S2);
		mw.move(TestUtil.S3);
		mw.move(TestUtil.S4);
		mw.move(TestUtil.S5);
		
		
		// Fixed Window includes all S, however it is empty fif not flushed
		Assert.assertTrue(mw.passed().size() == 0);
		mw.flush();
		Assert.assertTrue(mw.passed().size() == 1);
		log.info("ADD S6");
		mw.move(TestUtil.S6);
		mw.flush();
		Assert.assertTrue(mw.passed().size() == 1);
	}
	

	public void differentSizes() {
		FixedWindow mw = new FixedWindow();
		mw.addEvaluator(TestUtil.PassAll);
		mw.move(TestUtil.S1);
		mw.move(TestUtil.S2);
		mw.move(TestUtil.S3);
		mw.move(TestUtil.S4);
		mw.move(TestUtil.S5);
		Assert.assertTrue(mw.passed().size() == 0);
		mw.flush();
		Assert.assertTrue(mw.passed().size() == 1);
		log.info("ADD S6");
		mw.move(TestUtil.S6);
		mw.flush();
		mw.move(TestUtil.S7);
		mw.move(TestUtil.S8);
		mw.move(TestUtil.S9);
		mw.flush();
		Assert.assertTrue(mw.passed().size() == 2);
	}

}
