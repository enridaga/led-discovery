package led.discovery.hadoop.hive;

import org.apache.hadoop.hive.ql.exec.Description;
import org.jsoup.Jsoup;

@Description(name = "RemoveHTML", value = "_FUNC_(string) - sanitizes text by removing html-like tags")
public class RemoveHTML extends StringManipulationUDF {
	@Override
	protected String getFunctionName() {
		return "RemoveHTML";
	}

	@Override
	protected String perform(String input) {
		return Jsoup.parse(input.toString()).text();
	}
}
