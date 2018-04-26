package led.discovery.hadoop.pig;

import java.io.IOException;
import java.util.Iterator;

import org.apache.pig.EvalFunc;
import org.apache.pig.data.DataBag;
import org.apache.pig.data.DefaultDataBag;
import org.apache.pig.data.DefaultTuple;
import org.apache.pig.data.Tuple;

public class TupleToBag extends EvalFunc<DataBag> {

	@Override
	public DataBag exec(Tuple input) throws IOException {
		DataBag bag = new DefaultDataBag();
		Tuple o = (Tuple) input.get(0);
		
		for (int x = 0; x < o.size(); x++) {
			Tuple t = new DefaultTuple();
			t.append(o.get(x));
			System.out.println(o.get(x));
			bag.add(t);
		}
		return bag;
	}
}