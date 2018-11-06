package led.discovery.app.model;

import java.math.BigDecimal;
import java.math.RoundingMode;

import org.apache.commons.math3.distribution.NormalDistribution;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import led.discovery.app.resources.Discover;

public class SensitivityTest {
	public final static Logger L = LoggerFactory.getLogger(SensitivityTest.class);
//
//	// @Test
//	public void test() {
//		double valMax = 1.940708689499091;
//		double valMin = 0.01;
//		double reset = 0.507150;
//		double max = 10.0;
//		double min = 1.0;
//
//		// First 6 elements
//		testing(6, 6, reset, valMax);
//		testing(5, 6, reset, valMax);
//		testing(4, 6, reset, valMax);
//		testing(3, 6, reset, valMax);
//		testing(2, 6, reset, valMax);
////		testing(1, 6, reset, valMax);
//		// Last 4
//		testing(5, 5, valMin, reset);
//		testing(4, 5, valMin, reset);
//		testing(3, 5, valMin, reset);
//		testing(2, 5, valMin, reset);
//		testing(1, 5, valMin, reset);
//
//	}

	@Test
	public void test2() {
		double valMax = 1.0; //1.940708689499091;
		double valMin = 0.01;
		double reset = 0.507150;

		for (int x = 100; x > 0; x--) {
			testing2(x, 100, valMin, 1, reset);
		}
	}

//	public void testing(double step, double steps, double minVal, double maxVal) {
//		L.info("{} -> {}", step, Discover.sensitivityValue(step, steps, minVal, maxVal));
//	}

	public void testing2(double step, double steps, double minVal, double maxVal, double mean) {
		L.info("{} -> {}", step, scale(step, steps, minVal, maxVal, mean));
	}

	public double scale(double step, double steps, double minVal, double maxVal, double mean) {
		int middle = Math.round(Math.round(steps / 2));
		double factor;
		if (step == middle) {
			return mean;
		} else if (step == steps) {
			return maxVal;
		} else if (step > middle) {
//			return ((maxVal - minVal) * (step - 1) / (steps - 1)) + minVal;
			// Above mean
			double linear = ((maxVal - mean) * (step - middle - 1) / (middle - 1)) + mean;
			double linearStep = (maxVal - mean) / middle;
			factor = linearStep / (step - middle);
			L.debug("linear {} linearStep {} factor {}", new Object[] { linear, linearStep, factor });
			return linear - factor;
		} else if (step < middle && !(step < 1)) {
			// Below mean
			double linear = ((mean - minVal) * (step - 1) / (middle - 1)) + minVal;
			double linearStep = (mean - minVal) / middle;
			factor = linearStep / (middle - step);
			L.debug("linear {} linearStep {} factor {}", new Object[] { linear, linearStep, factor });
			return linear + factor;
		}
		return minVal;
	}
}
