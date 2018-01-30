package led.discovery.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

public class FileSourceFactory implements SourceFactory<File>, FilenameFilter {
	@Override
	public Source getSource(final File file) throws IOException {
		return new Source() {
			@Override
			public InputStream getContent() throws IOException {
				return new FileInputStream(file);
			}

			@Override
			public String getDocumentName() {
				return file.getAbsolutePath();
			}

			@Override
			public Charset getEncoding() {
				// TODO Detect encoding?
				return Charset.defaultCharset();
			}
		};
	}

	@Override
	public boolean accept(File dir, String name) {
		return true;
	}
}
