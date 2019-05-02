package led.discovery.io;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import led.discovery.app.model.FileCache;
import led.discovery.app.model.Findler;
import led.discovery.app.model.FindlerManager;
import led.discovery.app.model.OutputModel;

public class RunAnnotator {
	private Logger L = LoggerFactory.getLogger(RunAnnotator.class);
	Properties method;
	FindlerManager manager;

	public RunAnnotator(String cacheDir) throws IOException {
		Properties properties = new Properties();
		properties.load(getClass().getClassLoader().getResource("heat-entity-music-filtered.properties").openStream());
		this.method = properties;
		if (L.isDebugEnabled()) {
			StringWriter writer = new StringWriter();
			method.list(new PrintWriter(writer));
			L.debug("Annotate with properties: \n{}", writer.getBuffer().toString());
		}
		manager = new FindlerManager(new Findler(method), new FileCache(cacheDir));
	}

	public OutputModel annotate(String sourceId, String text) throws IOException {
		return manager.find(sourceId, text);
	}

}
