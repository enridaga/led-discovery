package led.discovery.spark;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.spark.ml.PipelineModel;
import org.apache.spark.ml.feature.CountVectorizerModel;
import org.apache.spark.ml.feature.Tokenizer;
import org.apache.spark.ml.feature.VectorIndexerModel;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.RowFactory;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.Metadata;
import org.apache.spark.sql.types.StructField;
import org.apache.spark.sql.types.StructType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Predictor {
	protected SparkSession spark;
	private PipelineModel rfModel;
	private List<String> vocabulary;
	private CountVectorizerModel cvModel;
	private static final Logger L = LoggerFactory.getLogger(Predictor.class);

	/**
	 * XXX Maybe the vocabulary could be extracted from the Pipeline?
	 * 
	 * @param modelSource
	 * @param vocabulary
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public Predictor(String modelSource, String vocabulary) throws FileNotFoundException, IOException {
		SparkSession s = SparkSession.builder().appName("LED Predictor").config("spark.master", "local").getOrCreate();
		_init(s, modelSource, vocabulary);
	}

	public Predictor(SparkSession spark, String modelSource, String vocabulary) throws FileNotFoundException, IOException {
		_init(spark, modelSource, vocabulary);
	}

	private void _init(SparkSession spark, String modelSource, String vocabulary) throws FileNotFoundException, IOException {
		L.info("Initialization");
		this.spark = spark;
		rfModel = PipelineModel.load(modelSource);
//		VectorIndexerModel vim = (VectorIndexerModel) rfModel.stages()[1];
		this.vocabulary = IOUtils.readLines(new FileInputStream(vocabulary), StandardCharsets.UTF_8);
		cvModel = new CountVectorizerModel(this.vocabulary.toArray(new String[this.vocabulary.size()]));
		cvModel.setInputCol("words").setOutputCol("features");
		L.info("Initialized");
	}

	protected Dataset<Row> vectorized(String docId, String text) {
		List<Row> data = Arrays.asList(RowFactory.create(-1.0, docId, text));
		StructType schema = new StructType(new StructField[] { new StructField("label", DataTypes.DoubleType, false, Metadata.empty()), new StructField("docId", DataTypes.StringType, false, Metadata.empty()), new StructField("sentence", DataTypes.StringType, false, Metadata.empty())
		});
		Dataset<Row> sentenceData = spark.createDataFrame(data, schema);
		Tokenizer tokenizer = new Tokenizer().setInputCol("sentence").setOutputCol("words");
		Dataset<Row> wordsData = tokenizer.transform(sentenceData);
		L.trace("Tokenized");
		Dataset<Row> vectorized = cvModel.transform(wordsData);
		L.trace("Vectorized");
		vectorized.show(1);
		return vectorized;
	}

	public boolean isLED(String text) {
		L.debug("Input size {}", text.length());
		String docId = "Input";
		Dataset<Row> vectorized = vectorized(docId, text);
		Dataset<Row> result = rfModel.transform(vectorized);
		L.debug("Predicted");
		result.select("predictedLabel").show(1);
		String prediction = result.select("predictedLabel").first().getAs(0);
		return prediction.equals("1.0");
	}
}
