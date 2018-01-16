package led.discovery.tfidf;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Computes TF/IDF scores from a TermsDatabase.
 * 
 * This class is stateless.
 * 
 * @author enridaga
 *
 */
public class TFIDF {
	private Logger log = LoggerFactory.getLogger(TFIDF.class);
	private TermsDatabase data;

	public TFIDF(TermsDatabase data) throws IOException {
		this.data = data;
	}

	/**
	 * Computes the term frequency. Term frequency is adjusted for document
	 * length : ft,d ÷ (number of words in d)
	 * 
	 * @param docId
	 *            Index of the document in the collection
	 * @param termId
	 *            Index of term in the dictionary
	 * @return the term frequency
	 */
	public double tf(int docId, int termId) throws IOException {
		return (double) data.countTerm(docId, termId) / (double) data.countTerms(docId);
	}

	/**
	 * Computes the term frequency.
	 * 
	 * 
	 * @param docName
	 *            Name of the document in the collection
	 * @param term
	 *            the term to check for frequency
	 * @return
	 */
	public double tf(String docName, String term) throws IOException {
		return tf(data.getDocId(docName), data.getTermId(term));
	}

	/**
	 * Computes the dictionary terms frequency of a given document
	 * 
	 * @param docId
	 *            The document identifier (index in the collection)
	 * @return the term frequency of doc terms (key being the term of the
	 *         dictionary)
	 */
	public Map<Integer, Double> tf(int docId) throws IOException {
		List<Integer> termIds = data.getTermIds();
		Map<Integer, Double> tf = new HashMap<Integer, Double>();
		for (Integer tid : termIds) {
			tf.put(tid, tf(docId, tid));
		}
		return tf;
	}

	/**
	 * Computes the idf of a given term against the collection of documents.
	 * 
	 * @param termId
	 * @return
	 */
	public double idf(int termId) throws IOException {
		int count = 0;
		int z = data.countDocuments();
		count = data.countDocumentsContainingTerm(termId);
		double fl = (double) z / (double) count;
		double idf = Math.log(fl);
		return idf;
	}

	/**
	 * Computes the inverse document frequency of all terms in the dictionary.
	 * 
	 * @return an array of idf scores (index is the term id in dictionary)
	 */
	public Map<Integer, Double> idf() throws IOException {
		Map<Integer, Double> idf = new HashMap<Integer, Double>();
		List<Integer> termIds = data.getTermIds();
		for (Integer termId : termIds) {
			idf.put(termId, idf(termId));
		}
		return idf;
	}

	/**
	 * 
	 * @param tf
	 *            - terms frequency of given document (position are ids from
	 *            dictionary)
	 * @param idf
	 *            - inverse document frequency of terms (position are ids from
	 *            dictionary)
	 * @return
	 */
	public Map<Integer, Double> tfidf(Map<Integer, Double> tf, Map<Integer, Double> idf) throws IOException {
		Map<Integer, Double> tfidf = new HashMap<Integer, Double>();
		for (Entry<Integer, Double> tfEntry : tf.entrySet()) {
			double tfv = tf.get(tfEntry.getKey());
			double idfv = idf.get(tfEntry.getKey());
			double tfidfv = tfv * idfv;
			tfidf.put(tfEntry.getKey(), tfidfv);
		}
		return tfidf;
	}

	public List<Map.Entry<String, Double>> compute(int docId, Map<Integer,Double> idf) throws IOException {
		Map<Integer,Double> tfidf = tfidf(tf(docId), idf);
		log.info(" - tfidf vector length {}", tfidf.size());
		Map<String, Double> termsTfidf = new HashMap<String, Double>();
		for (Entry<Integer,Double> tfidfe:tfidf.entrySet()) {
			termsTfidf.put(data.getTerm(tfidfe.getKey()), tfidfe.getValue());
		}
		log.info(" - tfidf entries {}", termsTfidf.size());
		List<Map.Entry<String, Double>> entries = new ArrayList<Map.Entry<String, Double>>();
		entries.addAll(termsTfidf.entrySet());
		return entries;
	}

	public List<Map.Entry<String, Double>> compute(String docName) throws IOException {
		return compute(data.getDocId(docName), idf());
	}

	public Map<String, List<Map.Entry<String, Double>>> computeMap() throws IOException {
		Comparator<Map.Entry<String, Double>> byMapValuesDesc = new Comparator<Map.Entry<String, Double>>() {
			@Override
			public int compare(Map.Entry<String, Double> left, Map.Entry<String, Double> right) {
				// inverse order
				return right.getValue().compareTo(left.getValue());
			}
		};
		Map<String, List<Map.Entry<String, Double>>> computation = new HashMap<String, List<Map.Entry<String, Double>>>();
		Map<Integer,Double> idf = idf();
		// FIXME We assume documents id are a complete 1-to-N set
		log.info("Documents: {}", data.countDocuments());
		List<Integer> documentIds = data.getDocumentIds();
		for(Integer x : documentIds) {
			List<Map.Entry<String, Double>> entries = compute(x, idf);
			Collections.sort(entries, byMapValuesDesc);
			computation.put(data.getDocName(x), entries);
		}
		return computation;
	}
}
