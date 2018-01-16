package led.discovery.tfidf;

import java.io.IOException;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TFIDFTest {
	Logger log = LoggerFactory.getLogger(TFIDFTest.class);

	
	@Test
	public void test() throws IOException {
		TermsDatabase db = new InMemTermsDatabase();
		new TFIDF(db);
	}
	
	@Test
	public void tf() throws IOException {
		TermsDatabase db = new InMemTermsDatabase();
		TFIDF tfidf = new TFIDF(db);
		db.addDocument("d1", new String[] {"a", "b", "c"});
		log.info("tf is {}", tfidf.tf("d1", "a") );
		Assert.assertTrue(tfidf.tf("d1", "a") > 0.2);
		Assert.assertTrue(tfidf.tf("d1", "a") < 0.4);
	}

}
