package led.discovery.annotator.window;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

public class TextWindowTest {
	@Rule
	public TestName name = new TestName();

	@Test
	public void test() {
		TextWindow w = new TextWindow(1);
		Assert.assertFalse(w.isFull());
		Assert.assertTrue(w.sentences().size() == 0);
		Assert.assertTrue(w.size() == 1);
		w.add(TestUtil.S1);
		Assert.assertTrue(w.isFull());
		Assert.assertTrue(w.sentences().size() == 1);
		Assert.assertTrue(w.size() == 1);

		w = new TextWindow(5);
		Assert.assertFalse(w.isFull());
		Assert.assertTrue(w.sentences().size() == 0);
		Assert.assertTrue(w.size() == 5);

		w.add(TestUtil.S2);
		Assert.assertFalse(w.isFull());
		Assert.assertTrue(w.sentences().size() == 1);
		Assert.assertTrue(w.size() == 5);

		w.add(TestUtil.S3);
		w.add(TestUtil.S4);
		w.add(TestUtil.S5);
		w.add(TestUtil.S6);
		Assert.assertTrue(w.isFull());
		Assert.assertTrue(w.sentences().size() == 5);
		Assert.assertTrue(w.size() == 5);
		Exception e = null;
		try {
			// cannot add more if full
			w.add(TestUtil.S20);
		} catch (IllegalStateException ec) {
			e = ec;
		}
		Assert.assertTrue(e != null);
	}

	@Test
	public void includes1() {
		TextWindow tw1 = new TextWindow(2);
		TextWindow tw2 = new TextWindow(3);
		tw1.add(TestUtil.S1);
		tw1.add(TestUtil.S2);
		tw2.add(TestUtil.S1);
		tw2.add(TestUtil.S2);
		tw2.add(TestUtil.S3);
		Assert.assertTrue(tw2.includes(tw1));
		Assert.assertTrue(!tw1.includes(tw2));
	}

	@Test
	public void includes2() {
		TextWindow tw1 = new TextWindow(1);
		TextWindow tw2 = new TextWindow(3);
		tw1.add(TestUtil.S2);
		tw2.add(TestUtil.S1);
		tw2.add(TestUtil.S2);
		tw2.add(TestUtil.S3);
		Assert.assertTrue(tw2.includes(tw1));
		Assert.assertTrue(!tw1.includes(tw2));
	}

	@Test
	public void includes3() {
		TextWindow tw1 = new TextWindow(3);
		TextWindow tw2 = new TextWindow(3);
		tw1.add(TestUtil.S1);
		tw1.add(TestUtil.S2);
		tw1.add(TestUtil.S3);
		tw2.add(TestUtil.S1);
		tw2.add(TestUtil.S2);
		tw2.add(TestUtil.S3);
		Assert.assertTrue(tw2.includes(tw1));
		Assert.assertTrue(tw1.includes(tw2));
	}

	@Test
	public void notIncludes1() {
		TextWindow tw1 = new TextWindow(3);
		TextWindow tw2 = new TextWindow(3);
		tw1.add(TestUtil.S1);
		tw1.add(TestUtil.S2);
		tw1.add(TestUtil.S3);
		tw2.add(TestUtil.S2);
		tw2.add(TestUtil.S3);
		tw2.add(TestUtil.S4);
		Assert.assertTrue(!tw2.includes(tw1));
		Assert.assertTrue(!tw1.includes(tw2));
	}

	@Test
	public void notIncludes2() {
		TextWindow tw1 = new TextWindow(3);
		TextWindow tw2 = new TextWindow(3);
		tw1.add(TestUtil.S1);
		tw1.add(TestUtil.S2);
		tw1.add(TestUtil.S3);
		tw2.add(TestUtil.S3);
		tw2.add(TestUtil.S4);
		tw2.add(TestUtil.S5);
		Assert.assertTrue(!tw2.includes(tw1));
		Assert.assertTrue(!tw1.includes(tw2));
	}

	@Test
	public void notIncludes3() {
		TextWindow tw1 = new TextWindow(3);
		TextWindow tw2 = new TextWindow(10);
		tw1.add(TestUtil.S1);
		tw1.add(TestUtil.S2);
		tw1.add(TestUtil.S3);

		tw2.add(TestUtil.S4);
		tw2.add(TestUtil.S5);
		tw2.add(TestUtil.S6);
		tw2.add(TestUtil.S7);
		tw2.add(TestUtil.S8);
		tw2.add(TestUtil.S9);
		tw2.add(TestUtil.S10);
		tw2.add(TestUtil.S11);
		tw2.add(TestUtil.S12);
		tw2.add(TestUtil.S13);

		Assert.assertTrue(!tw2.includes(tw1));
		Assert.assertTrue(!tw1.includes(tw2));
	}

	@Test
	public void equalsTest() {
		TextWindow tw1 = new TextWindow(1);
		tw1.add(TestUtil.S1);
		TextWindow tw2 = new TextWindow(1);
		tw2.add(TestUtil.S1);
		Assert.assertTrue(tw1.equals(tw2));
		Assert.assertTrue(tw2.equals(tw1));
		List<Object> l1 = new ArrayList<Object>();
		l1.add(tw1);
		List<Object> l2 = new ArrayList<Object>();
		l2.add(tw2);
		Assert.assertTrue(l1.equals(l2));
		Assert.assertTrue(l2.equals(l1));
		TextWindow tw3 = new TextWindow(1);
		tw3.add(TestUtil.S2);
		List<Object> l3 = new ArrayList<Object>();
		l3.add(tw3);
		Assert.assertTrue(!l2.equals(l3));
	}

	@Test
	public void testSorter0() {
		TextWindow tw1 = new TextWindow(1);
		tw1.add(TestUtil.S1);
		TextWindow tw2 = new TextWindow(1);
		tw2.add(TestUtil.S1);
		
		int zero = TextWindow.Sorter.compare(tw1, tw2);
		Assert.assertTrue(zero == 0);
	}
	
	@Test
	public void testSorterBefore() {
		TextWindow tw1 = new TextWindow(1);
		tw1.add(TestUtil.S1);
		TextWindow tw2 = new TextWindow(1);
		tw2.add(TestUtil.S2);
		
		int before = TextWindow.Sorter.compare(tw1, tw2);
		Assert.assertTrue(before == -1);
	}
	
	@Test
	public void testSorterAfter() {
		TextWindow tw1 = new TextWindow(1);
		tw1.add(TestUtil.S2);
		TextWindow tw2 = new TextWindow(1);
		tw2.add(TestUtil.S1);
		
		int after = TextWindow.Sorter.compare(tw1, tw2);
		Assert.assertTrue(after == 1);
	}
}
