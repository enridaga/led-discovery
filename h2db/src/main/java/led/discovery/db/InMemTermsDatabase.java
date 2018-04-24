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

public class InMemTermsDatabase implements TermsDatabase<Integer> {
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

	protected Integer addTerm(Term term) {
		if (!dictionary.contains(term)) {
			dictionary.add(term);
		}
		return dictionary.indexOf(term);
	}

	@Override
	public Term getTerm(Integer termId) {
		return dictionary.get(termId);
	}

	@Override
	public int countTerms(Integer termId) {
		return dictionary.size();
	}

	@Override
	public Integer getDocId(String name) {
		return docIds.indexOf(name);
	}

	@Override
	public String getDocName(Integer docId) {
		return docIds.get(docId);
	}

	@Override
	public boolean containsDocument(String name) {
		return docIds.contains(name) && docTerms.containsKey(docIds.indexOf(name));
	}

	@Override
	public boolean containsDocumentId(Integer docId) {
		try {
			return (docIds.get(docId) != null) && docTerms.containsKey(docId);
		} catch (IndexOutOfBoundsException e) {
			return false;
		}
	}

	@Override
	public Integer addDocument(String name, List<Term> terms) throws IOException {
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
	public int countTerm(Integer docId, Integer termId) {
		return Collections.frequency(Arrays.asList(docTerms.get(docId)), termId);
	}

	@Override
	public int getTermCount(Integer docId, Term term) {
		return countTerm(docId, getTermId(term));
	}

	@Override
	public int countDocumentsContainingTerm(Integer termId) {
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
	public Integer getTermId(Term term) {
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
	public Map<Integer, Integer> countEachTerm(Integer docId) {
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
