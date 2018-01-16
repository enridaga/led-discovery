package led.discovery.tfidf;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class InMemTermsDatabase implements TermsDatabase {
	// private Logger log = LoggerFactory.getLogger(InMemTermsDatabase.class);
	private Map<Integer, Integer[]> docTerms;
	private List<String> dictionary;
	private List<String> docIds;

	public InMemTermsDatabase() {
		docTerms = new HashMap<Integer, Integer[]>();
		docIds = new ArrayList<String>();
		dictionary = new ArrayList<String>();
	}

	@Override
	public boolean containsTerm(String term) {
		return dictionary.contains(term);
	}

	@Override
	public int addTerm(String term) {
		if (!dictionary.contains(term)) {
			dictionary.add(term);
		}
		return dictionary.indexOf(term);
	}

	@Override
	public String getTerm(int termId) {
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
	public int addDocument(String name, String[] lemmas) throws IOException {
		if (docIds.contains(name)) {
			throw new IOException("Already exists");
		}
		docIds.add(name);
		int id = docIds.indexOf(name);
		Integer[] terms = new Integer[lemmas.length];
		for (int x = 0; x < lemmas.length; x++) {
			terms[x] = addTerm(lemmas[x]);
		}
		docTerms.put(id, terms);
		return id;
	}

	@Override
	public int countTerm(int docId, int termId) {
		return Collections.frequency(Arrays.asList(docTerms.get(docId)), termId);
	}

	@Override
	public int getTermCount(int docId, String term) {
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
	public int getTermId(String term) {
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
	public List<String> getTerms() {
		return Collections.unmodifiableList(dictionary);
	}
}
