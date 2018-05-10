package led.discovery.annotator;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.stanford.nlp.ling.CoreAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreAnnotations.LemmaAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.PartOfSpeechAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.Annotator;
import edu.stanford.nlp.util.ArraySet;
import edu.stanford.nlp.util.CoreMap;
import led.discovery.nlp.Term;

public class MusicalHeatAnnotator implements Annotator {
	private Logger log = LoggerFactory.getLogger(MusicalHeatAnnotator.class);

	public final class MusicalHeatScoreAnnotation implements CoreAnnotation<Double> {
		@Override
		public Class<Double> getType() {
			return Double.class;
		}
	}

	public final class MusicalHeatAnnotation implements CoreAnnotation<Integer> {
		@Override
		public Class<Integer> getType() {
			return Integer.class;
		}
	}

	private HashMap<String, Double> dictionary;
	private List<String> sortedTerms;
	double max = 0;
	double min = 1;
	int bins = 5;
	double[] binsIndexes;

	public MusicalHeatAnnotator() {
		// Load musical dictionary
		dictionary = new HashMap<String, Double>();
		sortedTerms = new ArrayList<String>();
		CSVFormat format = CSVFormat.DEFAULT;
		Reader reader = new InputStreamReader(getClass().getResourceAsStream("dictionary.csv"));
		try (CSVParser parser = new CSVParser(reader, format)) {
			Iterator<CSVRecord> records = parser.iterator();
			while (records.hasNext()) {
				CSVRecord record = records.next();
				try {
					Double val = Double.valueOf(record.get(1));
					if (val > max) {
						max = val;
					} else if (val < min) {
						min = val;
					}
					dictionary.put(record.get(0), val);
					sortedTerms.add(record.get(0));
				} catch (Exception ex) {
					log.warn("Corrupted record: {}", record);
				}
			}

			binsIndexes = _generateIndexes(min, max, bins);
			log.trace("min: {} ; max: {} ; step: {}", new Object[] { min, max, bins });
			log.trace("Indexes: {}", new Object[] { binsIndexes });
		} catch (IOException e) {
			log.error("cannot load dictionary", e);
		}
	}

	private double[] _generateIndexes(double min, double max, int bins2) {
		double step = (max - min) / bins2;
		double[] indexes = new double[bins2];
		for (int x = 0; x < bins2; x++) {
			indexes[x] = min + (step * (x + 1));
		}
		return indexes;
	}

	private int _getBin(double tfidf) {
		if (tfidf < min) {
			return 0;
		}
		for (int x = 0; x < binsIndexes.length; x++) {
			if (tfidf < binsIndexes[x]) {
				return x + 1;
			}
		}
		return binsIndexes.length;
	}

	@Override
	public void annotate(Annotation annotation) {
		List<CoreMap> sentences = annotation.get(SentencesAnnotation.class);
		for (CoreMap sentence : sentences) {
			double sentenceScore = 0.0;
			int sentenceHeat = 0;
			int tokens = 0;
			for (CoreLabel token : sentence.get(CoreAnnotations.TokensAnnotation.class)) {
				String lemma = token.getString(LemmaAnnotation.class);
				String pos = token.getString(PartOfSpeechAnnotation.class);
				Term term = Term.build(lemma, pos);
				Double tfidf = dictionary.get(term.toString());
				int heat = 0;
				if (tfidf != null) {
					heat = _getBin(tfidf);
				} else {
					tfidf = 0.0;
				}
				token.set(MusicalHeatAnnotation.class, heat);
				token.set(MusicalHeatScoreAnnotation.class, tfidf);
				sentenceScore += tfidf;
				sentenceHeat += heat;
				tokens++;
			}
			// XXX Not sure yet this makes sense
			sentenceScore = sentenceScore / tokens;
			sentenceHeat = _getBin(sentenceScore);
			sentence.set(MusicalHeatAnnotation.class, sentenceHeat);
			sentence.set(MusicalHeatScoreAnnotation.class, sentenceScore);
		}
	}

	@Override
	public Set<Class<? extends CoreAnnotation>> requirementsSatisfied() {
		return Collections.unmodifiableSet(new ArraySet<>(Arrays.asList(MusicalHeatAnnotation.class, MusicalHeatScoreAnnotation.class)));
	}

	@Override
	public Set<Class<? extends CoreAnnotation>> requires() {
		return Collections.unmodifiableSet(new ArraySet<>(Arrays.asList(CoreAnnotations.TextAnnotation.class, CoreAnnotations.TokensAnnotation.class, CoreAnnotations.SentencesAnnotation.class, CoreAnnotations.PartOfSpeechAnnotation.class)));
	}

}
