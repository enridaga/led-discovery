package led.discovery.io;

import java.util.Iterator;

public interface SourceProvider {
	public Iterator<Source> getSources();
}
