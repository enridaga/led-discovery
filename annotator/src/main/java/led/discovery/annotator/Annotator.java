package led.discovery.annotator;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Properties;

import org.apache.commons.io.IOUtils;

import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import led.discovery.annotator.viewer.Viewer;

public class Annotator {

	public Annotation perform(String text) {
		Properties props = new Properties();
		props.setProperty("customAnnotatorClass.led.musicalheat", "led.discovery.annotator.MusicalHeatAnnotator");
		props.setProperty("annotators", "tokenize, ssplit, pos, lemma, led.musicalheat");
		StanfordCoreNLP pipeline = new StanfordCoreNLP(props);
		Annotation annotation = new Annotation(text);
		pipeline.annotate(annotation);
		return annotation;
	}

	public String html(String text) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		PrintStream ps;
		try {
			ps = new PrintStream(baos, true, "utf-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return "";
		}
		StringBuilder sb = new StringBuilder();
		try {
			sb.append(IOUtils.toString(getClass().getResourceAsStream("html-wrapper-before.html"), StandardCharsets.UTF_8));
		} catch (IOException e) {
			e.printStackTrace();
		}
		new Viewer(perform(text), ps).render();
		String content = new String(baos.toByteArray(), StandardCharsets.UTF_8);
		content = content.replaceAll("\\n", "<br/>");
		sb.append(content);
		try {
			sb.append(IOUtils.toString(getClass().getResourceAsStream("html-wrapper-after.html"), StandardCharsets.UTF_8));
		} catch (IOException e) {
			e.printStackTrace();
		}
		return sb.toString();
	}

	public static final void main(String[] args) throws IOException {
		String file = args[0];
		String outputfile = args[1];
		Annotator a = new Annotator();
		byte[] encoded = Files.readAllBytes(Paths.get(file));
		String text = new String(encoded, StandardCharsets.UTF_8);
		String html = a.html(text);
		File output = new File(outputfile);
		PrintWriter out = new PrintWriter(output);
		out.println(html);
		out.close();
	}
}
