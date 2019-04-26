package led.discovery.app.model;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.AnnotationPipeline;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;

public class Findler {
	private Logger L = LoggerFactory.getLogger(Findler.class);
	Properties properties;

	public Findler(Properties properties) {
		this.properties = properties;
	}

	public Properties getProperties() {
		return properties;
	}

	public OutputModel find(String text) {
		OutputModel model;
		final Annotation annotation = annotate(text, properties);
		model = OutputModel.build(annotation);
		model.setMetadata("bytes", Integer.toString(text.getBytes().length));
		model.setMetadata("th", properties.getProperty("custom.led.heat.threshold"));
		for (Object k : properties.keySet()) {
			model.setMetadata("property-" + k, properties.getProperty((String) k));
		}
		return model;
	}

	public Annotation annotate(String text, Properties method) {
		if (L.isDebugEnabled()) {
			StringWriter writer = new StringWriter();
			method.list(new PrintWriter(writer));
			L.debug("Annotate with properties: \n{}", writer.getBuffer().toString());
		}
		StanfordCoreNLP.clearAnnotatorPool();
		AnnotationPipeline pipeline = new StanfordCoreNLP(method);
		Annotation annotation = new Annotation(text);
		pipeline.annotate(annotation);
		return annotation;
	}
}
