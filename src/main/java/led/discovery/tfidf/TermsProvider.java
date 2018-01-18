package led.discovery.tfidf;

import java.util.List;

import led.discovery.utils.Term;

public interface TermsProvider {
	public List<Term> terms(String text);
}
