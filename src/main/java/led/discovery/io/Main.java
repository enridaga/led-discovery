package led.discovery.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import led.discovery.db.H2TermsDatabase;
import led.discovery.db.TermsDatabase;
import led.discovery.nlp.StanfordNLPProvider;
import led.discovery.nlp.Term;
import led.discovery.tfidf.TFIDF;
import led.discovery.utils.GutenbergZipFileSourceFactory;

public class Main {

	public static void main(String[] args) throws FileNotFoundException, IOException {
		File fileList = new File(args[0]);
		List<String> files = IOUtils.readLines(new FileInputStream(fileList), "UTF-8");
		// TermsDatabase db = new InMemTermsDatabase();
		File f = new File("h2-main");
		FileUtils.deleteDirectory(f);
		TermsDatabase db = new H2TermsDatabase(f);
		GutenbergZipFileSourceFactory fac = new GutenbergZipFileSourceFactory();
		FileSourceProvider provider = new FileSourceProvider(fac);
		provider.addAll(files);
		Loader loader = new Loader(db, provider, new StanfordNLPProvider());
		loader.load();
		TFIDF o = new TFIDF(db);
		o.init();
		Map<String, List<Map.Entry<Term, Double>>> result = o.computeMap();

		/**
		 * Show the first 20
		 */
		for (Entry<String, List<Map.Entry<Term, Double>>> doc : result.entrySet()) {
			Iterator<Map.Entry<Term, Double>> terms = doc.getValue().iterator();
			System.err.println(doc.getKey());
			int c = 0;
			while (terms.hasNext() && c < 20) {
				Entry<Term, Double> m = terms.next();
				if (m.getValue() == 0)
					continue;
				System.err.print(m.getKey());
				System.err.print(" ");
				System.err.println(m.getValue());
				c++;
			}
		}
	}
}
