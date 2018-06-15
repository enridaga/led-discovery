package led.discovery.spark;

import java.io.FileNotFoundException;
import java.io.IOException;

import org.apache.commons.lang.StringUtils;

import led.discovery.analysis.entities.spot.SpotlightClient;

/**
 * Generating Bags of Terms (Lemma+POS)
 * 
 * @author enridaga
 *
 */
public class GenEntitiesFeatures extends GenFeaturesVectorAbstract {
	private SpotlightClient client = null;

	public GenEntitiesFeatures(String[] args) throws IOException {
		super(args);
		client = new SpotlightClient("http://anne.kmi.open.ac.uk/rest/annotate");
	}

	protected String prepare(String txt) {
		L.debug("txt length {}", txt.length());
		String[] s = getFeaturesFactory().entities(txt, client);
		L.debug("{} entities", s.length);
		return StringUtils.join(s, " ");
	}

	public static final void main(String[] args) throws FileNotFoundException, IOException {
		new GenEntitiesFeatures(args).run();
	}
}
