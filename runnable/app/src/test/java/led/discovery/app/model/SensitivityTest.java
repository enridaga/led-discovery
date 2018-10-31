package led.discovery.app.model;

import java.math.BigDecimal;
import java.math.RoundingMode;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import led.discovery.app.resources.Discover;

public class SensitivityTest {
	public final static Logger L = LoggerFactory.getLogger(SensitivityTest.class);

	@Test
	public void test() {
		double valMax = 1.940708689499091;
		double valMin = 0.01;
		double reset = 0.507150;
		double max = 10.0;
		double min = 1.0;

		// First 6 elements
		testing(6, 6, reset, valMax);
		testing(5, 6, reset, valMax);
		testing(4, 6, reset, valMax);
		testing(3, 6, reset, valMax);
		testing(2, 6, reset, valMax);
//		testing(1, 6, reset, valMax);
		// Last 4
		testing(5, 5, valMin, reset);
		testing(4, 5, valMin, reset);
		testing(3, 5, valMin, reset);
		testing(2, 5, valMin, reset);
		testing(1, 5, valMin, reset);
		
	}

	public void testing(double step, double steps, double minVal, double maxVal) {
		L.info("{} -> {}", step, Discover.sensitivityValue(step, steps, minVal, maxVal));
	}

	
	

}
