package led.discovery.app.model;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;

public class OutputModelTest {
	Logger L = LoggerFactory.getLogger(OutputModelTest.class);

	@Test
	public void testJson() throws IOException {
		
		Properties props = new Properties();
		props.load(this.getClass().getClassLoader().getResourceAsStream("data/MusicEmbeddingsTest.properties"));
		StringWriter writer = new StringWriter();
		props.list(new PrintWriter(writer));
		L.debug("Properties: \n{}", writer.getBuffer().toString());
		StanfordCoreNLP pipeline = new StanfordCoreNLP(props);
		String text = IOUtils.toString(this.getClass().getClassLoader().getResourceAsStream("data/RECOLL.txt"),
				StandardCharsets.UTF_8);
		Annotation annotation = new Annotation(text);
		pipeline.annotate(annotation);
		OutputModel model = OutputModel.build(annotation);
		String json1 = model.toJSON();
		L.trace("\n{}",json1);
		OutputModel model2 = OutputModel.fromJSON(json1);

		Assert.assertTrue(model2.numberOfLEFound() == model.numberOfLEFound());
		Assert.assertTrue(model.getMetadata().size() == model2.getMetadata().size());
		Assert.assertTrue(model.blocks().next().getText().equals(model2.blocks().next().getText()));
		Assert.assertTrue(json1.equals(model2.toJSON()));
	}
}
