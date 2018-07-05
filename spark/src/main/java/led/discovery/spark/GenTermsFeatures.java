package led.discovery.spark;

import java.io.FileNotFoundException;
import java.io.IOException;

import org.apache.commons.lang.StringUtils;

/**
 * Generating Bags of Terms (Lemma+POS)
 * 
 * @author enridaga
 *
 */
public class GenTermsFeatures extends GenFeaturesVectorAbstract {
	public GenTermsFeatures(String[] args) throws IOException {
		super(args);
	}

	protected String prepareFeatures(String sourceId, String txt, boolean positive) {
		L.debug("txt length {}", txt.length());
		String[] split = getFeaturesFactory().aterms(txt);
		L.debug("{} terms", split.length);
		return StringUtils.join(split, " ");
	}

	public static void main(String[] args) throws FileNotFoundException, IOException {
		new GenTermsFeatures(args).run();
	}
}
