package led.discovery.spark;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Ignore;
//import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PredictorTest {
	private static final Logger L = LoggerFactory.getLogger(PredictorTest.class);
	private static RandomForestPredictor p;
	private static String e_1363621374;
	private static String e_1363623494;
	private static String e_Reuters21578_10000;
	private static Dataset<Row> trainingBow;
	private static SparkSession utilSpark;

	@BeforeClass
	public static void beforeClass() throws IOException {
		L.info("./ is {}", PredictorTest.class.getClassLoader().getResource("."));
		String model = PredictorTest.class.getClassLoader().getResource("./model-bow.parquet").getPath().toString();
		L.info("{}", model);
		String vocabulary = PredictorTest.class.getClassLoader().getResource("./training-bow.parquet.vocab").getPath().toString();
		String trainingBOWDir = PredictorTest.class.getClassLoader().getResource("./training-bow.parquet").getPath().toString();
		e_1363623494 = IOUtils.toString(PredictorTest.class.getClassLoader().getResource("./1363623494.txt").openStream(), StandardCharsets.UTF_8);
		e_1363621374 = IOUtils.toString(PredictorTest.class.getClassLoader().getResource("./1363621374.txt").openStream(), StandardCharsets.UTF_8);
		e_Reuters21578_10000 = IOUtils.toString(PredictorTest.class.getClassLoader().getResource("./Reuters21578-10000.txt").openStream(), StandardCharsets.UTF_8);
		
		utilSpark = SparkSession.builder().appName("Java Spark Text2Vec").config("spark.master", "local").getOrCreate();
		trainingBow = utilSpark.read().format("parquet").load(trainingBOWDir);
		L.info("{}", vocabulary);
		p = new RandomForestPredictor(utilSpark, model, vocabulary);

	}

	
	@Test
	public void test1() throws URISyntaxException, FileNotFoundException, IOException {
		boolean out = p.isLED(e_1363623494);
		L.info("1363623494: {}", out);
		Assert.assertTrue(out);
	}

	@Test
	public void test2() throws URISyntaxException, FileNotFoundException, IOException {
		boolean out2 = p.isLED(e_1363621374);
		L.info("1363621374: {}", out2);
		Assert.assertTrue(out2);
	}
	
	@Test
	public void test3() throws URISyntaxException, FileNotFoundException, IOException {
		boolean out2 = p.isLED(e_Reuters21578_10000);
		L.info("Reuters21578-10000: {}", out2);
		Assert.assertTrue(!out2);
	}

	@Test
	public void vectorized() {
		trainingBow.show(5);
		List<Row> r1 = trainingBow.filter("docId='1363623494.txt'").select("features").collectAsList();
		List<Row> r2 = p.vectorized("Input", e_1363623494).select("features").collectAsList();
		Assert.assertTrue(r1.size() == r2.size());
		for(int x=0; x<r1.size(); x++) {
			Row rr1 = r1.get(x);
			Row rr2 = r2.get(x);
			L.debug("{} == {}", rr1.get(0), rr2.get(0));
			Assert.assertTrue(rr1.get(0).equals(rr2.get(0)));
		}		
	}
}
