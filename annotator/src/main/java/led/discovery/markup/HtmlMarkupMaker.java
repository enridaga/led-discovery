package led.discovery.markup;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringEscapeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.stanford.nlp.ling.CoreAnnotations.CharacterOffsetBeginAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.CharacterOffsetEndAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.LemmaAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.PartOfSpeechAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;
import edu.stanford.nlp.util.StringUtils;
import led.discovery.annotator.DBpediaSpotlightAnnotator.DBpediaEntityAnnotation;
import led.discovery.annotator.DBpediaSpotlightAnnotator.EntityLabel;
import led.discovery.annotator.ListeningExperienceAnnotator.ListeningExperienceEndAnnotation;
import led.discovery.annotator.ListeningExperienceAnnotator.ListeningExperienceStartAnnotation;
import led.discovery.annotator.ListeningExperienceAnnotator.ListeningExperienceWithinAnnotation;
import led.discovery.annotator.MusicalHeatAnnotator.MusicalHeatAnnotation;
import led.discovery.annotator.MusicalHeatAnnotator.MusicalHeatScoreAnnotation;
import led.discovery.annotator.window.TextWindow;

public class HtmlMarkupMaker {
	private Logger L = LoggerFactory.getLogger(getClass());
	private PrintStream _pw = null;

	private ByteArrayOutputStream _render(Annotation annotation) {
		L.trace("render()");
		ByteArrayOutputStream baos = new ByteArrayOutputStream();

		try {
			_pw = new PrintStream(baos, true, "utf-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return baos;
		}

		List<CoreMap> sentences = annotation.get(SentencesAnnotation.class);
		for (CoreMap sentence : sentences) {
			_sentence(sentence);
		}

		_pw = null;
		return baos;
	}

	private void _token(CoreLabel token) {
		// this is the text of the token
		String lemma = token.getString(LemmaAnnotation.class);
		String pos = token.getString(PartOfSpeechAnnotation.class);
		_startTag("token");
		_attribute("lemma", lemma);
		_attribute("pos", pos);
		int heatos = token.get(MusicalHeatAnnotation.class);
		_attribute("mheat", Integer.toString(heatos));
		double score = token.get(MusicalHeatScoreAnnotation.class);
		_attribute("mhscore", Double.toString(score));
		_closeTag();
		_pw.print(token.originalText());
		_endTag("token");
		_pw.print(token.after());
	}

	private void _sentence(CoreMap map) {
		// LED?
		if (map.get(ListeningExperienceStartAnnotation.class) != null) {
			_beginExperience(map.get(ListeningExperienceStartAnnotation.class));
		}

		_startTag("sentence");
		// Attributes
		int begin = map.get(CharacterOffsetBeginAnnotation.class);
		int end = map.get(CharacterOffsetEndAnnotation.class);
		_attribute("offset-begin", begin);
		_attribute("offset-end", end);
		if (map.get(ListeningExperienceWithinAnnotation.class) != null) {
			_attribute("within-annotation", _toIdsList(map.get(ListeningExperienceWithinAnnotation.class)));
		}
		L.trace("sentence {} {}", begin, end);
		int heatos = map.get(MusicalHeatAnnotation.class);
		_attribute("mheat", Integer.toString(heatos));
		double score = map.get(MusicalHeatScoreAnnotation.class);
		_attribute("mhscore", Double.toString(score));
		_closeTag();
		List<CoreLabel> tokens = map.get(TokensAnnotation.class);
		List<EntityLabel> entities = map.get(DBpediaEntityAnnotation.class);
		Map<Integer, EntityLabel> positions = new HashMap<Integer, EntityLabel>();
		if (entities != null)
			for (EntityLabel l : entities) {
				positions.put(l.beginPosition(), l);
			}
		L.trace("{} tokens", tokens.size());
		EntityLabel entity = null;
		for (CoreLabel token : tokens) {
			if (entity == null) {
				entity = positions.get(token.beginPosition());
				// Open entity
				if (entity != null) {
					_openEntity(entity);
				}
			} else {
				// to be closed?
				if (token.beginPosition() > entity.endPosition()) {
					_endEntity();
					entity = null;
				} // otherwise the token is within the entity annotation
			}
			_token(token);

		}
		_pw.flush();
		_endTag("sentence");
		if (map.get(ListeningExperienceEndAnnotation.class) != null) {
			_endExperience(map.get(ListeningExperienceEndAnnotation.class));
		}
	}

	private String _toIdsList(List<TextWindow> list) {
		List<String> l = new ArrayList<String>();
		for (TextWindow w : list) {
			l.add(Integer.toString(w.hashCode()));
		}
		return StringUtils.join(l, " ");
	}

	private void _beginExperience(List<TextWindow> textWindow) {
		for (TextWindow tw : textWindow) {
			_startTag("led-start");
			_attribute("sentences", tw.size());
			_attribute("led-id", tw.hashCode());
			_closeTag();
			_endTag("led-start");
		}
	}

	private void _endExperience(List<TextWindow> textWindow) {
		for (TextWindow tw : textWindow) {
			_startTag("led-end");
			_attribute("led-id", tw.hashCode());
			_closeTag();
			_endTag("led-end");
		}
	}

	private void _endEntity() {
		_endTag("entity");
	}

	private void _openEntity(EntityLabel entity) {
		_startTag("entity");
		_attribute("dbpedia", entity.getUri());
		_closeTag();
	}

	private void _startTag(String name) {
		_pw.print("<");
		_pw.print(name);
	}

	private void _closeTag() {
		_pw.print('>');
	}

	private void _endTag(String name) {
		_pw.print("</");
		_pw.print(name);
		_pw.print('>');
	}

	private void _attribute(String name, int value) {
		_attribute(name, Integer.toString(value));
	}

	private void _attribute(String name, String value) {
		_pw.print(' ');
		_pw.print(name);
		_pw.print("=");
		_pw.print('"');
		_pw.print(StringEscapeUtils.escapeXml(value));
		_pw.print('"');
	}

	public String html(String text, StanfordCoreNLP pipeline) {
		Annotation annotation = new Annotation(text);
		pipeline.annotate(annotation);
		// Before
		StringBuilder sb = new StringBuilder();
		try {
			sb.append(IOUtils.toString(getClass().getResourceAsStream("html-wrapper-before.html"), StandardCharsets.UTF_8));
		} catch (IOException e) {
			e.printStackTrace();
		}
		// Content
		ByteArrayOutputStream baos = _render(annotation);
		String content = new String(baos.toByteArray(), StandardCharsets.UTF_8);
		content = content.replaceAll("\\n", "<br/>");
		sb.append(content);

		// After
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
		HtmlMarkupMaker a = new HtmlMarkupMaker();
		byte[] encoded = Files.readAllBytes(Paths.get(file));
		String text = new String(encoded, StandardCharsets.UTF_8);

		// Prepare Pipeline
		Properties props = new Properties();
		props.setProperty("customAnnotatorClass.led.musicalheat", "led.discovery.annotator.MusicalHeatAnnotator");
		// props.setProperty("customAnnotatorClass.led.entities",
		// "led.discovery.annotator.DBpediaSpotlightAnnotator");
		props.setProperty("customAnnotatorClass.led.experiences", "led.discovery.annotator.ListeningExperienceAnnotator");
		String[] annotators = new String[] { "tokenize", "ssplit", "pos", "lemma", "led.musicalheat", // "led.entities",
				"led.experiences" };
		props.setProperty("annotators", StringUtils.join(annotators, ", "));
		StanfordCoreNLP pipeline = new StanfordCoreNLP(props);
		// Prepared

		String html = a.html(text, pipeline);
		File output = new File(outputfile);
		PrintWriter out = new PrintWriter(output);
		out.println(html);
		out.close();
	}
}
