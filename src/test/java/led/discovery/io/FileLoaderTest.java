package led.discovery.io;

import java.io.File;
import java.io.IOException;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import led.discovery.db.InMemTermsDatabase;
import led.discovery.db.TermsDatabase;
import led.discovery.io.FileLoader;
import led.discovery.nlp.StanfordNLPProvider;

public class FileLoaderTest {
	
	Logger log = LoggerFactory.getLogger(FileLoaderTest.class);
	
	FileLoader loader;
	TermsDatabase db;
	@Before
	public void before() {
		db = new InMemTermsDatabase();
		StanfordNLPProvider p = new StanfordNLPProvider();
		loader = new FileLoader(db, p);
	}

	@Test
	public void fromFileList() throws IOException {
		String list = "src/test/resources/files.txt";
		loader.addFromFileList(new File(list));
		Assert.assertTrue(db.countDocuments() == 4);
	}
}
