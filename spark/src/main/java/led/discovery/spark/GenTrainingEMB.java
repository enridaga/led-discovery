package led.discovery.spark;

import java.io.File;
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
import org.apache.spark.ml.feature.Word2Vec;
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

import led.discovery.nlp.StanfordNLPProvider;
import led.discovery.nlp.Term;

/**
 * Generating Bags of Terms (Lemma+POS)
 * 
 * @author enridaga
 *
 */
public class GenTrainingEMB {
	private File trainingFile;
	private File outputFile;
	private StanfordNLPProvider provider;
	private static final Logger L = LoggerFactory.getLogger(GenTrainingEMB.class);

	public GenTrainingEMB(String[] args) {
		provider = new StanfordNLPProvider();
		this.trainingFile = new File(args[0]);
		this.outputFile = new File(args[1]);
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

	protected String prepare(String txt) {
		L.debug("{}", txt);

		Word2Vec w2v = new Word2Vec();
//		w2v.fit
		List<Term> terms = provider.terms(txt);
		List<String> termss = new ArrayList<String>();
		for (Term t : terms) {
			termss.add(t.toString());
		}
		String[] split = termss.toArray(new String[termss.size()]);
		L.debug("{} terms", split.length);
		return StringUtils.join(split, " ");
	}

	public void run() throws FileNotFoundException, IOException {
		L.info("Starting Spark Session");
		_clean();
		SparkSession spark = SparkSession.builder().appName("Java Spark Text2Vec").config("spark.master", "local").getOrCreate();
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
		L.info("{} positives examples loaded ({} avg length)", posMap.size(), plen);
		L.info("{} negative examples loaded ({} avg length)", negMap.size(), nlen);

		// Just testing this.

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
		new GenTrainingEMB(args).run();
	}
}
