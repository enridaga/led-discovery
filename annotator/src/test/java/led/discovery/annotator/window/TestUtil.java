package led.discovery.annotator.window;

import edu.stanford.nlp.ling.CoreAnnotations.CharacterOffsetBeginAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.CharacterOffsetEndAnnotation;
import edu.stanford.nlp.util.ArrayCoreMap;
import edu.stanford.nlp.util.CoreMap;

public class TestUtil {
	public final static CoreMap S1 = sentence(0, 1000);
	public final static CoreMap S2 = sentence(1001, 2000);
	public final static CoreMap S3 = sentence(2001, 3000);
	public final static CoreMap S4 = sentence(3001, 4000);
	public final static CoreMap S5 = sentence(4001, 5000);
	public final static CoreMap S6 = sentence(5001, 6000);
	public final static CoreMap S7 = sentence(6001, 7000);
	public final static CoreMap S8 = sentence(7001, 8000);
	public final static CoreMap S9 = sentence(8001, 9000);
	public final static CoreMap S10 = sentence(9001, 10000);
	public final static CoreMap S11 = sentence(10001, 11000);
	public final static CoreMap S12 = sentence(11001, 12000);
	public final static CoreMap S13 = sentence(12001, 13000);
	public final static CoreMap S14 = sentence(13001, 14000);
	public final static CoreMap S15 = sentence(14001, 15000);
	public final static CoreMap S16 = sentence(15001, 16000);
	public final static CoreMap S17 = sentence(16001, 17000);
	public final static CoreMap S18 = sentence(17001, 18000);
	public final static CoreMap S19 = sentence(18001, 19000);
	public final static CoreMap S20 = sentence(19001, 20000);
	// Evaluators
	public final static TextWindowEvaluator PassAll = new TextWindowEvaluator() {

		@Override
		public boolean pass(TextWindow w) {
			return true;
		}
	};

	public static CoreMap sentence(int f, int t) {
		CoreMap cm = new ArrayCoreMap();
		cm.set(CharacterOffsetBeginAnnotation.class, f);
		cm.set(CharacterOffsetEndAnnotation.class, t);
		return cm;
	}

}
