package led.discovery.nlp;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.stanford.nlp.ling.CoreAnnotations.LemmaAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.PartOfSpeechAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;

public class StanfordNLPProvider implements TermsProvider {
	private Set<String> stopwords;
	private StanfordCoreNLP pipeline;
	private Logger log = LoggerFactory.getLogger(StanfordCoreNLP.class);

	public StanfordNLPProvider() {
		Properties props = new Properties();
		props.setProperty("annotators", "tokenize, ssplit, pos, lemma");
		pipeline = new StanfordCoreNLP(props);
		try {
			stopwords = new HashSet<String>(IOUtils.readLines(getClass().getResourceAsStream("stopwords.txt"), "UTF-8"));
		} catch (IOException e) {
			log.error("Cannot load stopwords.", e);
		}
	}

	@Override
	public List<Term> terms(String text) {
		Annotation document = new Annotation(text);
		pipeline.annotate(document);
		List<CoreMap> sentences = document.get(SentencesAnnotation.class);
		List<Term> terms = new ArrayList<Term>();
		LemmaCleaner cleaner = new StandardLemmaCleaner();
		for (CoreMap sentence : sentences) {
			// traversing the words in the current sentence
			// a CoreLabel is a CoreMap with additional token-specific methods
			for (CoreLabel token : sentence.get(TokensAnnotation.class)) {
				// this is the text of the token
				String lemma = token.getString(LemmaAnnotation.class);
				String pos = token.getString(PartOfSpeechAnnotation.class);
				if (stopwords.contains(lemma)) {
					continue;
				}
				if ((lemma = cleaner.clean(lemma)) != null) {
					terms.add(Term.build(lemma, pos));
				}
			}
		}
		return Collections.unmodifiableList(terms);
	}
}
