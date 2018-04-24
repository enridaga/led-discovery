package led.discovery.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.zip.ZipInputStream;

import led.discovery.io.FileSourceFactory;
import led.discovery.io.Source;

public class GutenbergZipFileSourceFactory extends FileSourceFactory implements FilenameFilter {
	@Override
	public Source getSource(File file) throws IOException {
		String gutenbergId = getGutenbergId(file);
		String gutenbergName = "Gutenberg-" + gutenbergId;
		Charset encoding = getGutenbergEncoding(file);
		if (file.getName().endsWith(".zip")) {

		} else {
			throw new IOException("Not a gutenberg ZIP file: " + file);
		}
		return new Source() {
			@Override
			public InputStream getContent() throws IOException {
				ZipInputStream is = new ZipInputStream(new FileInputStream(file));
				if (is.getNextEntry() != null) {
					return is;
				} else {
					throw new IOException("Empty ZIP archive?");
				}
			}

			@Override
			public String getDocumentName() {
				return gutenbergName;
			}

			@Override
			public Charset getEncoding() {
				return encoding;
			}
		};

	}

	private String getGutenbergId(File file) {
		String fname = file.getName();
		// remove extension
		String name = fname.substring(0, fname.lastIndexOf('.'));
		// remove charset indicator
		if (name.contains("-")) {
			return name.substring(0, name.lastIndexOf('-'));
		}
		return name;
	}

	private Charset getGutenbergEncoding(File file) {
		String fname = file.getName();
		// remove extension
		String name = fname.substring(0, fname.lastIndexOf('.'));
		if (name.endsWith("-8")) {
			return Charset.forName("ISO-8859-1");
		}
		if (name.endsWith("-0")) {
			return Charset.forName("UTF-8");
		}
		return Charset.forName("ASCII");
	}

	@Override
	public boolean accept(File dir, String name) {
		if (!name.endsWith(".zip")) {
			return false;
		}
		String fname = name;
		// remove extension
		String id = fname.substring(0, fname.lastIndexOf('.'));
		if (name.endsWith("-0")) {
			// Best version UTF-8
			return true;
		} else if (name.endsWith("-8")) {
			// We prefer -0 version
			if (new File(dir, id + "-0.zip").exists()) {
				return false;
			}
			return true;
		} else {
			// We prefer -0 version
			if (new File(dir, id + "-0.zip").exists()) {
				return false;
			} else // We prefer -8 version
			if (new File(dir, id + "-8.zip").exists()) {
				return false;
			}
			return true;
		}
	}
}
