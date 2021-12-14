package led.discovery.analysis.entities.spot;

import org.junit.Test;

public class RegexTest {
	@Test
	public void test() {
		String input = "Enrico&#12;Francesca&amp;Christ&# ";
		input = input.replaceAll("&[^\\s;]+[\\s;]", " ");
		System.out.println(input);
	}

	@Test
	public void testXMLDeclaration(){
		String xml ="<?xml version='1.0' ?> <tag>som text</tag>";
		xml = xml.replaceFirst("<\\?xml.*\\?>","<?xml version=\"1.1\" encoding=\"UTF-8\" ?>");
		System.out.println(xml);
	}
}
