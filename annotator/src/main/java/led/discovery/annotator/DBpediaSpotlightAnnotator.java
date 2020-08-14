package led.discovery.annotator;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.stanford.nlp.ling.CoreAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.Annotator;
import edu.stanford.nlp.util.ArraySet;
import edu.stanford.nlp.util.CoreMap;
import edu.stanford.nlp.util.ErasureUtils;
import led.discovery.analysis.entities.spot.SpotlightAnnotation;
import led.discovery.analysis.entities.spot.SpotlightClient;
import led.discovery.analysis.entities.spot.SpotlightResponse;
import led.discovery.annotator.apicache.SimpleCache;

public class DBpediaSpotlightAnnotator implements Annotator {
	private Logger L = LoggerFactory.getLogger(DBpediaSpotlightAnnotator.class);
	private SimpleCache cache;

	/**
	 * A URI
	 *
	 */
	public final class DBpediaEntityAnnotation implements CoreAnnotation<List<EntityLabel>> {
		@Override
		public Class<List<EntityLabel>> getType() {
			return ErasureUtils.uncheckedCast(List.class);
		}
	}

	private String service;
	// private String cache;
	private SpotlightClient client;
	private double confidence;
	private int support;
	private int batch_sentences;

	public DBpediaSpotlightAnnotator(String name, Properties props) {
		// load the lemma file
		// format should be tsv with word and lemma
		service = props.getProperty("custom.spotlight.service", "https://api.dbpedia-spotlight.org/en/annotate");
		cache = new SimpleCache(new File(props.getProperty("custom.cache", ".simple_cache")));
		cache.set_cache_extension(".spotlight");
		confidence = Double.parseDouble(props.getProperty("custom.spotlight.confidence", "0.2"));
		support = Integer.parseInt(props.getProperty("custom.spotlight.support", "0"));
		batch_sentences = Integer.parseInt(props.getProperty("custom.spotlight.batch_size", "1"));
		L.debug("Using service {}", service);
		// cache = props.getProperty("custom.spotlight.cache");
		client = new SpotlightClient(service);
	}

	private void annotate(CoreMap sentence) throws IOException {
		L.trace("Sentence {}", sentence);
		int sentenceOffset = sentence.get(CoreAnnotations.TokensAnnotation.class).get(0).beginPosition();
		String text = sentence.get(CoreAnnotations.TextAnnotation.class);

		try {
			String key = SimpleCache.hashLabel(text + confidence + support);
			SpotlightResponse r;
			boolean updateCache = false;
			if (cache.is_cached(key)) {
				r = (SpotlightResponse) cache.get_cache_as_object(key);
			} else {
				updateCache = true;
				r = client.perform(text, confidence, support);
			}
			List<EntityLabel> entities = new ArrayList<EntityLabel>();
			for (SpotlightAnnotation an : r.asList()) {
				L.trace("{} [{}]", an.getUri(), an.getConfidence());
				EntityLabel el = new EntityLabel();
				el.setBeginPosition(sentenceOffset + an.getOffset());
				el.setSurfaceForm(an.getSurfaceForm());
				el.setEndPosition(sentenceOffset + an.getOffset() + an.getSurfaceForm().length());
				el.setNER(an.getUri());
				el.setUri(an.getUri());
				List<String> types = an.getTypes();
				if (types == null) {
					types = Collections.emptyList();
				}
				el.setTypes(types);
				entities.add(el);
			}
			sentence.set(DBpediaEntityAnnotation.class, entities);
			if (updateCache) {
				cache.set_cache(key, r);
			}
		} catch (IOException e) {
			L.error("Interaction with service failed.", e);
			throw e;
		}
	}

	@Override
	public void annotate(Annotation annotation) {
		L.trace("Annotating, batch_sentences = {}", batch_sentences);
		try {
			List<CoreMap> sentences = annotation.get(SentencesAnnotation.class);
			if (batch_sentences == 1) {
				for (CoreMap sentence : sentences) {
					annotate(sentence);
				}
			} else {
				List<CoreMap> batch = new ArrayList<CoreMap>();
				for (int c = 0; c < sentences.size(); c++) {
					CoreMap sentence = sentences.get(c);
					batch.add(sentence);
					if (batch.size() == batch_sentences || c == (sentences.size() - 1)) {
						annotate(batch);
						batch.clear();
					}
				}
			}
		} catch (IOException e) {
			L.error("Interrupt", e);
		}
	}

