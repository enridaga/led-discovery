package led.discovery.app.model;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.StreamingOutput;

import org.codehaus.jackson.io.JsonStringEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.esotericsoftware.minlog.Log;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreAnnotations.CharacterOffsetBeginAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.CharacterOffsetEndAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.util.CoreMap;
import led.discovery.annotator.ListeningExperienceAnnotator.ListeningExperienceAnnotation;
import led.discovery.annotator.ListeningExperienceAnnotator.ListeningExperienceEndAnnotation;
import led.discovery.annotator.ListeningExperienceAnnotator.ListeningExperienceStartAnnotation;
import led.discovery.annotator.ListeningExperienceAnnotator.ListeningExperienceWithinAnnotation;
import led.discovery.annotator.ListeningExperienceAnnotator.NotListeningExperienceAnnotation;
import led.discovery.annotator.evaluators.HeatEntityEvaluator;
import led.discovery.annotator.evaluators.HeatEvaluator;

public class OutputModel {
	private List<Block> blocks;
	private Map<String, String> metadata;
	private int numberOfLEDFound;
	private static Logger L = LoggerFactory.getLogger(OutputModel.class);

	private OutputModel() {
		// Init an empty model
		this.numberOfLEDFound = 0;
		this.metadata = new HashMap<String, String>();
		this.blocks = new ArrayList<Block>();
	}

	private OutputModel(Annotation annotation) {
		// Init first
		this();
		// Then
//		this.numberOfLEDFound = annotation.get(ListeningExperienceAnnotation.class).size();
		List<CoreMap> sentences = annotation.get(SentencesAnnotation.class);
		Block current = null;
		StringBuilder text = null;
		int previousSentenceEnd = 0;
		double maxScore = 0.0;
		double minScore = 1.0;
		// Subsequent false blocks are merged ...
		for (CoreMap sentence : sentences) {
			
			// Is LED?
			boolean isLed = false;
			if (sentence.get(ListeningExperienceStartAnnotation.class) != null) {
				isLed = true;
			} else if (sentence.get(ListeningExperienceEndAnnotation.class) != null) {
				isLed = true;
			} else if (sentence.get(ListeningExperienceWithinAnnotation.class) != null) {
				isLed = true;
			}

			// It will be only in the first iteration
			if (current != null) {
				// Close block
				if (
				// This is a new Listening Experience
				(sentence.get(ListeningExperienceStartAnnotation.class) != null) ||
				// This is not a Listening Experience but the current was
//						(isLed && !current.isLE) || 
						(!isLed && current.isLE)) {
					current.offsetEnd = previousSentenceEnd;
					current.text = text.toString();
					blocks.add(current);
					current = null;
					text = null;
				} else {
					// Same type of sentence, append to current block (add space in between)
					text.append(' ');
					text.append(sentence.get(CoreAnnotations.TextAnnotation.class));
				}
			}

			// Begin new block
			if (current == null) {
				current = new Block();
				current.offsetStart = sentence.get(CharacterOffsetBeginAnnotation.class);
				text = new StringBuilder();
				text.append(sentence.get(CoreAnnotations.TextAnnotation.class));
				// This block will have the status of the initial sentence
				current.isLE = isLed;
				if (current.isLE) {
					L.debug(" ~~~ ~~~ ~~~ ~~~ ");
					numberOfLEDFound++;
				}
				// Get the score of this window
				if (sentence.get(ListeningExperienceStartAnnotation.class) != null) {
					// FIXME
					// TODO the evaluator depends on the configured method!
					double score;
					try{
						score = sentence.get(ListeningExperienceStartAnnotation.class).get(0).getScore(HeatEvaluator.class);
					}catch(NullPointerException npe) {
						score = sentence.get(ListeningExperienceStartAnnotation.class).get(0).getScore(HeatEntityEvaluator.class);						
					}
					current.setMetadata("score", Double.toString(score));
					L.debug(" ~~~ score: {}", current.getMetadata("score"));
					if(maxScore < score) {
						maxScore = score;
					}
					if(minScore > score) {
						minScore = score;
					}
				}else {
					current.setMetadata("score", "-1");
				}
			}

			// Setting end in case we need to close the block in the next iteration
			previousSentenceEnd = sentence.get(CharacterOffsetEndAnnotation.class);
			if (L.isDebugEnabled() && current.isLE) {
				L.debug(" ~~~ text: {}", sentence.get(CoreAnnotations.TextAnnotation.class));
			}
		}
		
		// Close and add last block
		current.offsetEnd = previousSentenceEnd;
		current.text = text.toString();
		blocks.add(current);
		
		L.debug("Passed {}", annotation.get(ListeningExperienceAnnotation.class).size());
		L.debug("Not Passed {}", annotation.get(NotListeningExperienceAnnotation.class).size());
		L.debug("True Blocks: {}", numberOfLEDFound);
		L.debug("False Blocks: {}", blocks.size() - numberOfLEDFound);
		L.debug("Max score: {}", maxScore);
		L.debug("Min score: {}", minScore);
		setMetadata("maxScore", Double.toString(maxScore));
		setMetadata("minScore", Double.toString(minScore));
	}

	public void setMetadata(String key, String value) {
		this.metadata.put(key, value);
	}

	public String getMetadata(String key) {
		return this.metadata.get(key);
	}

	public Map<String, String> getMetadata() {
		return Collections.unmodifiableMap(metadata);
	}

	public int numberOfLEFound() {
		return numberOfLEDFound;
	}

	public Iterator<Block> blocks() {
		return blocks.iterator();
	}

	public StreamingOutput streamJSON() {
		return new StreamingOutput() {
			@Override
			public void write(OutputStream os) throws IOException, WebApplicationException {
				OutputModel.writeAsJSON(OutputModel.this, os);
			}
		};
	}

