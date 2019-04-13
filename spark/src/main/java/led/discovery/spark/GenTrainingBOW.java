package led.discovery.spark;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.spark.ml.feature.CountVectorizer;
import org.apache.spark.ml.feature.CountVectorizerModel;
import org.apache.spark.ml.feature.Tokenizer;
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

/**
 * See https://spark.apache.org/docs/2.1.0/ml-features.html#tf-idf
 * 
 *
 */
public class GenTrainingBOW {
	private File positivesFolder;
	private File negativesFolder;
	private File outputFile;

	private static final Logger L = LoggerFactory.getLogger(GenTrainingBOW.class);

	public GenTrainingBOW(String[] args) {
		this.positivesFolder = new File(args[0]);
		this.negativesFolder = new File(args[1]);
		this.outputFile = new File(args[2]);
	}

	private void _clean() {
		if (outputFile.exists()) {
			try {
				FileUtils.deleteDirectory(outputFile);
			} catch (IOException e) {
				L.error("", e);
			}
		}
	}

	public void run() throws FileNotFoundException, IOException {
		_clean();
		SparkSession spark = SparkSession.builder().appName("Java Spark Text2Vec").config("spark.master", "local").getOrCreate();
		List<Row> data = new ArrayList<Row>();
		int numberOf = 0;
		for (File f : this.positivesFolder.listFiles()) {
			try (FileInputStream fis = new FileInputStream(f)) {
				data.add(RowFactory.create(1.0, f.getName(), IOUtils.toString(fis, StandardCharsets.UTF_8)));
				numberOf++;
			} catch (IOException e) {
				L.error("", e);
			}
		}
		L.info("{} positive examples loaded", numberOf);
		int numberOfNeg = 0;
		for (File f : this.negativesFolder.listFiles()) {
			try (FileInputStream fis = new FileInputStream(f)) {
				data.add(RowFactory.create(0.0, f.getName(), IOUtils.toString(fis, StandardCharsets.UTF_8)));
				numberOfNeg++;
			} catch (IOException e) {
				L.error("", e);
			}
			if (numberOfNeg >= numberOf) {
				L.info("Reached the limit of {} negative examples", numberOfNeg);
				break;
			}
		}

		StructType schema = new StructType(new StructField[] { new StructField("label", DataTypes.DoubleType, false, Metadata.empty()), new StructField("docId", DataTypes.StringType, false, Metadata.empty()), new StructField("sentence", DataTypes.StringType, false, Metadata.empty())
		});

		Dataset<Row> sentenceData = spark.createDataFrame(data, schema);
		Tokenizer tokenizer = new Tokenizer().setInputCol("sentence").setOutputCol("words");
		Dataset<Row> wordsData = tokenizer.transform(sentenceData);

		CountVectorizerModel cvModel = new CountVectorizer().setInputCol("words").setOutputCol("features").fit(wordsData);
		Dataset<Row> vectorized = cvModel.transform(wordsData);
		vectorized.show(10);
		L.info("VocabSize: {}", cvModel.getVocabSize());
		String[] vocabulary = cvModel.vocabulary();
		IOUtils.write(StringUtils.join(vocabulary, "\n"), new FileOutputStream(outputFile.getAbsolutePath() +
			".vocab"), StandardCharsets.UTF_8);
		vectorized.write().format("parquet").save(outputFile.getAbsolutePath());
	}

	public static final void main(String[] args) throws FileNotFoundException, IOException {
		new GenTrainingBOW(args).run();
	}
}
