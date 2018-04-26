package led.discovery.hive.udf;

import java.io.IOException;
import java.util.Iterator;

import org.apache.hadoop.fs.Path;
import org.apache.pig.data.Tuple;
import org.apache.pig.pigunit.Cluster;
import org.apache.pig.pigunit.PigTest;
import org.apache.pig.tools.parameters.ParseException;
import org.junit.BeforeClass;
import org.junit.Test;

public class ToLemmaVectorTest {
	private static Cluster cluster;

	@BeforeClass
	public static void beforeClass() throws IllegalArgumentException, IOException {
	}

	@Test
	public void test() throws IOException, ParseException {
		String pig = "DEFINE ToLemmaVector led.discovery.pig.udf.ToLemmaVector(); \n" + 
	"data = LOAD 'src/test/resources/to_lemma_vector_test_data.txt' USING PigStorage('|') AS (id:chararray, text:chararray);\n"
	+ "DUMP data; \n"
	+ "vect = FOREACH data GENERATE id, ToLemmaVector(text) AS lemma; \n" + "DUMP vect; ";
		String[] script = pig.split("\n");
		String[] args = new String[] { "input=to_lemma_vector_test_data.txt" };
		PigTest test = new PigTest(script, args);
		test.runScript();
	}
	

	@Test
	public void testFlatten() throws IOException, ParseException {
		String pig = "DEFINE ToLemmaVector led.discovery.pig.udf.ToLemmaVector(); \n" + 
	"data = LOAD 'src/test/resources/to_lemma_vector_test_data.txt' USING PigStorage('|') AS (id:chararray, text:chararray);\n"
	+ "DUMP data; \n"
	+ "vect = FOREACH data GENERATE id, FLATTEN(ToLemmaVector(text)) AS lemma; \n" + "DUMP vect; ";
		String[] script = pig.split("\n");
		String[] args = new String[] { "input=to_lemma_vector_test_data.txt" };
		PigTest test = new PigTest(script, args);
		test.runScript();
	}
}
