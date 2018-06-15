package led.discovery.analysis.entities.spot;

import org.junit.Test;

public class RegexTest {
	@Test
	public void test() {
		String input = "Enrico&#12;Francesca&amp;Christ&# ";
		input = input.replaceAll("&[^\\s;]+[\\s;]", " ");
		System.out.println(input);
	}
}
