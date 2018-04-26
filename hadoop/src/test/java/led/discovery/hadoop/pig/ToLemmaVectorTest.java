package led.discovery.hadoop.pig;

import java.io.IOException;

import org.apache.pig.pigunit.PigTest;
import org.apache.pig.tools.parameters.ParseException;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

public class ToLemmaVectorTest {

	@BeforeClass
	public static void beforeClass() throws IllegalArgumentException, IOException {
	}

	@Ignore
	@Test
	public void test() throws IOException, ParseException {
		String pig = "DEFINE ToLemmaVector led.discovery.hadoop.pig.ToLemmaVector(); \n" +
			"data = LOAD 'src/test/resources/to_lemma_vector_test_data.txt' USING PigStorage('|') AS (id:chararray, text:chararray);\n" +
			"DUMP data; \n" +
			"vect = FOREACH data GENERATE id, ToLemmaVector(text) AS lemma; \n" + "DUMP vect; ";
		String[] script = pig.split("\n");
		String[] args = new String[] { "input=to_lemma_vector_test_data.txt" };
		PigTest test = new PigTest(script, args);
		test.runScript();
	}

	@Ignore
	@Test
	public void testFlatten() throws IOException, ParseException {
		String pig = "DEFINE ToLemmaVector led.discovery.hadoop.pig.ToLemmaVector(); \n" +
			"DEFINE TupleToBag led.discovery.hadoop.pig.TupleToBag(); \n" +
			"data = LOAD 'src/test/resources/to_lemma_vector_test_data.txt' USING PigStorage('|') AS (id:chararray, text:chararray);\n" +
			"DUMP data; \n" +
			"vect = FOREACH data GENERATE id, ToLemmaVector(text) AS terms; \n" + "DUMP vect; \n" +
			"asbag = FOREACH vect {" +
			"		GENERATE id, Flatten(TupleToBag(terms)); " +
			"}\n" +
			"DUMP asbag;" +
			"";

		String[] script = pig.split("\n");
		String[] args = new String[] { "input=to_lemma_vector_test_data.txt" };
		PigTest test = new PigTest(script, args);
		test.runScript();
	}
}
