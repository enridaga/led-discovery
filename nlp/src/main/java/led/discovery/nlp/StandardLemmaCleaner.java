package led.discovery.nlp;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StandardLemmaCleaner implements LemmaCleaner {
	private Logger log = LoggerFactory.getLogger(StandardLemmaCleaner.class);
	public final String NUMBER = "<NUMBER>";
	public final String CHARSEQ = "<CHARSEQ>";
	public final Pattern _end_char_not_alphanumeric = Pattern.compile("[^a-zA-Z0-9]$");
	public final Pattern _start_char_not_alphanumeric = Pattern.compile("^[^a-zA-Z0-9]");
	public final Pattern _is_a_number = Pattern.compile("^([\\+\\-]{1})?([0-9]+)(([\\\\.,]{1})[0-9]+)?$");
	@Override
	public String clean(String lemma) {
		
		// IGNORE TERMS LONGER THEN 20
		if (lemma.length() > 20) {
			log.trace("skipping term longer then 20 chars: {}", lemma);
			return null;
		}

		// Remove end char if not alphanumeric
		if (_end_char_not_alphanumeric.matcher(lemma).find()) {
			log.trace("removing end char from: {}", lemma);
			lemma = lemma.substring(0, lemma.length() - 1);
		}

		// Remove start char if not alphanumeric
		if (_start_char_not_alphanumeric.matcher(lemma).find()) {
			log.trace("removing start char from: {}", lemma);
			lemma = lemma.substring(1);
		}

		// NUMBERS are just numbers
		if (_is_a_number.matcher(lemma).find()) {
			log.trace("is number: {}", lemma);
			return NUMBER;
		}

		// ACCEPT at most 1 NON ASCII character
		if (tooMany(lemma, "[^\\p{ASCII}]", 1)) {
			log.trace("too many non ascii: {}", lemma);
			return null;
		}
		
		// TOO MANY NON CHARS
		if (tooMany(lemma, "[^a-zA-Z]", 1)) {
			log.trace("too many non letters: {}", lemma);
			return null;
		}

		// IGNORE TERMS SHORTER THEN 3
		if (lemma.length() <= 3) {
			log.trace("skipping term shorter then 3 chars: {}", lemma);
			return null;
		}

		// REPLACE TABS AND NEWLINES TO SPACES
		lemma = lemma.replaceAll("[\\n\\t]", " "); 
		return lemma;
	}
	
	public boolean tooMany(String input, String regex, int limit) {
		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(input);
		int from = 0;
		int count = 0;
		while (matcher.find(from)) {
			count++;
			from = matcher.start() + 1;
			if(count > limit) {
				return true;
			}
		}
		return false;
	}
}
