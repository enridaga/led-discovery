package led.discovery.hive.udf;

import org.apache.hadoop.hive.ql.exec.UDFArgumentException;
import org.apache.hadoop.hive.ql.exec.UDFArgumentLengthException;
import org.apache.hadoop.hive.ql.exec.UDFArgumentTypeException;
import org.apache.hadoop.hive.ql.metadata.HiveException;
import org.apache.hadoop.hive.ql.udf.generic.GenericUDF;
import org.apache.hadoop.hive.serde2.objectinspector.ObjectInspector;
import org.apache.hadoop.hive.serde2.objectinspector.ObjectInspectorConverters;
import org.apache.hadoop.hive.serde2.objectinspector.primitive.PrimitiveObjectInspectorFactory;
import org.apache.hadoop.io.Text;

public abstract class StringManipulationUDF extends GenericUDF {

	private ObjectInspectorConverters.Converter converter;

	public StringManipulationUDF() {
		super();
	}

	@Override
	public ObjectInspector initialize(ObjectInspector[] arguments) throws UDFArgumentException {
		String name = getFunctionName();
		if (arguments.length != 1) {
			throw new UDFArgumentLengthException(name + "() accepts exactly one argument.");
		}
		if (arguments[0].getCategory() != ObjectInspector.Category.PRIMITIVE || (!arguments[0].getTypeName().equals("org.apache.hadoop.io.Text")) && (!arguments[0].getTypeName().equals("java.lang.String"))) {
			throw new UDFArgumentTypeException(0, "The single argument to " + name + "() should be " + "org.apache.hadoop.io.Text" + " but " + arguments[0].getTypeName() + " is found");
		}
		this.converter = ObjectInspectorConverters.getConverter(arguments[0], PrimitiveObjectInspectorFactory.writableStringObjectInspector);
		return PrimitiveObjectInspectorFactory.writableStringObjectInspector;
	}

	@Override
	public Object evaluate(DeferredObject[] arguments) throws HiveException {
		// Should be exactly one argument
		if (arguments.length != 1)
			return null;
		if (arguments[0] == null)
			return null;
		
		Text s = (Text) converter.convert(arguments[0].get());
		return new Text(perform(s.toString()));
	}

	@Override
	public String getDisplayString(String[] children) {
		return getFunctionName() + "(<String>)";
	}

	protected abstract String getFunctionName();

	protected abstract String perform(String input);
}