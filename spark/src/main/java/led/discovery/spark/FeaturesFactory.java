package led.discovery.spark;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.safety.Whitelist;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import led.discovery.analysis.entities.spot.SpotlightAnnotation;
import led.discovery.analysis.entities.spot.SpotlightClient;
import led.discovery.analysis.entities.spot.SpotlightResponse;
import led.discovery.nlp.StanfordNLPProvider;
import led.discovery.nlp.Term;

public class FeaturesFactory {
	private static final Logger L = LoggerFactory.getLogger(FeaturesFactory.class);
	private StanfordNLPProvider provider = new StanfordNLPProvider();

	public String[] terms(String text) {
		L.trace("{}", text);
		List<Term> terms = provider.terms(text);
		List<String> termss = new ArrayList<String>();
		for (Term t : terms) {
			termss.add(t.toString());
		}
		L.trace("{}", termss);
		return termss.toArray(new String[termss.size()]);
	}

	public String[] aterms(String text) {
		L.trace("{}", text);
		List<Term> terms = provider.terms(text);
		List<String> termss = new ArrayList<String>();
		for (Term t : terms) {
			termss.add(t.toAString());
		}
		L.trace("{}", termss);
		return termss.toArray(new String[termss.size()]);
	}

	public String[] entities(String txt, SpotlightClient spotlight) {
		L.trace("{}", txt);
		List<SpotlightAnnotation> ann;
		try {
			String xml;
			String text = Jsoup.clean(txt, Whitelist.simpleText());
			xml = spotlight.perform(text).getXml().replaceAll("&[^\\s;]+[\\s;]", " ");
			ann = SpotlightResponse.asList(xml, 0.4);
		} catch (IOException e) {
			L.error("Spotlight error", e);
			return new String[] {};
		}
		List<String> termss = new ArrayList<String>();
		for (SpotlightAnnotation a : ann) {
			termss.add(a.getUri());
		}
		L.trace("{}", termss);
		return termss.toArray(new String[termss.size()]);
	}
}
