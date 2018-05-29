package led.discovery.spark;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.spark.ml.Pipeline;
import org.apache.spark.ml.PipelineModel;
import org.apache.spark.ml.PipelineStage;
import org.apache.spark.ml.classification.RandomForestClassificationModel;
import org.apache.spark.ml.classification.RandomForestClassifier;
import org.apache.spark.ml.evaluation.MulticlassClassificationEvaluator;
import org.apache.spark.ml.feature.IndexToString;
import org.apache.spark.ml.feature.StringIndexer;
import org.apache.spark.ml.feature.StringIndexerModel;
import org.apache.spark.ml.feature.VectorIndexer;
import org.apache.spark.ml.feature.VectorIndexerModel;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SaveMode;
import org.apache.spark.sql.SparkSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * See
 * https://spark.apache.org/docs/2.2.0/ml-classification-regression.html#random-forest-classifier
 * 
 * @author enridaga
 *
 */
public class TrainModel {
	private File trainingDir;
	private File outputModelFile;
	private File outputTestFile;
	private static final Logger L = LoggerFactory.getLogger(GenTrainingBOW.class);

	public TrainModel(String[] args) {
		this.trainingDir = new File(args[0]);
		this.outputModelFile = new File(args[1]);
		this.outputTestFile = new File(args[1] + ".test.csv");
	}

	public void run() throws IOException {
		L.info("Start");
		_clean();
		SparkSession spark = SparkSession.builder().appName("Java Spark Text2Vec").config("spark.master", "local").getOrCreate();
		// Load and parse the data file, converting it to a DataFrame.
		L.info("Loading training data ...");
		Dataset<Row> data = spark.read().format("parquet").load(trainingDir.getAbsolutePath());
		L.info("... loaded.");
		// Index labels, adding metadata to the label column.
		// Fit on whole dataset to include all labels in index.
		// FIXME Probably this step is useless. We know the labels are 1.0 and 0.0
		StringIndexerModel labelIndexer = new StringIndexer().setInputCol("label").setOutputCol("indexedLabel").fit(data);
		// Automatically identify categorical features, and index them.
		// Set maxCategories so features with > 4 distinct values are treated as
		// continuous.
		// FIXME Probably this step is useless. We know the labels are 1.0 and 0.0
		VectorIndexerModel featureIndexer = new VectorIndexer().setInputCol("features").setOutputCol("indexedFeatures").setMaxCategories(4) // XXX WHY?
				.fit(data);

		// Split the data into training and test sets (30% held out for testing)
		// Prepare training and test using the same balance of positives and negatives
		Dataset<Row> positives = data.filter("label=1.0");
		Dataset<Row> negatives = data.filter("label=0.0");
		L.info("{} positives", positives.count());
		L.info("{} negatives", negatives.count());
		Dataset<Row>[] psplits = positives.randomSplit(new double[] { 0.7, 0.3 });
		
		Dataset<Row>[] nsplits = negatives.randomSplit(new double[] { 0.7, 0.3 }); 
		
		Dataset<Row> trainingData = psplits[0].union(nsplits[0]);
		Dataset<Row> testData = psplits[1].union(nsplits[1]);
		L.info("Training data: {}", trainingData.count());
		psplits[0].show(5);
		nsplits[0].show(5);
		L.info("Test data: {}", testData.count());
		psplits[1].show(5);
		nsplits[1].show(5);
		// Train a RandomForest model.
		L.info("Preparing RandomForestClassifier");
		RandomForestClassifier rf = new RandomForestClassifier().setLabelCol("indexedLabel").setFeaturesCol("indexedFeatures");
		// Convert indexed labels back to original labels.
		L.info("Convert indexed labels back to original labels");
		IndexToString labelConverter = new IndexToString().setInputCol("prediction").setOutputCol("predictedLabel").setLabels(labelIndexer.labels());
		
		// Chain indexers and forest in a Pipeline
		L.info("Preparing Pipeline");
		Pipeline pipeline = new Pipeline().setStages(new PipelineStage[] { labelIndexer, featureIndexer, rf, labelConverter });

		// Train model. This also runs the indexers.
		L.info("Train model");
		PipelineModel model = pipeline.fit(trainingData);

		// Make predictions.
		L.info("Test data");
		Dataset<Row> predictions = model.transform(testData);

		// Select example rows to display.
		predictions.select("predictedLabel", "label", "docId", "features").show(5);
		predictions.select("predictedLabel", "label", "docId").write().option("timestampFormat", "yyyy/MM/dd HH:mm:ss ZZ").mode(SaveMode.Overwrite).format("csv").save(outputTestFile.getAbsolutePath());
		// Select (prediction, true label) and compute test error
		MulticlassClassificationEvaluator evaluator = new MulticlassClassificationEvaluator().setLabelCol("indexedLabel").setPredictionCol("prediction").setMetricName("accuracy");
		double accuracy = evaluator.evaluate(predictions);
		L.info("Test Error = {}", (1.0 - accuracy));

//		RandomForestClassificationModel rfModel = (RandomForestClassificationModel) (model.stages()[2]);
//		L.debug("Learned classification forest model:\n {}", rfModel.toDebugString());

		// Model to Save
		model.save(outputModelFile.getAbsolutePath());
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
		new TrainModel(args).run();
	}
}