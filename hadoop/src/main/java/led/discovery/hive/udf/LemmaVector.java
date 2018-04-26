package led.discovery.hive.udf;

import java.util.ArrayList;

import org.apache.hadoop.hive.ql.exec.Description;
import org.apache.hadoop.hive.ql.exec.UDFArgumentException;
import org.apache.hadoop.hive.ql.exec.UDFArgumentLengthException;
import org.apache.hadoop.hive.ql.metadata.HiveException;
import org.apache.hadoop.hive.ql.udf.generic.GenericUDF;
import org.apache.hadoop.hive.serde2.objectinspector.ObjectInspector;
import org.apache.hadoop.hive.serde2.objectinspector.ObjectInspectorConverters;
import org.apache.hadoop.hive.serde2.objectinspector.ObjectInspectorFactory;
import org.apache.hadoop.hive.serde2.objectinspector.primitive.PrimitiveObjectInspectorFactory;
import org.apache.hadoop.io.Text;

import led.discovery.nlp.StanfordNLPProvider;
import led.discovery.nlp.Term;

@Description(name = "ToLemmaVector", value = "_FUNC_(string) - returns an array of strings")
public class LemmaVector extends GenericUDF {
	private StanfordNLPProvider provider;
	private ObjectInspectorConverters.Converter[] converters;

	@Override
	public ObjectInspector initialize(ObjectInspector[] arguments) throws UDFArgumentException {
		if (arguments.length != 1) {
			throw new UDFArgumentLengthException("The function ToLemmaVector(s) takes exactly 1 arguments.");
		}

		converters = new ObjectInspectorConverters.Converter[arguments.length];
		for (int i = 0; i < arguments.length; i++) {
			converters[i] = ObjectInspectorConverters.getConverter(arguments[i], PrimitiveObjectInspectorFactory.writableStringObjectInspector);
		}
		provider = new StanfordNLPProvider();
		return ObjectInspectorFactory.getStandardListObjectInspector(PrimitiveObjectInspectorFactory.writableStringObjectInspector);
	}

	@Override
	public Object evaluate(DeferredObject[] arguments) throws HiveException {
		assert (arguments.length == 1);
		if (arguments[0].get() == null) {
			return null;
		}
		Text s = (Text) converters[0].convert(arguments[0].get());
		ArrayList<Text> result = new ArrayList<Text>();
		for (Term term : provider.terms(s.toString())) {
			result.add(new Text(term.toString()));
		}
		return result;
	}

	@Override
	public String getDisplayString(String[] children) {
		assert children.length == 1;
		return "ToLemmaVector(<String>)";
	}
}
