package led.discovery.pipelines;

import java.io.IOException;
import java.util.Properties;

import org.apache.commons.io.IOUtils;
import org.junit.BeforeClass;
import org.junit.Test;

import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import led.discovery.markup.HtmlMarkupMaker;

public class HtmlMarkupMakerTest {
	static String snippet;

	@BeforeClass
	public static void beforeClass() throws IOException {
		snippet = IOUtils.toString(HtmlMarkupMakerTest.class.getClassLoader().getResourceAsStream("RECOLL-Snippet-1.txt"), "UTF-8");
	}

	@Test
	public void test() {
		Properties props = new Properties();
		props.setProperty("customAnnotatorClass.led.musicalheat", "led.discovery.annotator.MusicalHeatAnnotator");
		props.setProperty("annotators", "tokenize, ssplit, pos, lemma, led.musicalheat");
		StanfordCoreNLP pipeline = new StanfordCoreNLP(props);
		HtmlMarkupMaker viewer = new HtmlMarkupMaker();
		String html = viewer.html(snippet, pipeline);
	}
}