	private void annotate(List<CoreMap> batch) throws IOException {
		L.trace("Batch {} ({} sentences)", batch, batch.size());
		int batchOffset = batch.get(0).get(CoreAnnotations.TokensAnnotation.class).get(0).beginPosition();

		StringBuilder textBuilder = new StringBuilder();
		for (CoreMap sentence : batch) {
			String sentenceText = sentence.get(CoreAnnotations.TextAnnotation.class);
			textBuilder.append(sentenceText);
		}
		String text = textBuilder.toString();
		//

		try {
			String key = SimpleCache.hashLabel(new StringBuilder().append(text).append(Double.toString(confidence))
					.append(Double.toString(support)).toString());
			SpotlightResponse r;
			boolean updateCache = false;
			if (cache.is_cached(key)) {
				r = (SpotlightResponse) cache.get_cache_as_object(key);
			} else {
				updateCache = true;
				r = client.perform(text, confidence, support);
			}
			// List<EntityLabel> entities = new ArrayList<EntityLabel>();
			for (SpotlightAnnotation an : r.asList()) {
				L.trace("{} [{}]", an.getUri(), an.getConfidence());
				EntityLabel el = new EntityLabel();
				el.setBeginPosition(batchOffset + an.getOffset());
				el.setSurfaceForm(an.getSurfaceForm());
				el.setEndPosition(batchOffset + an.getOffset() + an.getSurfaceForm().length());
				el.setNER(an.getUri());
				el.setUri(an.getUri());
				List<String> types = an.getTypes();
				if (types == null) {
					types = Collections.emptyList();
				}
				el.setTypes(types);
				boolean found = false;
				// Put the annotation on the right sentence
				for (CoreMap mm : batch) {
					int begin = mm.get(CoreAnnotations.TokensAnnotation.class).get(0).beginPosition();
					int end = mm.get(CoreAnnotations.TokensAnnotation.class)
							.get(mm.get(CoreAnnotations.TokensAnnotation.class).size() - 1).endPosition();
					if (el.beginPosition() >= begin && el.endPosition() <= end) {
						L.trace("sentence found {} {} => {} {}",
								new Object[] { begin, end, el.beginPosition(), el.endPosition() });
						if (!mm.containsKey(DBpediaEntityAnnotation.class)) {
							mm.set(DBpediaEntityAnnotation.class, new ArrayList<EntityLabel>());
						}
						mm.get(DBpediaEntityAnnotation.class).add(el);
						found = true;
						break;
					}
				}
				if(!found) {
					// This may happen as the offsets not always match!
					L.trace("sentence not found, looking into surface forms");
					for (CoreMap mm : batch) {
						if(mm.get(CoreAnnotations.TextAnnotation.class).contains(el.getSurfaceForm())){
							L.trace("sentence found {} {} ",
									new Object[] { el.beginPosition(), el.endPosition() });
							if (!mm.containsKey(DBpediaEntityAnnotation.class)) {
								mm.set(DBpediaEntityAnnotation.class, new ArrayList<EntityLabel>());
							}
							mm.get(DBpediaEntityAnnotation.class).add(el);
							found = true;
							break;
						}
					}
				}
				if(!found) {
					L.error("Could not find sentence for entity: {}", el);
				}
			}

			if (updateCache) {
				cache.set_cache(key, r);
			}
		} catch (IOException e) {
			L.error("Interaction with service failed.", e);
			throw e;
		}

	}

	@Override
	public Set<Class<? extends CoreAnnotation>> requirementsSatisfied() {
		return Collections.unmodifiableSet(new ArraySet<>(Arrays.asList(DBpediaEntityAnnotation.class)));
	}

	@Override
	public Set<Class<? extends CoreAnnotation>> requires() {
		return Collections.unmodifiableSet(new ArraySet<>(Arrays.asList(CoreAnnotations.TextAnnotation.class,
				CoreAnnotations.TokensAnnotation.class, CoreAnnotations.SentencesAnnotation.class)));
	}

	public class EntityLabel extends CoreLabel {
		public String getUri() {
			return uri;
		}

		public void setUri(String uri) {
			this.uri = uri;
		}

		public List<String> getTypes() {
			return types;
		}

		public void setTypes(List<String> types) {
			this.types = types;
		}
		
		public void setSurfaceForm(String form) {
			this.surfaceForm = form;
		}

		public String getSurfaceForm() {
			return this.surfaceForm;
		}
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		private String uri;
		private List<String> types;
		private String surfaceForm;
		
		public String toString() {
			return uri;
		}
	}
}
