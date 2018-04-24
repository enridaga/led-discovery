package led.discovery.nlp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LemmaCleanerTest {
	Logger log = LoggerFactory.getLogger(LemmaCleanerTest.class);
	LemmaCleaner cl = new StandardLemmaCleaner();

	@Test
	public void whenHashtag() {
		Assert.assertTrue(cl.clean("#ALFRED").equals("ALFRED"));
	}

	@Test
	public void whenNumber() {
		Assert.assertTrue(cl.clean("3,123").equals("<NUMBER>"));
		Assert.assertTrue(cl.clean("3.123").equals("<NUMBER>"));
		Assert.assertTrue(cl.clean("+1").equals("<NUMBER>"));
		Assert.assertTrue(cl.clean("0").equals("<NUMBER>"));
		Assert.assertTrue(cl.clean("-3.3").equals("<NUMBER>"));
		Assert.assertTrue(cl.clean(",123").equals("<NUMBER>"));
		Assert.assertTrue(cl.clean("123").equals("<NUMBER>"));
		Assert.assertTrue(cl.clean("03,123").equals("<NUMBER>"));
	}

	@Test
	public void tooManyNonAlphanumeric() {
		Assert.assertNull(cl.clean("3,1,23"));
	}

	@Test
	public void tabsAndNewlines() {
		Assert.assertTrue(cl.clean("abc	def").equals("abc def"));
		Assert.assertTrue(cl.clean("abc\n" + "def").equals("abc def"));
	}

	@Test
	public void unchanged() {
		Assert.assertTrue(cl.clean("abc,def").equals("abc,def"));
	}

	@Test
	public void test() throws IOException {
		InputStream is = getClass().getClassLoader().getResourceAsStream("led-garbage.txt");
		int countOK = 0;
		try (BufferedReader br = new BufferedReader(new InputStreamReader(is))) {
			String line;
			while ((line = br.readLine()) != null) {
				// process the line.
				String cleaned = cl.clean(line);
				if (cleaned == null) {
					log.trace("BAD [{}]", line);
				} else {
					log.trace("OK [{}] > [{}]", line, cleaned);
					countOK++;
				}
			}

		}
		log.info("{} are OK (629818 expected)", countOK);
		Assert.assertTrue(countOK == 629818);
	}
}
