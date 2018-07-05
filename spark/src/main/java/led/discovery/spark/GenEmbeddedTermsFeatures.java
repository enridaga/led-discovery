package led.discovery.spark;

import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * Generating Bags of Terms (Lemma+POS)
 * 
 * @author enridaga
 *
 */
public class GenEmbeddedTermsFeatures extends GenTermsFeatures {
	public GenEmbeddedTermsFeatures(String[] args) throws IOException {
		super(args);
		useWordEmbeddings();
	}
	public static void main(String[] args) throws FileNotFoundException, IOException {
		new GenEmbeddedTermsFeatures(args).run();
	}
}
