package led.discovery.annotator;

import java.io.File;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//import com.amazonaws.util.IOUtils;

import edu.stanford.nlp.ling.CoreAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.Annotator;
import edu.stanford.nlp.util.ArraySet;
import edu.stanford.nlp.util.CoreMap;
import edu.stanford.nlp.util.ErasureUtils;

import led.discovery.annotator.DBpediaSpotlightAnnotator.DBpediaEntityAnnotation;
import led.discovery.annotator.DBpediaSpotlightAnnotator.EntityLabel;
import led.discovery.annotator.apicache.SimpleCache;

public class MusicalEntityAnnotator implements Annotator {
	private Logger log = LoggerFactory.getLogger(MusicalEntityAnnotator.class);
	private String service;
	private int distance_from = 0;
	private int distance_to = 5;
	private int bunch = 5;
	private SimpleCache cache;

	/**
	 * A URI
	 *
	 */
	public final class MusicalEntityAnnotation implements CoreAnnotation<Set<String>> {
		@Override
		public Class<Set<String>> getType() {
			return ErasureUtils.uncheckedCast(Set.class);
		}
	}

	public final class MusicalEntityRatioAnnotation implements CoreAnnotation<Double> {
		@Override
		public Class<Double> getType() {
			return ErasureUtils.uncheckedCast(Double.class);
		}
	}

	public MusicalEntityAnnotator(String name, Properties props) {
		service = props.getProperty("custom.dbpedia.service");
		cache = new SimpleCache(new File(props.getProperty("custom.cache", ".simple_cache")));
		cache.set_cache_extension(".entities");
		if (service == null) {
			service = "http://dbpedia.org/sparql";
		}
	}

	private static final String QUERY_A = "PREFIX dbc: <http://dbpedia.org/resource/Category:> "
			+ "PREFIX dct: <http://purl.org/dc/terms/> " + "PREFIX skos: <http://www.w3.org/2004/02/skos/core#> "
			+ "SELECT  distinct ?sub " + "WHERE { " + "  VALUES ?sub { ";
	private static final String QUERY_B = " } . " + " ?sub dct:subject ?subject . " + " ?subject skos:broader{";
	private static final String QUERY_C = ",";
	private static final String QUERY_D = "} dbc:Music " + "}";

	public Set<String> testBunch(Set<String> entUris) {
		StringBuilder queryBuilder = new StringBuilder().append(QUERY_A);
		for (String uri : entUris) {
			queryBuilder.append('<').append(uri).append('>').append(" ");
		}
		queryBuilder.append(QUERY_B);
		queryBuilder.append(distance_from);
		queryBuilder.append(QUERY_C);
		queryBuilder.append(distance_to);
		queryBuilder.append(QUERY_D);
		String sparqlQuery = queryBuilder.toString();
		log.trace("{} {}", service, sparqlQuery);

		// Cache
		boolean updateCache = false;
		String key = SimpleCache.hashLabel(sparqlQuery);
		String result;
		if (cache.is_cached(key)) {
			result = cache.get_cache(key);
		} else {
			updateCache = true;

			HttpClient httpclient = HttpClients.createDefault();
			HttpPost httppost = new HttpPost(service);
			// Request parameters and other properties.
			List<NameValuePair> params = new ArrayList<NameValuePair>(2);
			params.add(new BasicNameValuePair("query", sparqlQuery));
			params.add(new BasicNameValuePair("format", "text/csv"));

			try {
				httppost.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));
				// Execute and get the response.
				HttpResponse response = httpclient.execute(httppost);
				HttpEntity entity = response.getEntity();
				try (InputStream instream = entity.getContent()) {
					// TODO Check headers ...
					result = IOUtils.toString(instream, StandardCharsets.UTF_8);
				}
				if (updateCache) {
					cache.set_cache(key, result);
				}
			} catch (Exception e) {
				log.error("", e);
				return Collections.emptySet();
			}
		}

		Set<String> uris = new HashSet<String>();
		String[] lines = result.split("\\r?\\n");
		List<String> found = new ArrayList<String>();
		// remove quotes
		found.addAll(Arrays.asList(lines));
		found.remove(0);
		log.trace("{}", found);
		for (String l : found)
			uris.add(l.substring(1, l.length() - 1));

//		uris.addAll(found);
		return uris;
	}

	@Override
	public void annotate(Annotation annotation) {
		List<CoreMap> sentences = annotation.get(SentencesAnnotation.class);
		for (CoreMap sentence : sentences) {
			log.debug("{}", sentence);
			List<EntityLabel> entities = sentence.get(DBpediaEntityAnnotation.class);

			Set<String> allUris = new HashSet<String>();

			for (EntityLabel l : entities) {
				if (!allUris.contains(l.getUri())) {
					allUris.add(l.getUri());
				}
			}
			Set<String> uris = new HashSet<String>();
			Set<String> abunch = new HashSet<String>();
			// Do a query once in a while (bunch)
			for (String s : allUris) {
				if (abunch.size() < bunch) {
					abunch.add(s);
				} else {
					uris.addAll(testBunch(abunch));
					abunch.clear();
					abunch.add(s);
				}
			}
			log.debug("musical entities: {}", uris);
			sentence.set(MusicalEntityAnnotation.class, uris);
			sentence.set(MusicalEntityRatioAnnotation.class, ((double) uris.size() / (double) allUris.size()));
		}
	}

	@Override
	public Set<Class<? extends CoreAnnotation>> requirementsSatisfied() {
		return Collections
				.unmodifiableSet(new ArraySet<>(MusicalEntityAnnotation.class, MusicalEntityRatioAnnotation.class));
	}

	@Override
	public Set<Class<? extends CoreAnnotation>> requires() {
		return Collections.unmodifiableSet(new ArraySet<>(Arrays.asList(DBpediaEntityAnnotation.class)));
	}
}
