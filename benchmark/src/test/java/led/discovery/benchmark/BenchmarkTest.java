package led.discovery.benchmark;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.rules.TestName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BenchmarkTest {
	private static final Logger log = LoggerFactory.getLogger(BenchmarkTest.class);
	private static Benchmark benchmark;
	@Rule
	public TestName name = new TestName();

	@Rule
	public TemporaryFolder testFolder = new TemporaryFolder();

	@BeforeClass
	public static void beforeClass() throws IOException {
		Reader r = new StringReader(IOUtils.toString(BenchmarkMakerTest.class.getClassLoader().getResourceAsStream("./benchmark-sample.csv"), "UTF-8"));
		benchmark = new Benchmark(r);
	}

	@Before
	public void before() {
		log.info("{}", name.getMethodName());
	}

	private Integer[] _pos(Object[] ooo) {
		if (ooo == null) {
			return new Integer[] { -1, -1 };
		}
		return new Integer[] { (Integer) ooo[2], (Integer) ooo[3] };
	}

	@Test
	public void testPerfectMatch() {
		Integer[] m = _pos(benchmark.matches("GLOVER.txt", 394609, 395892));
		Assert.assertTrue(m[0] == 394609);
		Assert.assertTrue(m[1] == 395892);
	}

	@Test
	public void testLeftOverlap1() {
		Integer[] m = _pos(benchmark.matches("GLOVER.txt", 390000, 395892));
		Assert.assertTrue(m[0] == 394609);
		Assert.assertTrue(m[1] == 395892);
	}

	@Test
	public void testLeftOverlap2() {
		Integer[] m = _pos(benchmark.matches("GLOVER.txt", 390000, 395000));
		Assert.assertTrue(m[0] == 394609);
		Assert.assertTrue(m[1] == 395892);
	}

	@Test
	public void testRightOverlap() {
		Integer[] m = _pos(benchmark.matches("GLOVER.txt", 394609, 396000));
		Assert.assertTrue(m[0] == 394609);
		Assert.assertTrue(m[1] == 395892);
	}

	@Test
	public void testRight2Overlap() {
		Integer[] m = _pos(benchmark.matches("GLOVER.txt", 395000, 396000));
		Assert.assertTrue(m[0] == 394609);
		Assert.assertTrue(m[1] == 395892);
	}

	@Test
	public void testIncluded() {
		Integer[] m = _pos(benchmark.matches("GLOVER.txt", 395000, 395800));
		Assert.assertTrue(m[0] == 394609);
		Assert.assertTrue(m[1] == 395892);
	}

	@Test
	public void testIncludes() {
		Integer[] m = _pos(benchmark.matches("GLOVER.txt", 390000, 396000));
		Assert.assertTrue(m[0] == 394609);
		Assert.assertTrue(m[1] == 395892);
	}

	@Test
	public void testNoMatch1() {
		Integer[] m = _pos(benchmark.matches("GLOVER.txt", 90000, 100000));
		Assert.assertTrue(m[0] == -1);
		Assert.assertTrue(m[1] == -1);
	}

	@Test
	public void testNoMatch2() {
		Integer[] m = _pos(benchmark.matches("GLOVER.txt", 395892, 394609));
		Assert.assertTrue(m[0] == -1);
		Assert.assertTrue(m[1] == -1);
	}
}
