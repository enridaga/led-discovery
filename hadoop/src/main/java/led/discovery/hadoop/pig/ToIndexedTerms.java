package led.discovery.hadoop.pig;

import java.io.IOException;

import org.apache.pig.EvalFunc;
import org.apache.pig.data.DataBag;
import org.apache.pig.data.DefaultDataBag;
import org.apache.pig.data.DefaultTuple;
import org.apache.pig.data.Tuple;

import led.discovery.spark.FeaturesFactory;

public class ToIndexedTerms extends EvalFunc<DataBag> {
	private static final FeaturesFactory ff = new FeaturesFactory();

	public ToIndexedTerms() {
	}

	@Override
	public DataBag exec(Tuple input) throws IOException {
		DataBag bag = new DefaultDataBag();
		if (input == null || input.size() == 0 || input.get(0) == null)
			return null;
		String str = (String) input.get(0);
		String[] terms;
		synchronized (ff) {
			terms = ff.aterms(str);
		}
		int index = 0;
		for (String term : terms) {
			Tuple t = new DefaultTuple();
			t.append(index);
			t.append(term);
			bag.add(t);
			index++;
		}
		return bag;
	}
}
