package led.discovery.db;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import org.junit.Assert;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.rules.TestName;
import org.junit.runners.MethodSorters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import led.discovery.db.H2Queries;
import led.discovery.db.H2TermsDatabase;
import led.discovery.db.TermsDatabase;
import led.discovery.nlp.Term;
import led.discovery.tfidf.TFIDF;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class H2TermsDatabaseTest {
	private static final Logger l = LoggerFactory.getLogger(H2TermsDatabaseTest.class);
	
	@Rule
	public TestName name = new TestName();

	@Rule
	public TemporaryFolder testFolder = new TemporaryFolder();

	@Before
	public void before() throws IOException {
		l.debug(">>> {} <<<", name.getMethodName());
		connectionUrl = "jdbc:h2:file:" + testFolder.newFolder().getAbsolutePath() + "/H2Test";
		l.debug("Connection url: {}", connectionUrl);
	}

	private String connectionUrl;
	private String user = "user";
	private String pwd = "pwd234556";

	@Test
	public void test1_connection() throws ClassNotFoundException, SQLException {
		Class.forName("org.h2.Driver");
		Connection conn = DriverManager.getConnection(connectionUrl, user, pwd);

		boolean success = conn.createStatement().execute("CREATE TABLE TEST (ID INT PRIMARY KEY, NAME VARCHAR(255) ) ");
		Assert.assertFalse(success);
		success = conn.createStatement().execute("INSERT INTO TEST (ID,NAME) VALUES (0,'ENRICO')");
		Assert.assertFalse(success);
		java.sql.ResultSet rs = conn.createStatement().executeQuery("SELECT * FROM TEST");
		Assert.assertTrue(rs.next());
		l.debug("Result {} {}", rs.getInt(1), rs.getString(2));
		rs.close();
		conn.close();
	}
	
	@Test
	public void test2_createTables() throws ClassNotFoundException, SQLException {

		Class.forName("org.h2.Driver");
		Connection conn = DriverManager.getConnection(connectionUrl, user, pwd);

		boolean success = conn.createStatement().execute(H2Queries.CREATE_TABLE_DOCUMENTS);
		success = conn.createStatement().execute(H2Queries.CREATE_TABLE_TERMS);
		success = conn.createStatement().execute(H2Queries.CREATE_TABLE_TERM_DOCUMENT);
		Assert.assertFalse(success); // no result set

		// EXECUTING TWICE SHOULD NOT DO ANY HARM
		success = conn.createStatement().execute(H2Queries.CREATE_TABLE_DOCUMENTS);
		success = conn.createStatement().execute(H2Queries.CREATE_TABLE_TERMS);
		success = conn.createStatement().execute(H2Queries.CREATE_TABLE_TERM_DOCUMENT);
		conn.close();
	}
	
	@Test
	public void test3_setup() throws ClassNotFoundException, SQLException, IOException {
		Class.forName("org.h2.Driver");
		String folder = testFolder.newFolder().getAbsolutePath();
		new H2TermsDatabase(new File(folder), "test", user, pwd);
	}
	
	@Test
	public void test4_getDocumentsSize() throws IOException {
		String folder = testFolder.newFolder().getAbsolutePath();
		H2TermsDatabase d = new H2TermsDatabase(new File(folder), "test", user, pwd);
		d.addDocument("doc1", Term.buildList("a", "b", "c", "d", "e" ));
		Assert.assertTrue("getDocumentsSize", d.countDocuments() == 1);
		Assert.assertTrue("containsDocument(int)", d.containsDocumentId(d.getDocId("doc1")));
		Assert.assertTrue("!containsDocument(int)", !d.containsDocumentId(0)); // id 0 does not exists
		Assert.assertTrue("!containsDocument(int)", !d.containsDocumentId(2)); // id 2 does not exists
		Assert.assertTrue("containsDocument(String)", d.containsDocumentId(1)); // 1 is the first id
		Assert.assertTrue("containsDocument(String)", d.containsDocument("doc1"));
		Assert.assertTrue("!containsDocument(String)", !d.containsDocument("doc2"));
		
		// Add another documents with the same terms
		d.addDocument("doc2", Term.buildList( "a", "b", "c", "d", "e" ));
		Assert.assertTrue("getDocumentsSize", d.countDocuments() == 2);
		
		// Add another documents with new terms
		d.addDocument("doc3", Term.buildList( "f", "g", "h", "i", "j", "k" ));
		Assert.assertTrue("getDocumentsSize", d.countDocuments() == 3);
	}
	
	public void getDocumentIds() throws IOException {
		String folder = testFolder.newFolder().getAbsolutePath();
		TermsDatabase d = new H2TermsDatabase(new File(folder), "test", user, pwd);
		d.addDocument("doc1", Term.buildList( "a", "b", "c", "d", "e" ));
		Assert.assertTrue(d.getDocumentIds().size() == 1);
		Assert.assertTrue(d.getDocumentIds().contains(1));
		Assert.assertTrue(!d.getDocumentIds().contains(2));
		Assert.assertTrue(!d.getDocumentIds().contains(0));
	}
	public void getTermIds() throws IOException {
		String folder = testFolder.newFolder().getAbsolutePath();
		TermsDatabase d = new H2TermsDatabase(new File(folder), "test", user, pwd);
		d.addDocument("doc1", Term.buildList( "a" ));
		Assert.assertTrue(d.getTermIds().size() == 1);
		Assert.assertTrue(d.getTermIds().contains(1));
		Assert.assertTrue(!d.getTermIds().contains(2));
		Assert.assertTrue(d.getTerms().contains(Term.build("a","")));
	}
	
	@Test
	public void tf() throws IOException {
		String folder = testFolder.newFolder().getAbsolutePath();
		TermsDatabase db = new H2TermsDatabase(new File(folder));
		TFIDF tfidf = new TFIDF(db);
		db.addDocument("d1", Term.buildList("a", "b", "c"));
		l.info("tf is {}", tfidf.tf("d1", Term.build("a","")) );
		Assert.assertTrue(tfidf.tf("d1", Term.build("a","")) > 0.2);
		Assert.assertTrue(tfidf.tf("d1", Term.build("a","")) < 0.4);
	}
	
	@Test
	public void countDocumentsContainingTermId() throws IOException {
		String folder = testFolder.newFolder().getAbsolutePath();
		TermsDatabase db = new H2TermsDatabase(new File(folder));
		db.addDocument("d1", Term.buildList("a", "b", "c"));
		db.addDocument("d2", Term.buildList("c", "d", "e"));
		db.addDocument("d3", Term.buildList("a", "f", "c"));
		Assert.assertTrue(db.countDocumentsContainingTerm(db.getTermId(Term.build("c", ""))) == 3);
		Assert.assertTrue(db.countDocumentsContainingTerm(db.getTermId(Term.build("a", ""))) == 2);
		Assert.assertTrue(db.countDocumentsContainingTerm(db.getTermId(Term.build("b", ""))) == 1);
	}
}
