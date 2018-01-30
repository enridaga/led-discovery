package led.discovery.db;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import led.discovery.nlp.Term;

public interface TermsDatabase {

	public int addTerm(Term term) throws IOException;

	public Term getTerm(int termId) throws IOException;

	public int getTermId(Term term) throws IOException;

	public int getDocId(String name) throws IOException;

	public String getDocName(int docId) throws IOException;

	public boolean containsTerm(Term term) throws IOException;

	public boolean containsDocument(String name) throws IOException;

	public boolean containsDocument(int docId) throws IOException;

	public int addDocument(String name, List<Term> terms) throws IOException;

	public int countTerms(int docId) throws IOException;

	public int countTerm(int docId, int termId) throws IOException;

	public int countTerms() throws IOException;

	public int countDocuments() throws IOException;

	public int countDocumentsContainingTerm(int termId) throws IOException;

	public int getTermCount(int docId, Term term) throws IOException;

	public List<Integer> getTermIds() throws IOException;

	public List<Integer> getDocumentIds() throws IOException;

	public List<Term> getTerms() throws IOException;

	public List<String> getDocuments() throws IOException;

	/**
	 * Counts the number of terms in a document (including repeated terms)
	 * 
	 * @return
	 * @throws IOException 
	 */
	public Map<Integer, Integer> countDocumentTerms() throws IOException;

	/**
	 * Count occurrences of each term in a document
	 * 
	 * @param docId
	 * @return
	 * @throws IOException 
	 */
	public Map<Integer, Integer> countEachTerm(int docId) throws IOException;

	/**
	 * Key being the term and Value the number of documents having it
	 * 
	 * @return
	 * @throws IOException 
	 */
	public Map<Integer, Integer> countDocumentsContainingTermIds() throws IOException;

	public Map<Integer, Term> getTermAndIds() throws IOException;
}
