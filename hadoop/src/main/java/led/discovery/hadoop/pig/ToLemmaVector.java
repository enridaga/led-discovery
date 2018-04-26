package led.discovery.hadoop.pig;

import java.io.IOException;
import java.util.List;

import org.apache.pig.EvalFunc;
import org.apache.pig.data.DefaultTuple;
import org.apache.pig.data.Tuple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import led.discovery.nlp.StanfordNLPProvider;
import led.discovery.nlp.Term;

public class ToLemmaVector extends EvalFunc<Tuple> {
	private static final StanfordNLPProvider provider = new StanfordNLPProvider();
	private final static Logger log = LoggerFactory.getLogger(ToLemmaVector.class);

	public ToLemmaVector() {
		//log.trace("initializing");
	}

	@Override
	public Tuple exec(Tuple input) throws IOException {
		//log.trace("{}", input);
		Tuple tuple = new DefaultTuple();
		if (input == null || input.size() == 0)
			return null;
		String str = (String) input.get(0);
		List<Term> terms;
		synchronized (provider) {
			terms = provider.terms(str);
		}
		for (Term term : terms) {
			tuple.append(term.toString());
		}
		// return result;
		return tuple;
	}
}
