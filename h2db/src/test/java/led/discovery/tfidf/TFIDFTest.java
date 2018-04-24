package led.discovery.tfidf;

import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import led.discovery.db.InMemTermsDatabase;
import led.discovery.db.TermsDatabase;
import led.discovery.nlp.Term;

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
		db.addDocument("d1", Term.buildList("a", "b", "c"));
		tfidf.init();
		log.info("tf is {}", tfidf.tf("d1", Term.build("a", "")));
		Assert.assertTrue(tfidf.tf("d1", Term.build("a", "")) > 0.2);
		Assert.assertTrue(tfidf.tf("d1", Term.build("a", "")) < 0.4);
	}

}
