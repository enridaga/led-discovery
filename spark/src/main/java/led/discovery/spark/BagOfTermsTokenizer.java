package led.discovery.spark;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.spark.ml.UnaryTransformer;
import org.apache.spark.ml.util.Identifiable$;
import org.apache.spark.sql.types.DataType;
import org.apache.spark.sql.types.DataTypes;

import led.discovery.nlp.StanfordNLPProvider;
import led.discovery.nlp.Term;
import scala.Function1;
import scala.collection.JavaConversions;
/**
 * XXX Not serializable as depends on StanfordNLPProvider
 * XXX Not working...
 * @author enridaga
 *
 */
public class BagOfTermsTokenizer extends
		UnaryTransformer<String, scala.collection.immutable.Seq<String>, BagOfTermsTokenizer> {
	/**
	 * 
	 */
	private static final long serialVersionUID = -5634045654241230652L;

	private StanfordNLPProvider provider;
	/**
	 * 
	 */
	private String uid = null;

	public BagOfTermsTokenizer() {
		provider = new StanfordNLPProvider();
	}

	@Override
	public String uid() {
	    return getUid();
	}

	private String getUid() {
	    if (uid == null) {
	        uid = Identifiable$.MODULE$.randomUID("bottok");
	    }
	    return uid;
	}

	@Override
	public Function1<String, scala.collection.immutable.Seq<String>> createTransformFunc() {
		// can't use labmda syntax :(
		return new scala.runtime.AbstractFunction1<String, scala.collection.immutable.Seq<String>>() {
			@Override
			public scala.collection.immutable.Seq<String> apply(String s) {
				List<Term> terms = provider.terms(s);
				List<String> termss = new ArrayList<String>();
				for (Term t : terms) {
					termss.add(t.toString());
				}
				String[] split = terms.toArray(new String[termss.size()]);
				// convert to Scala type
				return JavaConversions.iterableAsScalaIterable(Arrays.asList(split)).toList();
			}
		};
	}

	@Override
	public void validateInputType(DataType inputType) {
		super.validateInputType(inputType);
		if (inputType != DataTypes.StringType)
			throw new IllegalArgumentException("Input type must be string type but got " +
				inputType + ".");
	}

	@Override
	public DataType outputDataType() {
		return DataTypes.createArrayType(DataTypes.StringType, false);
	}
}
