package led.discovery.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Scanner;

import com.glaforge.i18n.io.CharsetToolkit;

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
				try {
					// 1024 bytes
					byte[] b = new byte[1024];
					FileInputStream fis = new FileInputStream(file);
					fis.read(b);
					fis.close();
					return new CharsetToolkit(b).guessEncoding();
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
				return Charset.defaultCharset();
			}
		};
	}

	@Override
	public boolean accept(File dir, String name) {
		return true;
	}
}
