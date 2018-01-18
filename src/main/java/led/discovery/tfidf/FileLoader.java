package led.discovery.tfidf;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import led.discovery.utils.Term;

public class FileLoader {
	private TermsDatabase db;
	private TermsProvider provider;

	private Logger log = LoggerFactory.getLogger(FileLoader.class);

	public FileLoader(TermsDatabase db, TermsProvider provider) {
		this.provider = provider;
		this.db = db;
	}

	private void add(String name, InputStream is) throws IOException {
		if (db.containsDocument(name)) {
			log.warn("[SKIP] Already in database: {}", name);
		}

		String text = IOUtils.toString(is, "UTF-8");
		List<Term> terms = provider.terms(text);
		db.addDocument(name, terms);
	}

	public void add(File file) throws IOException {
		if (!file.exists() || file.isDirectory()) {
			throw new IOException("Not a file");
		}
		if (file.getName().endsWith(".zip")) {
			ZipInputStream is = new ZipInputStream(new FileInputStream(file));
			ZipEntry entry;
			while ((entry = is.getNextEntry()) != null) {
				long start = System.currentTimeMillis();
				add(file.getAbsolutePath() + "/" + entry.getName(), is);
				long end = System.currentTimeMillis();
				String fileInfo = String.format("Entry: [%s] len %d created %TD", entry.getName(), entry.getSize(), new Date(entry.getTime()));
				log.info("{} [loaded in {}{}]", new Object[] {fileInfo, ((end - start) / 1000) , "s"});
			}
		} else {
			InputStream is = new FileInputStream(file);
			add(file.getAbsolutePath(), is);
		}
	}

	public void addAll(String[] files) {
		for (String f : files) {
			try {
				add(new File(f));
			} catch (IOException e) {
				log.error("Problem with file " + f, e);
			}
		}
	}

	/**
	 * One file per line
	 * 
	 * @param list
	 * @throws IOException
	 */
	public void addFromFileList(File list) throws IOException {
		TFIDF tfidf = new TFIDF(new InMemTermsDatabase());
		List<String> files = IOUtils.readLines(new FileInputStream(list), "UTF-8");
		addAll(files.toArray(new String[files.size()]));
	}
}
