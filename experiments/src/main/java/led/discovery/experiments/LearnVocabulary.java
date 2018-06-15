package led.discovery.experiments;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import led.discovery.benchmark.MapUtil;
import led.discovery.nlp.StanfordNLPProvider;
import led.discovery.nlp.Term;

public class LearnVocabulary {
	private static final Logger L = LoggerFactory.getLogger(LearnVocabulary.class);
	private File input;
	private File output;
	private StanfordNLPProvider provider;
	// https://stackoverflow.com/questions/1833252/java-stanford-nlp-part-of-speech-labels
	private final String[] POS = new String[] { "V", "R", "N", "J" };

	public LearnVocabulary(String[] args) {
		input = new File(args[0]);
		output = new File(args[1]);
		provider = new StanfordNLPProvider();
	}

	public static final void main(String[] args) throws IOException {
		new LearnVocabulary(args).run();
	}

	private void run() throws IOException {
		_clean();

		L.info("Loading positive examples");
		Map<String, String> posMap = new HashMap<String, String>();
		List<Integer> plen = new ArrayList<Integer>();
		int numberOf = 0;
		for (File f : this.input.listFiles()) {
			try (FileInputStream fis = new FileInputStream(f)) {
				String content = IOUtils.toString(fis, StandardCharsets.UTF_8);
				plen.add(content.length());
				posMap.put(f.getName(), content);
				numberOf++;
			} catch (IOException e) {
				L.error("", e);
			}
		}
		L.info("{} positive examples loaded", numberOf);

		L.info("Preparing features");
		Map<String, Integer> termsCounts = new HashMap<String, Integer>();
		Map<String, Set<String>> termsInDocs = new HashMap<String, Set<String>>();
		// For each positive file
		List<String> poss = Arrays.asList(POS);
		for (Entry<String, String> n : posMap.entrySet()) {
			L.info("Doc {}", n.getKey());
			List<Term> terms = provider.terms(n.getValue());
			for (Term t : terms) {
				if (!poss.contains(t.getAPOS())) {
					continue;
				}
				if (!termsInDocs.containsKey(t.toAString())) {
					termsInDocs.put(t.toAString(), new HashSet<String>());
				}
				if (!termsCounts.containsKey(t.toAString())) {
					termsCounts.put(t.toAString(), new Integer(0));
				}
				termsInDocs.get(t.toAString()).add(n.getKey());
				Integer c = termsCounts.get(t.toAString());
				c++;
				termsCounts.put(t.toAString(), c);
			}
		}
		termsCounts = MapUtil.sortByValueDesc(termsCounts);
		Map<String, Integer> termsInDocsCounts = new HashMap<String, Integer>();
		for (Entry<String, Set<String>> entry : termsInDocs.entrySet()) {
			termsInDocsCounts.put(entry.getKey(), entry.getValue().size());
		}
		termsInDocsCounts = MapUtil.sortByValueDesc(termsInDocsCounts);
		L.info("Writing {} terms", termsInDocsCounts.size());
		try (FileWriter fw = new FileWriter(output, true)) {
			for (Entry<String, Integer> entry : termsInDocsCounts.entrySet()) {
				fw.write(entry.getKey());
				fw.write(",");
				fw.write(Integer.toString(entry.getValue()));
				fw.write(",");
				fw.write(Integer.toString(termsCounts.get(entry.getKey())));
				fw.write("\n");
			}
		}
	}

	private void _clean() {
		if (output != null && output.exists()) {
			output.delete();
		}
	}
}
