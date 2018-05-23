package led.discovery.annotator.window;

import org.junit.Assert;
import org.junit.Test;

import edu.stanford.nlp.ling.CoreLabel;
import led.discovery.annotator.window.TextWindow;

public class WindowTest {

	@Test
	public void test() {
		TextWindow w = new TextWindow(1);
		Assert.assertFalse(w.isFull());
		Assert.assertTrue(w.sentences().size() == 0);
		Assert.assertTrue(w.size() == 1);
		w.add(new CoreLabel());
		Assert.assertTrue(w.isFull());
		Assert.assertTrue(w.sentences().size() == 1);
		Assert.assertTrue(w.size() == 1);

		w = new TextWindow(5);
		Assert.assertFalse(w.isFull());
		Assert.assertTrue(w.sentences().size() == 0);
		Assert.assertTrue(w.size() == 5);

		w.add(new CoreLabel());
		Assert.assertFalse(w.isFull());
		Assert.assertTrue(w.sentences().size() == 1);
		Assert.assertTrue(w.size() == 5);

		w.add(new CoreLabel());
		w.add(new CoreLabel());
		w.add(new CoreLabel());
		w.add(new CoreLabel());
		Assert.assertTrue(w.isFull());
		Assert.assertTrue(w.sentences().size() == 5);
		Assert.assertTrue(w.size() == 5);
		Exception e = null;

		try {
			// cannot add more if full
			w.add(new CoreLabel());
		} catch (IllegalStateException ec) {
			e = ec;
		}
		Assert.assertTrue(e != null);

	}
}
