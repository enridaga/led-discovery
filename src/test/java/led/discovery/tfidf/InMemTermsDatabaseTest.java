package led.discovery.tfidf;

import java.io.IOException;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import led.discovery.utils.Term;

public class InMemTermsDatabaseTest {
	Logger log = LoggerFactory.getLogger(InMemTermsDatabaseTest.class);
	InMemTermsDatabase d;

	@Before
	public void before() {
		d = new InMemTermsDatabase();
	}

	@Test
	public void getDocumentsSize() throws IOException {
		d.addDocument("doc1", Term.buildList("a", "b", "c", "d", "e") );
		Assert.assertTrue("getDocumentsSize", d.countDocuments() == 1);
		Assert.assertTrue("containsDocument(int)", d.containsDocument(0));
		Assert.assertTrue("!containsDocument(int)", !d.containsDocument(1));
		Assert.assertTrue("containsDocument(String)", d.containsDocument("doc1"));
		Assert.assertTrue("!containsDocument(String)", !d.containsDocument("doc2"));
		
		// Add another documents with the same terms
		d.addDocument("doc2", Term.buildList("a", "b", "c", "d", "e" ));
		Assert.assertTrue("getDocumentsSize", d.countDocuments() == 2);
		
		// Add another documents with new terms
		d.addDocument("doc3", Term.buildList("f", "g", "h", "i", "j", "k" ));
		Assert.assertTrue("getDocumentsSize", d.countDocuments() == 3);
	}
	
	@Test
	public void getTermsSize() throws IOException {
		d.addDocument("doc1", Term.buildList("a", "b", "c", "d", "e" ));
		Assert.assertTrue("getTermsSize", d.countTerms() == 5);
		Assert.assertTrue("getTermsSize", d.countTerms() == 5);
		Assert.assertTrue("getTermId exists", d.getTermId(Term.build("a","")) == 0);
		Assert.assertTrue("getTermId not exists", d.getTermId(Term.build("aaaa","")) == -1);

		// Add another documents with the same terms
		d.addDocument("doc2",  Term.buildList( "a", "b", "c", "d", "e" ));
		Assert.assertTrue("getTermsSize", d.countTerms() == 5);

		// Add another documents with new terms
		d.addDocument("doc3",  Term.buildList("f", "g", "h", "i", "j", "k" ));
		Assert.assertTrue("getTermsSize", d.countTerms() == 11);
	}

	@Test
	public void getTermFrequency() throws IOException {
		d.addDocument("doc1",  Term.buildList( "a", "b", "c", "d", "e" ));
		d.addDocument("doc2",  Term.buildList( "a", "a", "a", "d", "e" ));
		d.addDocument("doc3",  Term.buildList( "f", "g", "h", "i", "j", "k" ));
		Assert.assertTrue(d.countTerm(0, 0) == 1);
		Assert.assertTrue(d.countTerm(1, 0) == 3);
	}
	
	@Test
	public void containsTerm() throws IOException {
		d.addDocument("doc1",  Term.buildList("a", "b", "c", "d", "e" ));
		Assert.assertTrue(d.containsTerm( Term.build("a","")));
		Assert.assertTrue(d.containsTerm( Term.build("b","")));
		Assert.assertTrue(d.containsTerm( Term.build("c","")));
		Assert.assertTrue(d.containsTerm( Term.build("d","")));
		Assert.assertTrue(d.containsTerm( Term.build("e","")));
		Assert.assertTrue(!d.containsTerm( Term.build("f","")));
	}

	public void getDocumentIds() throws IOException {
		d.addDocument("doc1",  Term.buildList("a", "b", "c", "d", "e" ));
		Assert.assertTrue(d.getDocumentIds().size() == 1);
		Assert.assertTrue(d.getDocumentIds().contains(0));
	}
	
	public void getTermIds() throws IOException {
		d.addDocument("doc1", Term.buildList("a"));
		Assert.assertTrue(d.getTermIds().size() == 1);
		Assert.assertTrue(d.getTermIds().contains(0));
		Assert.assertTrue(d.getTerms().contains("a"));
	}
}
