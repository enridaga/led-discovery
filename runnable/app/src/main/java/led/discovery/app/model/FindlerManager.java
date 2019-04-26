package led.discovery.app.model;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FindlerManager {
	private Logger L = LoggerFactory.getLogger(FindlerManager.class);
	private Findler findler;
	private FileCache fileCache;

	public FindlerManager(Findler findler, FileCache cache) {
		this.fileCache = cache;
		this.findler = findler;
	}

	public OutputModel find(String source, String text) throws IOException {
		return find(source, text, true, false);
	}

	public OutputModel find(String source, String text, boolean usecache, boolean recache) throws IOException {
		StringWriter writer = new StringWriter();
		findler.getProperties().list(new PrintWriter(writer));
		String th = findler.getProperties().getProperty("custom.led.heat.threshold");
		String outputId = fileCache.buildHash(source, th, writer.getBuffer().toString());

		// If exists in cache reload it
		OutputModel model;
		if (fileCache.containsHash(outputId) && usecache && !recache) {
			L.info("Reading from cache: {}", outputId);
			model = OutputModel.fromJSON(fileCache.getByHash(outputId));
			model.setMetadata("cached", "true");
		} else {
			L.info("Computing annotations");
			model = findler.find(text);
			model.setMetadata("source", source);
			model.setMetadata("hash", outputId);
			if (usecache) {
				L.info("Writing cache: {}", outputId);
				OutputStream fos = fileCache.putStream(outputId);
				OutputModel.writeAsJSON(model, fos);
				L.trace("Written: {}", outputId);
			}
			model.setMetadata("cached", "false");
		}
		return model;
	}
}
