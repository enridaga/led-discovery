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

	protected String prepare(String txt) {
		L.debug("txt length {}", txt.length());
		String[] split = getFeaturesFactory().aterms(txt);
		L.debug("{} terms", split.length);
		return StringUtils.join(split, " ");
	}

	public static final void main(String[] args) throws FileNotFoundException, IOException {
		new GenTermsFeatures(args).run();
	}
}
