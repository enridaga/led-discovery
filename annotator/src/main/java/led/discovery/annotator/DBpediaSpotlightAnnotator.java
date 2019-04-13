package led.discovery.annotator;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
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
	private Logger log = LoggerFactory.getLogger(DBpediaSpotlightAnnotator.class);
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

	public DBpediaSpotlightAnnotator(String name, Properties props) {
		// load the lemma file
		// format should be tsv with word and lemma
		service = props.getProperty("custom.spotlight.service");
		cache = new SimpleCache(new File(props.getProperty("custom.cache", ".simple_cache")));
		cache.set_cache_extension(".spotlight");
		confidence = Double.parseDouble(props.getProperty("custom.spotlight.confidence", "0.2"));
		support = Integer.parseInt(props.getProperty("custom.spotlight.support", "0"));
		if (service == null) {
			service = "http://anne.kmi.open.ac.uk/rest/annotate";
		}
		// cache = props.getProperty("custom.spotlight.cache");
		client = new SpotlightClient(service);
	}

	@Override
	public void annotate(Annotation annotation) {
		List<CoreMap> sentences = annotation.get(SentencesAnnotation.class);
		for (CoreMap sentence : sentences) {
			log.trace("{}", sentence);
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
					log.trace("{} [{}]", an.getUri(), an.getConfidence());
					EntityLabel el = new EntityLabel();
					el.setBeginPosition(sentenceOffset + an.getOffset());
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
				log.error("Interaction with service failed.", e);
				break;
			}
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

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		private String uri;
		private List<String> types;

		public String toString() {
			return uri;
		}
	}
}
