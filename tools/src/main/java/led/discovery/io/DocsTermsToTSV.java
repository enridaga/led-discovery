package led.discovery.io;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import led.discovery.nlp.Term;
import led.discovery.nlp.TermsProvider;

public class DocsTermsToTSV {

	private TermsProvider provider;
	private Logger log = LoggerFactory.getLogger(DocsTermsToTSV.class);
	private SourceProvider sourceProvider;
	private File targetFile;

	public DocsTermsToTSV(File targetFile, SourceProvider sourceProvider, TermsProvider provider) {
		this.provider = provider;
		this.targetFile = targetFile;
		this.sourceProvider = sourceProvider;
	}

//	public void add(String name, InputStream is, Charset encoding) throws IOException {
//		// db.addDocument(name, terms);
//	}

	public void load() throws IOException {
		FileWriter fw = null;
		Iterator<Source> i = sourceProvider.getSources();
		fw = new FileWriter(targetFile);
		try (BufferedWriter bw = new BufferedWriter(fw)) {
			while (i.hasNext()) {
				Source s = i.next();
				try {
					long start = System.currentTimeMillis();
					String id = s.getDocumentName();
					String text = IOUtils.toString(s.getContent(), s.getEncoding());
					List<Term> terms = provider.terms(text);
					for (int x = 0; x < terms.size(); x++) {
						bw.write(id);
						bw.write("\t");
						bw.write(Integer.toString(x));
						bw.write("\t");
						bw.write(terms.get(x).toString());
						bw.write("\n");
					}
					long end = System.currentTimeMillis();
					log.info("{} [loaded in {}{}]", new Object[] { s.getDocumentName(), ((end - start) / 1000), "s" });
				} catch (IOException e) {
					log.error("Cannot load source {}: {}", s.getDocumentName(), e.getMessage());
				}

			}
		} finally {
			fw.close();
		}
	}

}
