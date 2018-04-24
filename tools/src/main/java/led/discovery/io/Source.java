package led.discovery.io;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

public interface Source {
	public String getDocumentName();
	public Charset getEncoding();
	public InputStream getContent() throws IOException;
}
