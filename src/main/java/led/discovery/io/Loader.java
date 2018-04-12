package led.discovery.io;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import led.discovery.db.TermsDatabase;
import led.discovery.nlp.Term;
import led.discovery.nlp.TermsProvider;

public class Loader {

	private TermsDatabase<?> db;
	private TermsProvider provider;
	private Logger log = LoggerFactory.getLogger(Loader.class);
	private SourceProvider sourceProvider;

	public Loader(TermsDatabase<?> database, SourceProvider sourceProvider, TermsProvider provider) {
		this.provider = provider;
		this.db = database;
		this.sourceProvider = sourceProvider;
	}

	public void add(String name, InputStream is, Charset encoding) throws IOException {
		String text = IOUtils.toString(is, encoding);
		List<Term> terms = provider.terms(text);
		db.addDocument(name, terms);
	}

	public void load() {
		Iterator<Source> i = sourceProvider.getSources();
		while (i.hasNext()) {
			Source s = i.next();
			try {
				if (db.containsDocument(s.getDocumentName())) {
					log.warn("[SKIP] Already in database: {}", s.getDocumentName());
					continue;
				}
				long start = System.currentTimeMillis();
				add(s.getDocumentName(), s.getContent(), s.getEncoding());
				long end = System.currentTimeMillis();
				log.info("{} [loaded in {}{}]", new Object[] { s.getDocumentName(), ((end - start) / 1000), "s" });
			} catch (IOException e) {
				log.error("Cannot load source {}: {}", s.getDocumentName(), e.getMessage());
			}
		}
	}
}
