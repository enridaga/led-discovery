package led.discovery.nlp;

import java.util.List;

public interface TermsProvider {
	public List<Term> terms(String text);
}
