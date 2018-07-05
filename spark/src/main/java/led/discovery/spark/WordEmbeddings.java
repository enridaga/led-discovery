package led.discovery.spark;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.spark.ml.feature.Tokenizer;
import org.apache.spark.ml.feature.Word2Vec;
import org.apache.spark.ml.feature.Word2VecModel;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;

public class WordEmbeddings {
	File featuresFile = null;
	File modelFile = null;
	SparkSession spark = null;

	public WordEmbeddings(String[] args) {
		featuresFile = new File(args[0]);
		modelFile = new File(args[1]); // featuresFile.getParent() + "/" + args[0] + "-word2vec.model");
		spark = SparkSession.builder().appName("Java Spark WordEmbeddings").config("spark.master", "local").getOrCreate();
	}

	public void run() {
		Word2VecModel model;

		if (!modelFile.exists()) {
			Dataset<Row> data = spark.read().format("com.databricks.spark.csv").option("timestampFormat", "yyyy/MM/dd HH:mm:ss ZZ").load(featuresFile.getAbsolutePath());
			data.show(5);
			Tokenizer tokenizer = new Tokenizer().setInputCol("_c2").setOutputCol("words");
			Dataset<Row> wordsData = tokenizer.transform(data);
			wordsData.show(10);
			// Learn a mapping from words to Vectors.
			Word2Vec word2Vec = new Word2Vec().setInputCol("words").setOutputCol("features").setMinCount(0); // .setVectorSize(3)
			model = word2Vec.fit(wordsData);
			try {
				model.save(modelFile.getAbsolutePath());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			System.out.println("Generated model file");
		} else {
			model = Word2VecModel.load(modelFile.getAbsolutePath());
		}
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
			System.out.println("------------------");
			System.out.println("Word to search:");
			while (true) {
				String input = reader.readLine();
				if (input.equals("exit"))
					break;
				if (input.startsWith("save as ")) {
					String suffix = input.substring(8);
					String path = featuresFile.getParent() + "/" + suffix + "-word2vec.model";
					if (new File(path).exists()) {
						System.err.println("File already exists");
					} else {
						System.out.println("Saving to " + path);
						model.save(path);
					}
				}
				try {
					Dataset<Row> _syns = model.findSynonyms(input, 10);
					_syns.show(10);

				} catch (Exception e) {
					System.err.println(e.getMessage());
				}
				System.out.println("Word to search:");
			}
			System.out.print("Exit.");
		} catch (Exception e) {
			System.out.println("An exception occured!");
			System.out.println(e.toString());
		}

		// try {
		// BufferedReader reader = new BufferedReader(new
		// InputStreamReader(System.in));
		// while (true) {
		// System.out.print(">");
		// String input = reader.readLine();
		// System.out.println(input);
		//// if (input.equals("exit"))
		//// break;
		//// if (input.startsWith("save as ")) {
		//// String suffix = input.substring(8);
		//// String path = featuresFile.getParent() + "/" + suffix +
		// "-word2vec.model";
		//// System.out.println("Saving to " + path);
		//// model.save(path);
		//// break;
		//// }
		//// Dataset<Row> _syns = model.findSynonyms(input, 10);
		//// _syns.show(10);
		// }
		//// System.out.print("Exit.");
		// } catch (Exception e) {
		// System.out.println("An exception occured!");
		// System.out.println(e.toString());
		// }
	}

	public static void main(String[] args) {
		new WordEmbeddings(args).run();
	}
}
