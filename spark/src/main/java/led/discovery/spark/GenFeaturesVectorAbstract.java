package led.discovery.spark;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringEscapeUtils;
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

import led.discovery.spark.FeaturesFactory;

public abstract class GenFeaturesVectorAbstract {

	protected File trainingFile;
	protected File vocabularyFile = null;
	protected List<String> vocabulary = null;
	protected File outputFile;
	protected int top = -1;
	private FeaturesFactory featuresFactory;
	protected static final Logger L = LoggerFactory.getLogger(GenTermsFeatures.class);

	public GenFeaturesVectorAbstract(String[] args) throws IOException {
		this.featuresFactory = new FeaturesFactory();
		this.trainingFile = new File(args[0]);

		this.outputFile = new File(args[1]);
		if (args.length > 2 && new File(args[2]).exists()) {
			this.vocabularyFile = new File(args[2]);
			this.vocabulary = IOUtils.readLines(new FileInputStream(vocabularyFile), StandardCharsets.UTF_8);

		}

		if (args.length > 3) {
			this.top = Integer.parseInt(args[3]);
			if (vocabulary != null) {
				this.vocabulary = this.vocabulary.subList(0, top);
			}
		}
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

	protected FeaturesFactory getFeaturesFactory() {
		return featuresFactory;
	}
	
	protected abstract String prepare(String txt);

	public void run() throws FileNotFoundException, IOException {
		L.info("Starting Spark Session");
		_clean();
		SparkSession spark = SparkSession.builder().appName("Java Spark GenFeaturesVector").config("spark.master", "local").getOrCreate();
		L.info("Loading training file");
		Map<String, String> posMap = new HashMap<String, String>();
		Map<String, String> negMap = new HashMap<String, String>();
		int plen = 0;
		int nlen = 0;
		try (CSVParser reader = new CSVParser(new FileReader(trainingFile), CSVFormat.DEFAULT)) {
			Iterator<CSVRecord> it = reader.iterator();
			while (it.hasNext()) {
				CSVRecord r = it.next();
				String polarity = r.get(0);
				String fname = r.get(1);
				int tlen = Integer.parseInt(r.get(2));
				String content = StringEscapeUtils.unescapeCsv(r.get(3));
				Map<String, String> map;
				if ("1".equals(polarity)) {
					map = posMap;
					plen += tlen;
				} else {
					map = negMap;
					nlen += tlen;
				}
				map.put(fname, content);
			}
		}
		L.info("{} positives examples loaded ({} avg length)", posMap.size(), plen / posMap.size());
		L.info("{} negative examples loaded ({} avg length)", negMap.size(), nlen / negMap.size());

		L.info("Preparing features");
		List<Row> data = new ArrayList<Row>();

		// For each positive file
		for (Entry<String, String> n : posMap.entrySet()) {
			String content2 = n.getValue();
			String prepared = prepare(content2);
			data.add(RowFactory.create(1.0, n.getKey(), prepared));
		}
		// For each negative file
		for (Entry<String, String> n : negMap.entrySet()) {
			String content2 = n.getValue();
			String prepared = prepare(content2);
			data.add(RowFactory.create(0.0, n.getKey(), prepared));
		}
		L.info("Prepared");
		StructType schema = new StructType(new StructField[] { new StructField("label", DataTypes.DoubleType, false, Metadata.empty()), new StructField("docId", DataTypes.StringType, false, Metadata.empty()), new StructField("sentence", DataTypes.StringType, false, Metadata.empty())
		});

		Dataset<Row> sentenceData = spark.createDataFrame(data, schema);
		// XXX split by space and makes lowercase
		Tokenizer tokenizer = new Tokenizer().setInputCol("sentence").setOutputCol("words");
		Dataset<Row> wordsData = tokenizer.transform(sentenceData);
		wordsData.show(50);

		// Prepare feature set
		Dataset<Row> termsData;
		CountVectorizer countVectorizer = new CountVectorizer().setInputCol("words").setOutputCol("features");
		if (top > 0) {
			countVectorizer.setVocabSize(top).setMinTF(0);
		}
		L.info("Vocabulary:");

		if (vocabulary != null) {
			List<String> terms = new ArrayList<String>();
			int cv = 0;
			for (String s : vocabulary) {
				cv++;
				String t = s.split(",")[0];
				if (cv < 10) {
					System.err.println(t);
				}
				terms.add(t.toLowerCase());
			}
			L.info("Use vocabulary {} ({} terms)", vocabularyFile, vocabulary.size());
			List<Row> vocabularyDoc = new ArrayList<Row>();
			vocabularyDoc.add(RowFactory.create(1.0, "vocabulary", StringUtils.join(terms, " ")));
			Dataset<Row> vocabularyData = spark.createDataFrame(vocabularyDoc, schema);
			termsData = tokenizer.transform(vocabularyData);
		} else {
			// Learn from data
			L.info("Learn vocabulary from training data");
			termsData = wordsData;
		}
		L.info("Terms data");
		termsData.show();
		CountVectorizerModel cvModel = countVectorizer.fit(termsData);
		if (true) {
			// L.info("VocabSize: {}", cvModel.getVocabSize());
			Dataset<Row> vectorized = cvModel.transform(wordsData);
			vectorized.show(10);
			String[] vocabulary = cvModel.vocabulary();
			IOUtils.write(StringUtils.join(vocabulary, "\n"), new FileOutputStream(outputFile.getAbsolutePath() +
				".vocab"), StandardCharsets.UTF_8);
			vectorized.write().format("parquet").save(outputFile.getAbsolutePath());
		}
	}

}