package led.discovery.analysis.entities.spot;

import java.io.IOException;
import java.nio.charset.Charset;

import org.apache.commons.io.IOUtils;
import org.jsoup.Jsoup;
import org.jsoup.safety.Whitelist;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CleanExperienceTest {
	public static final Logger log = LoggerFactory.getLogger(CleanExperienceTest.class);

	@Ignore
	@Test
	public void test() throws IOException {
		String source = "./1437672056241.txt";
		String text = IOUtils.toString(getClass().getClassLoader().getResourceAsStream(source), Charset.forName("UTF-8"));
		log.info("{}: \n-------\n{}\n-------\n", source, text);
		text = Jsoup.clean(text, Whitelist.simpleText());
		// log.info(" - simple: \n-------\n{}\n-------\n", text);
		// text = Jsoup.clean(text, Whitelist.none());
		// log.info(" - none: \n-------\n{}\n-------\n", text);
		// text = Jsoup.clean(text, Whitelist.basic());
		// log.info(" - basic: \n-------\n{}\n-------\n", text);
		// text = Jsoup.clean(text, Whitelist.relaxed());
		// log.info(" - relaxed: \n-------\n{}\n-------\n", text);
	}
}
