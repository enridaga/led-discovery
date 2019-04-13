package led.discovery.spark;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.ml.feature.Normalizer;
import org.apache.spark.ml.feature.StringIndexer;
import org.apache.spark.ml.feature.StringIndexerModel;
import org.apache.spark.ml.feature.VectorIndexer;
import org.apache.spark.ml.feature.VectorIndexerModel;
import org.apache.spark.ml.linalg.SparseVector;
import org.apache.spark.mllib.classification.SVMModel;
import org.apache.spark.mllib.classification.SVMWithSGD;
import org.apache.spark.mllib.evaluation.MulticlassMetrics;
import org.apache.spark.mllib.linalg.Vectors;
import org.apache.spark.mllib.regression.LabeledPoint;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import scala.Tuple2;

/**
 * See
 * https://spark.apache.org/docs/2.2.0/ml-classification-regression.html#random-forest-classifier
 * 
 *
 */
public class TrainSVMModel {
	private File trainingDir;
	private File outputModelFile;
	private File outputTestFile;
	private double trainsize = 7.0; // XXX
	private double testsize = 3.0;
	private static final Logger L = LoggerFactory.getLogger(TrainSVMModel.class);

	public TrainSVMModel(String[] args) {
		this.trainingDir = new File(args[0]);
		this.outputModelFile = new File(args[1]);
		this.outputTestFile = new File(args[1] + ".test.csv");
	}

	public static Normalizer getNormalizer() {
		// Normalize Vector
		Normalizer normalizer = new Normalizer().setInputCol("features").setOutputCol("normFeatures").setP(2.0);
		return normalizer;
	}

	public void run() throws IOException {
		L.info("Start");
		_clean();
		SparkSession spark = SparkSession.builder().appName("Java Spark ").config("spark.master", "local").getOrCreate();
		// Load and parse the data file, converting it to a DataFrame.
		L.info("Loading training data ...");
		Dataset<Row> data = spark.read().format("parquet").load(trainingDir.getAbsolutePath());
		L.info("... loaded.");
		// Index labels, adding metadata to the label column.
		// Fit on whole dataset to include all labels in index.
		// FIXME Probably this step is useless. We know the labels are 1.0 and
		// 0.0
		StringIndexerModel labelIndexer = new StringIndexer().setInputCol("label").setOutputCol("indexedLabel").fit(data);
		L.info("Normalize vector data ...");
		data = getNormalizer().transform(data);
		L.info("... done.");
		// Automatically identify categorical features, and index them.
		// Set maxCategories so features with > 4 distinct values are treated as
		// continuous.
		L.info("Indexing vector data ...");
		VectorIndexerModel featureIndexer = new VectorIndexer().setHandleInvalid("keep").setInputCol("normFeatures").setOutputCol("indexedFeatures").setMaxCategories(4) // XXX
																																											// WHY?
				.fit(data);
		L.info("... done.");
		L.info("Split negatives/positives for training data ...");
		// Split the data into training and test sets (30% held out for testing)
		// Prepare training and test using the same balance of positives and
		// negatives
		Dataset<Row> positives = data.filter("label=1.0");
		Dataset<Row> negatives = data.filter("label=0.0");
		L.info("{} positives", positives.count());
		L.info("{} negatives", negatives.count());
		Dataset<Row>[] psplits = positives.randomSplit(new double[] { trainsize, testsize });
		Dataset<Row>[] nsplits = negatives.randomSplit(new double[] { trainsize, testsize });
		Dataset<Row> trainingData = psplits[0].union(nsplits[0]);
		Dataset<Row> testData = psplits[1].union(nsplits[1]);
		L.info("Training data: {}", trainingData.count());
		psplits[0].show(5);
		nsplits[0].show(5);
		L.info("Test data: {}", testData.count());
		psplits[1].show(5);
		nsplits[1].show(5);
		L.info("... training data done.");
		
		Row r = trainingData.collectAsList().get(0);
		for(String c: trainingData.columns()) {
			L.debug("{} {} {}", new Object[] { c, r.getAs(c), r.getAs(c).getClass()});
		}
		
		// Train a RandomForest model.
		L.info("Preparing SVM Classifier");
		
		// Run training algorithm to build the model.
		JavaRDD<LabeledPoint> pp = trainingData.javaRDD().map(x -> new LabeledPoint(x.getAs("label"), Vectors.dense(((SparseVector)x.getAs("normFeatures")).toArray())));
		

		L.info("Train model");
		int numIterations = 100;
		SVMModel svm = SVMWithSGD.train(pp.rdd(), numIterations);
		// Clear the default threshold.
		svm.clearThreshold();

		// Compute raw scores on the test set.
		JavaRDD<Tuple2<Object, Object>> predictions = testData.javaRDD().map(x ->
		  new Tuple2<>(svm.predict(Vectors.dense(((SparseVector)x.getAs("normFeatures")).toArray())), x.getAs("label")));

		// Make predictions.
		L.info("Test data");
//		Dataset<Row> predictions = model.transform(testData);
		// Get evaluation metrics.
		MulticlassMetrics metrics = new MulticlassMetrics(JavaRDD.toRDD(predictions));
		double accuracy = metrics.accuracy();
		L.info("Test Error = {}", (1.0 - accuracy));

		// Model to Save
		svm.save(spark.sparkContext(), outputModelFile.getAbsolutePath());
	}

	private void _clean() {
		if (outputModelFile.exists()) {
			try {
				FileUtils.deleteDirectory(outputModelFile);
			} catch (IOException e) {
				L.error("", e);
			}
		}
		if (outputTestFile.exists()) {
			outputTestFile.delete();
		}
	}

	public static final void main(String[] args) throws IOException {
		new TrainSVMModel(args).run();
	}
}
