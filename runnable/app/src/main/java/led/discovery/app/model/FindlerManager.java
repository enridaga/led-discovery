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

//	public OutputModel find(String source, String text) throws IOException {
//		return find(source, text, true, false);
//	}

	public FileCache getCache() {
		return fileCache;
	}

	public boolean hasOutputModelCached(String source) {
		return fileCache.containsHash(buildOutputModelCacheId(source));
	}

	public String buildOutputModelCacheId(String source) {
		StringWriter writer = new StringWriter();
		findler.getProperties().list(new PrintWriter(writer));
		String th = findler.getProperties().getProperty("custom.led.heat.threshold");
		String outputId = fileCache.buildHash(source, th, writer.getBuffer().toString());
		return outputId;
	}

	public OutputModel find(String source, String text, boolean writeCache) throws IOException {
		String outputId = buildOutputModelCacheId(source);
		// If exists in cache reload it
		OutputModel model;
		L.info("Computing annotations");
		model = findler.find(text);
		model.setMetadata("source", source);
		model.setMetadata("hash", outputId);
		if (writeCache) {
			L.info("Writing cache: {}", outputId);
			OutputStream fos = fileCache.putStream(outputId);
			OutputModel.writeAsJSON(model, fos);
			L.trace("Written: {}", outputId);
		}
		model.setMetadata("cached", "false");
		return model;
	}

	public OutputModel fromCache(String source) throws IOException {
		String outputId = buildOutputModelCacheId(source);
		if (fileCache.containsHash(outputId)) {
			OutputModel model;
			L.info("Reading from cache: {}", outputId);
			model = OutputModel.fromJSON(fileCache.getByHash(outputId));
			model.setMetadata("cached", "true");
			return model;
		} else {
			throw new IOException("Not cached");
		}
	}
}