	public String toJSON() {
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		try {
			OutputModel.writeAsJSON(this, os);
			return new String(os.toByteArray(), "UTF-8");
		} catch (IOException e) {
			Log.error("Cannot write JSON", e);
			return "";
		}
	}

	public static void writeAsJSON(OutputModel model, OutputStream os) throws IOException {
		Writer writer = new BufferedWriter(new OutputStreamWriter(os));
		writer.append('{');
		writer.append('\n');

		writer.append('\t');
		writer.append("\"found\": ").append(Integer.toString(model.numberOfLEFound())).append(',');
		writer.append('\n');

		writer.append('\t');
		writer.append("\"meta\": {");
		writer.append('\n');
		writer.append('\t');
		writer.append('\t');
		// Metadata
		boolean firstMeta = true;
		for (Entry<String, String> entry : model.getMetadata().entrySet()) {
			if (firstMeta) {
				firstMeta = false;
			} else {
				writer.append(",");
				writer.append('\n');
				writer.append('\t');
				writer.append('\t');
			}
			// Key
			writer.append("\"");
			for (char j : JsonStringEncoder.getInstance().quoteAsString(entry.getKey())) {
				writer.append(j);
			}
			writer.append("\": \"");
			// Value
			for (char j : JsonStringEncoder.getInstance().quoteAsString(entry.getValue())) {
				writer.append(j);
			}
			writer.append("\"");
		}
		writer.append("},");
		writer.append('\n');

		writer.append('\t');
		writer.append("\"blocks\": [");
		writer.append('\n');
		Iterator<Block> blocks = model.blocks();
		boolean first = true;
		while (blocks.hasNext()) {
			if (first) {
				first = false;
			} else {
				writer.append(',');
			}

			Block block = blocks.next();

			writer.append('\t');
			writer.append('\t');
			writer.append('{');
			writer.append('\n');

			writer.append('\t');
			writer.append('\t');
			writer.append("\"begin\": ").append(Integer.toString(block.offsetStart)).append(',');
			writer.append('\n');

			writer.append('\t');
			writer.append('\t');
			writer.append("\"end\": ").append(Integer.toString(block.offsetEnd)).append(',');
			writer.append('\n');

			writer.append('\t');
			writer.append('\t');
			writer.append("\"le\": ").append(block.isLE ? "true" : "false").append(',');
			writer.append('\n');

			writer.append('\t');
			writer.append("\"meta\": {");
			writer.append('\n');
			writer.append('\t');
			writer.append('\t');
			// Metadata
			firstMeta = true;
			for (Entry<String, String> entry : block.getMetadata().entrySet()) {
				if (firstMeta) {
					firstMeta = false;
				} else {
					writer.append(",");
					writer.append('\n');
					writer.append('\t');
					writer.append('\t');
				}
				// Key
				writer.append("\"");
				for (char j : JsonStringEncoder.getInstance().quoteAsString(entry.getKey())) {
					writer.append(j);
				}
				writer.append("\": \"");
				// Value
				for (char j : JsonStringEncoder.getInstance().quoteAsString(entry.getValue())) {
					writer.append(j);
				}
				writer.append("\"");
			}
			writer.append("},");
			writer.append('\n');

			writer.append('\t');
			writer.append('\t');
			writer.append("\"text\": \"");
			for (char j : JsonStringEncoder.getInstance().quoteAsString(block.text))
				writer.append(j);
			writer.append("\"");
			writer.append('\n');

			writer.append('\t');
			writer.append('\t');
			writer.append('}');
			writer.append('\n');
		}
		writer.append('\t');
		writer.append("]");
		writer.append('\n');

		writer.append('}');
		writer.flush();
	}

	public static OutputModel fromJSON(String json) {
		JsonObject jsonObject = new JsonParser().parse(json).getAsJsonObject();
		OutputModel model = new OutputModel();
		model.numberOfLEDFound = jsonObject.get("found").getAsInt();
		JsonObject metadata = jsonObject.get("meta").getAsJsonObject();
		for (Entry<String, JsonElement> entry : metadata.entrySet()) {
			model.setMetadata(entry.getKey(), entry.getValue().getAsString());
		}
		Iterator<JsonElement> it = jsonObject.get("blocks").getAsJsonArray().iterator();
		while (it.hasNext()) {
			JsonObject bo = it.next().getAsJsonObject();
			Block b = new Block();
			b.isLE = bo.get("le").getAsBoolean();
			b.offsetStart = bo.get("begin").getAsInt();
			b.offsetEnd = bo.get("end").getAsInt();
			b.text = bo.get("text").getAsString();
			JsonObject bmetadata = bo.get("meta").getAsJsonObject();
			for (Entry<String, JsonElement> entry : bmetadata.entrySet()) {
				b.setMetadata(entry.getKey(), entry.getValue().getAsString());
			}
			model.blocks.add(b);
		}
		return model;
	}

	public static OutputModel build(Annotation annotation) {
		return new OutputModel(annotation);
	}

	public static class Block {
		private int offsetStart;
		private int offsetEnd;
		private String text;
		private boolean isLE;
		private Map<String, String> metadata = new HashMap<String, String>();

		public int offsetStart() {
			return offsetStart;
		}

		public int offsetEnd() {
			return offsetEnd;
		}

		public boolean isLE() {
			return isLE;
		}

		public String getText() {
			return text;
		}

		public void setMetadata(String key, String value) {
			metadata.put(key, value);
		}

		public String getMetadata(String key) {
			return metadata.get(key);
		}

		public Map<String, String> getMetadata() {
			return Collections.unmodifiableMap(metadata);
		}
	}
}
