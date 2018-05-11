package led.discovery.annotator.viewer;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import edu.stanford.nlp.util.CoreMap;
import led.discovery.annotator.DBpediaSpotlightAnnotator.DBpediaEntityAnnotation;
import led.discovery.annotator.DBpediaSpotlightAnnotator.EntityLabel;
import led.discovery.annotator.MusicalHeatAnnotator.MusicalHeatAnnotation;
import led.discovery.annotator.MusicalHeatAnnotator.MusicalHeatScoreAnnotation;

public class Viewer {
	private Logger L = LoggerFactory.getLogger(getClass());
	private Annotation annotation;
	private PrintWriter pw;

	public Viewer(Annotation annotation, PrintWriter pw) {
		this.annotation = annotation;
		this.pw = pw;
	}

	public Viewer(Annotation annotation2, PrintStream stream) {
		this(annotation2, new PrintWriter(stream));
	}

	public void render() {
		L.trace("render()");
		List<CoreMap> sentences = annotation.get(SentencesAnnotation.class);
		for (CoreMap sentence : sentences) {
			sentence(sentence);
		}
	}

	private void token(CoreLabel token) {
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
		pw.print(token.originalText());
		_endTag("token");
		pw.print(token.after());
	}

	private void sentence(CoreMap map) {
		_startTag("sentence");
		// Attributes
		int begin = map.get(CharacterOffsetBeginAnnotation.class);
		int end = map.get(CharacterOffsetEndAnnotation.class);
		_attribute("offset-begin", begin);
		_attribute("offset-end", end);
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
			token(token);

		}
		pw.flush();
		_endTag("sentence");
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
		pw.print("<");
		pw.print(name);
	}

	private void _closeTag() {
		pw.print('>');
	}

	private void _endTag(String name) {
		pw.print("</");
		pw.print(name);
		pw.print('>');
	}

	private void _attribute(String name, int value) {
		_attribute(name, Integer.toString(value));
	}

	private void _attribute(String name, String value) {
		pw.print(' ');
		pw.print(name);
		pw.print("=");
		pw.print('"');
		pw.print(StringEscapeUtils.escapeXml(value));
		pw.print('"');
	}
}
