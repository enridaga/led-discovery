package led.discovery.spark;

import java.io.File;
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
import org.apache.spark.ml.feature.Word2VecModel;
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

public class RandomForestPredictorEmb implements Predictor{
	protected SparkSession spark;
	private PipelineModel rfModel;
	
	private Word2VecModel model;
	private static final Logger L = LoggerFactory.getLogger(RandomForestPredictorEmb.class);

	/**
	 * XXX Maybe the vocabulary could be extracted from the Pipeline?
	 * 
	 * @param modelSource
	 * @param vocabulary
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public RandomForestPredictorEmb(String modelSource, String word2vec) throws FileNotFoundException, IOException {
		SparkSession s = SparkSession.builder().appName("LED Predictor Emb").config("spark.master", "local").getOrCreate();
		_init(s, modelSource, word2vec);
	}

	public RandomForestPredictorEmb(SparkSession spark, String modelSource, String word2vec) throws FileNotFoundException, IOException {
		_init(spark, modelSource, word2vec);
	}

	private void _init(SparkSession spark, String modelSource, String word2vec) throws FileNotFoundException, IOException {
		L.info("Initialization {} {}", modelSource, word2vec);
//		L.info("Initialization2 {} {}", new File(modelSource).exists(), new File(vocabulary).exists());
		this.spark = spark;
		rfModel = PipelineModel.load(modelSource);
		model = Word2VecModel.load(word2vec);
//		model.setInputCol("words").setOutputCol("features");
		L.info("Initialized");
	}

	protected String prepare(String text) {
		return text;
	}

	protected Dataset<Row> vectorized(String docId, String text) {
		List<Row> data = Arrays.asList(RowFactory.create(-1.0, docId, text));
		StructType schema = new StructType(new StructField[] { new StructField("label", DataTypes.DoubleType, false, Metadata.empty()), new StructField("docId", DataTypes.StringType, false, Metadata.empty()), new StructField("sentence", DataTypes.StringType, false, Metadata.empty())
		});
		Dataset<Row> sentenceData = spark.createDataFrame(data, schema);
		Tokenizer tokenizer = new Tokenizer().setInputCol("sentence").setOutputCol("words");
		Dataset<Row> wordsData = tokenizer.transform(sentenceData);
		L.trace("Tokenized");
		Dataset<Row> vectorized = model.transform(wordsData);
		L.trace("Vectorized");
		// vectorized.show(1);
		return vectorized;
	}
	/**
	 * NLP done before!
	 * @param text
	 * @return
	 */
	public boolean isLED(String text) {
		L.debug("Input size {}", text.length());
		String docId = "Input";
		Dataset<Row> vectorized = vectorized(docId, text);
		Dataset<Row> normalized = TrainRandomForestModel.getNormalizer().transform(vectorized);
		Dataset<Row> result = rfModel.transform(normalized);
		L.debug("Predicted");
		// result.select("predictedLabel").show(1);
		String prediction = result.select("predictedLabel").first().getAs(0);
		return prediction.equals("1.0");
	}
}
