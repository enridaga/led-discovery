package led.discovery.io;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FileSourceProvider implements SourceProvider {
	private Logger log = LoggerFactory.getLogger(FileSourceProvider.class);
	private List<Source> files;
	private FileSourceFactory sourceFactory;

	public FileSourceProvider(FileSourceFactory sourceFactory) {
		this.files = new ArrayList<Source>();
		this.sourceFactory = sourceFactory;
	}
	
	public FileSourceProvider() {
		this.files = new ArrayList<Source>();
		this.sourceFactory = new FileSourceFactory();
	}

	@Override
	public Iterator<Source> getSources() {
		return files.iterator();
	}

	public void add(File file) throws IOException {
		if (!file.exists() || file.isDirectory()) {
			throw new IOException("Not a file");
		}
		if (sourceFactory.accept(file.getParentFile(), file.getName())) {
			files.add(sourceFactory.getSource(file));
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
	

	public void addAll(List<String> files) {
		for (String f : files) {
			try {
				add(new File(f));
			} catch (IOException e) {
				log.error("Problem with file " + f, e);
			}
		}
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

	public void addFromDirectory(File root) throws IOException {
		Collection<File> all = new ArrayList<File>();
		addTree(root, all);
		for (File f : all) {
			add(f);
		}
	}
}
