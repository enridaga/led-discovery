package led.discovery.tfidf;

import java.io.IOException;
import java.util.List;

public interface TermsDatabase {
	
	public int addTerm(String term) throws IOException;

	public String getTerm(int termId) throws IOException;

	public int getTermId(String term) throws IOException;

	public int getDocId(String name) throws IOException;

	public String getDocName(int docId) throws IOException;

	public boolean containsTerm(String term) throws IOException;

	public boolean containsDocument(String name) throws IOException;

	public boolean containsDocument(int docId) throws IOException;

	public int addDocument(String name, String[] lemmas) throws IOException;

	public int countTerms(int docId) throws IOException;

	public int countTerm(int docId, int termId) throws IOException;

	public int countTerms() throws IOException;

	public int countDocuments() throws IOException;

	public int countDocumentsContainingTerm(int termId) throws IOException;

	public int getTermCount(int docId, String term) throws IOException;
	
	public List<Integer> getTermIds() throws IOException;
	
	public List<Integer> getDocumentIds() throws IOException;
	
	public List<String> getTerms()throws IOException;
	
	public List<String> getDocuments() throws IOException;
}
