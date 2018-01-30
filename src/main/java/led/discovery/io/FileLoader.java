package led.discovery.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import led.discovery.db.TermsDatabase;
import led.discovery.nlp.Term;
import led.discovery.nlp.TermsProvider;

@Deprecated
public class FileLoader {
	private TermsDatabase db;
	private TermsProvider provider;
	private Logger log = LoggerFactory.getLogger(FileLoader.class);

	public FileLoader(TermsDatabase db, TermsProvider provider) {
		this.provider = provider;
		this.db = db;
	}

	private void addTree(File file, Collection<File> all) {
		File[] children = file.listFiles();
		if (children != null) {
			for (File child : children) {
				all.add(child);
				addTree(child, all);
			}
		}
	}

	public void addFromDirectory(File root) {
		Collection<File> all = new ArrayList<File>();
		addTree(root, all);
		for (File f : all) {
			try {
				add(f);
			} catch (IOException e) {
				log.error("An error occurred: {}", e.getMessage());
			}
		}
	}

	private void add(String name, InputStream is) throws IOException {
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
				String name = file.getAbsolutePath() + "/" + entry.getName();
				if (db.containsDocument(name)) {
					log.warn("[SKIP] Already in database: {}", name);
					continue;
				}
				long start = System.currentTimeMillis();
				add(name, is);
				long end = System.currentTimeMillis();
				String fileInfo = String.format("Entry: [%s] len %d created %TD", entry.getName(), entry.getSize(), new Date(entry.getTime()));
				log.info("{} [loaded in {}{}]", new Object[] { fileInfo, ((end - start) / 1000), "s" });
			}
		} else {
			String name = file.getAbsolutePath();
			if (db.containsDocument(name)) {
				log.warn("[SKIP] Already in database: {}", name);
				return;
			}
			InputStream is = new FileInputStream(file);
			add(name, is);
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
		List<String> files = IOUtils.readLines(new FileInputStream(list), "UTF-8");
		addAll(files.toArray(new String[files.size()]));
	}
}
