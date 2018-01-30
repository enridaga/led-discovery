package led.discovery.io;

import java.io.IOException;

public interface SourceFactory<T> {
	public Source getSource(T anything) throws IOException;
}
