package led.discovery.spark;

public interface Predictor {

	/**
	 * NLP done before!
	 * @param text
	 * @return
	 */
	boolean isLED(String text);

}