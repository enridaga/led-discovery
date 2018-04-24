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

import led.discovery.db.TermsDatabase;
import led.discovery.nlp.Term;

/**
 * Computes TF/IDF scores from a TermsDatabase.
 * 
 * This class is stateless.
 * 
 * @author enridaga
 *
 */
public class TFIDF<T> {
	private Logger log = LoggerFactory.getLogger(TFIDF.class);
	private TermsDatabase<T> data;

	private List<T> termIds = null;
	private Map<T, Term> termsAndIds = null;
	private int countDocuments = -1;
	private List<T> documentIds;
	private Map<T, Integer> countDocumentTerms;

	public TFIDF(TermsDatabase<T> data) throws IOException {
		this.data = data;
	}

	/**
	 * Do this prior to invoking any tf/idf method
	 * 
	 * @throws IOException
	 */
	public void init() throws IOException {
		log.info("init started");
		termIds = data.getTermIds();
		log.info("{} term ids loaded", termIds.size());
		data.getTerms();
		log.info("{} terms loaded", termIds.size());
		termsAndIds = data.getTermAndIds();
		documentIds = data.getDocumentIds();
		countDocuments = documentIds.size();
		log.info("{} documents ids", documentIds.size());
		countDocumentTerms = data.countDocumentTerms();
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
	public double tf(T docId, T termId) throws IOException {
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
	public double tf(String docName, Term term) throws IOException {
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
	public Map<T, Double> tf(T docId) throws IOException {
		Map<T, Double> tf = new HashMap<T, Double>();
		Map<T, Integer> tc = data.countEachTerm(docId);
		for (T tid : termIds) {
			double ttf;
			if (tc.containsKey(tid)) {
				ttf = (double) tc.get(tid) / (double) countDocumentTerms.get(docId);
			} else {
				ttf = 0;
			}
			tf.put(tid, ttf);
		}
		return tf;
	}

	/**
	 * Computes the idf of a given term against the collection of documents.
	 * 
	 * @param termId
	 * @return
	 */
	public double idf(T termId, int documentsWithTermId) throws IOException {
		int count = 0;
		int z = countDocuments;
		count = documentsWithTermId;
		double fl = (double) z / (double) count;
		double idf = Math.log(fl);
		return idf;
	}

	/**
	 * Computes the inverse document frequency of all terms in the dictionary.
	 * 
	 * @return an array of idf scores (index is the term id in dictionary)
	 */
	public Map<T, Double> idf() throws IOException {
		long start = System.currentTimeMillis();
		Map<T, Double> idf = new HashMap<T, Double>();
		Map<T, Integer> documentsWithTermIds = data.countDocumentsContainingTermIds();
		for (Entry<T, Integer> term : documentsWithTermIds.entrySet()) {
			idf.put(term.getKey(), idf(term.getKey(), term.getValue()));
		}
		long end = System.currentTimeMillis();
		log.trace("IDF computed in {}s", (end - start) / 1000);
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
	public Map<T, Double> tfidf(Map<T, Double> tf, Map<T, Double> idf) throws IOException {
		Map<T, Double> tfidf = new HashMap<T, Double>();
		for (Entry<T, Double> tfEntry : tf.entrySet()) {
			double tfv = tf.get(tfEntry.getKey());
			double idfv = idf.get(tfEntry.getKey());
			double tfidfv = tfv * idfv;
			tfidf.put(tfEntry.getKey(), tfidfv);
		}
		return tfidf;
	}

	public List<Map.Entry<Term, Double>> compute(T docId, Map<T, Double> idf) throws IOException {
		Map<T, Double> tfidf = tfidf(tf(docId), idf);
		Map<Term, Double> termsTfidf = new HashMap<Term, Double>();
		for (Entry<T, Double> tfidfe : tfidf.entrySet()) {
			termsTfidf.put(termsAndIds.get(tfidfe.getKey()), tfidfe.getValue());
		}
		List<Map.Entry<Term, Double>> entries = new ArrayList<Map.Entry<Term, Double>>();
		entries.addAll(termsTfidf.entrySet());
		return entries;
	}

	public List<Map.Entry<Term, Double>> compute(String docName) throws IOException {
		return compute(data.getDocId(docName), idf());
	}

	public Map<String, List<Map.Entry<Term, Double>>> computeMap() throws IOException {
		Comparator<Map.Entry<Term, Double>> byMapValuesDesc = new Comparator<Map.Entry<Term, Double>>() {
			@Override
			public int compare(Map.Entry<Term, Double> left, Map.Entry<Term, Double> right) {
				// inverse order
				return right.getValue().compareTo(left.getValue());
			}
		};
		Map<String, List<Map.Entry<Term, Double>>> computation = new HashMap<String, List<Map.Entry<Term, Double>>>();
		log.info("Computing idf ... ");
		long start = System.currentTimeMillis();
		Map<T, Double> idf = idf();
		log.info(" ... idf map size {} ...", idf.size());
		long end = System.currentTimeMillis();
		log.info(" ... computed in {}ms", (end - start));
		log.info("Documents: {}", countDocuments);
		for (T x : documentIds) {
			log.info("Computing doc {} ...", x);
			start = System.currentTimeMillis();
			List<Map.Entry<Term, Double>> entries = compute(x, idf);
			end = System.currentTimeMillis();
			log.info(" ... computed in {}s ...", (end - start) / 1000);
			start = System.currentTimeMillis();
			Collections.sort(entries, byMapValuesDesc);
			end = System.currentTimeMillis();
			log.info(" ... sorted in {}s", (end - start) / 1000);
			computation.put(data.getDocName(x), entries);
		}
		return computation;
	}
}
