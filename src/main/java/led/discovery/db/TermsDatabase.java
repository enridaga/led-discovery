package led.discovery.db;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import led.discovery.nlp.Term;

public interface TermsDatabase<IDType> {

	//protected IDType addTerm(Term term) throws IOException;

	public Term getTerm(IDType termId) throws IOException;

	public IDType getTermId(Term term) throws IOException;

	public IDType getDocId(String name) throws IOException;

	public String getDocName(IDType docId) throws IOException;

	public boolean containsTerm(Term term) throws IOException;

	public boolean containsDocument(String name) throws IOException;

	public boolean containsDocumentId(IDType docId) throws IOException;

	public IDType addDocument(String name, List<Term> terms) throws IOException;

	public int countTerms(IDType docId) throws IOException;

	public int countTerm(IDType docId, IDType termId) throws IOException;

	public int countTerms() throws IOException;

	public int countDocuments() throws IOException;

	public int countDocumentsContainingTerm(IDType termId) throws IOException;

	public int getTermCount(IDType docId, Term term) throws IOException;

	public List<IDType> getTermIds() throws IOException;

	public List<IDType> getDocumentIds() throws IOException;

	public List<Term> getTerms() throws IOException;

	public List<String> getDocuments() throws IOException;

	/**
	 * Counts the number of terms in a document (including repeated terms)
	 * 
	 * @return
	 * @throws IOException 
	 */
	public Map<IDType, Integer> countDocumentTerms() throws IOException;

	/**
	 * Count occurrences of each term in a document
	 * 
	 * @param docId
	 * @return
	 * @throws IOException 
	 */
	public Map<IDType, Integer> countEachTerm(IDType docId) throws IOException;

	/**
	 * Key being the term and Value the number of documents having it
	 * 
	 * @return
	 * @throws IOException 
	 */
	public Map<IDType, Integer> countDocumentsContainingTermIds() throws IOException;

	public Map<IDType, Term> getTermAndIds() throws IOException;
}
