package led.discovery.db;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import led.discovery.nlp.Term;

public class InMemTermsDatabase implements TermsDatabase {
	// private Logger log = LoggerFactory.getLogger(InMemTermsDatabase.class);
	private Map<Integer, Integer[]> docTerms;
	private List<Term> dictionary;
	private List<String> docIds;

	public InMemTermsDatabase() {
		docTerms = new HashMap<Integer, Integer[]>();
		docIds = new ArrayList<String>();
		dictionary = new ArrayList<Term>();
	}

	@Override
	public boolean containsTerm(Term term) {
		return dictionary.contains(term);
	}

	@Override
	public int addTerm(Term term) {
		if (!dictionary.contains(term)) {
			dictionary.add(term);
		}
		return dictionary.indexOf(term);
	}

	@Override
	public Term getTerm(int termId) {
		return dictionary.get(termId);
	}

	@Override
	public int countTerms(int termId) {
		return dictionary.size();
	}

	@Override
	public int getDocId(String name) {
		return docIds.indexOf(name);
	}

	@Override
	public String getDocName(int docId) {
		return docIds.get(docId);
	}

	@Override
	public boolean containsDocument(String name) {
		return docIds.contains(name) && docTerms.containsKey(docIds.indexOf(name));
	}

	@Override
	public boolean containsDocument(int docId) {
		try {
			return (docIds.get(docId) != null) && docTerms.containsKey(docId);
		} catch (IndexOutOfBoundsException e) {
			return false;
		}
	}

	@Override
	public int addDocument(String name, List<Term> terms) throws IOException {
		if (docIds.contains(name)) {
			throw new IOException("Already exists");
		}
		docIds.add(name);
		int id = docIds.indexOf(name);
		Integer[] termIds = new Integer[terms.size()];
		for (int x = 0; x < terms.size(); x++) {
			termIds[x] = addTerm(terms.get(x));
		}
		docTerms.put(id, termIds);
		return id;
	}

	@Override
	public int countTerm(int docId, int termId) {
		return Collections.frequency(Arrays.asList(docTerms.get(docId)), termId);
	}

	@Override
	public int getTermCount(int docId, Term term) {
		return countTerm(docId, getTermId(term));
	}

	@Override
	public int countDocumentsContainingTerm(int termId) {
		int count = 0;
		for (int x = 0; x < docTerms.size(); x++) {
			// Any document containing the term counts 1
			if (Arrays.asList(docTerms.get(x)).contains(termId)) {
				count++;
			}
		}
		return count;
	}

	@Override
	public int countDocuments() {
		return docIds.size();
	}

	@Override
	public int getTermId(Term term) {
		return dictionary.indexOf(term);
	}

	@Override
	public int countTerms() {
		return dictionary.size();
	}

	@Override
	public List<Integer> getDocumentIds() {
		return IntStream.range(0, docIds.size()).boxed().collect(Collectors.toList());
	}

	@Override
	public List<Integer> getTermIds() {
		return IntStream.range(0, dictionary.size()).boxed().collect(Collectors.toList());
	}

	@Override
	public List<String> getDocuments() {
		return Collections.unmodifiableList(docIds);
	}

	@Override
	public List<Term> getTerms() {
		return Collections.unmodifiableList(dictionary);
	}

	/**
	 * Returns a map <doc-id,term-count>
	 * 
	 * @return
	 */
	@Override
	public Map<Integer, Integer> countDocumentTerms() {
		Map<Integer, Integer> map = new HashMap<Integer, Integer>();
		for (Entry<Integer, Integer[]> dt : docTerms.entrySet()) {
			map.put(dt.getKey(), dt.getValue().length);
		}
		return map;
	}

	@Override
	public Map<Integer, Integer> countEachTerm(int docId) {
		Map<Integer, Integer> map = new HashMap<Integer, Integer>();
		Integer[] terms = docTerms.get(docId);
		List<Integer> tl = Arrays.asList(terms);
		for (int x = 0; x < dictionary.size(); x++) {
			map.put(x, Collections.frequency(tl, x));
		}
		return map;
	}

	@Override
	public Map<Integer, Integer> countDocumentsContainingTermIds() {
		Map<Integer, Integer> count = new HashMap<Integer, Integer>();
		for (int x = 0; x < dictionary.size(); x++) {
			int c = 0;
			for (Entry<Integer, Integer[]> doc : docTerms.entrySet()) {
				if (Arrays.asList(doc.getValue()).contains(x)) {
					c++;
				}
			}
			count.put(x, c);
		}
		return count;
	}

	@Override
	public Map<Integer, Term> getTermAndIds() throws IOException {
		Map<Integer, Term> termAndIds = new HashMap<Integer, Term>();
		for (int x = 0; x < dictionary.size(); x++) {
			termAndIds.put(x, dictionary.get(x));
		}
		return termAndIds;
	}
}
