package led.discovery.benchmark;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.text.similarity.LevenshteinDistance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExcerptFinder {
	private static final Logger log = LoggerFactory.getLogger(ExcerptFinder.class);
	private static final Pattern REMOVE_SCRIPT = Pattern.compile("<script .*?>.*?</script>", Pattern.CASE_INSENSITIVE |
		Pattern.DOTALL | Pattern.MULTILINE);
	private static final Pattern REMOVE_STYLES = Pattern.compile("<style .*?>.*?</style>", Pattern.CASE_INSENSITIVE |
		Pattern.DOTALL | Pattern.MULTILINE);
	private static final Pattern REMOVE_CSSSTYLES = Pattern.compile("[A-Za-z0-9\\-\\_]+:[A-Za-z0-9\"\\%\\,\\-\\_\\s\\.]+?;", Pattern.CASE_INSENSITIVE |
		Pattern.DOTALL | Pattern.MULTILINE);
	private static final Pattern REMOVE_TAGS = Pattern.compile("<.*?>", Pattern.CASE_INSENSITIVE |
		Pattern.DOTALL | Pattern.MULTILINE);
	private static final Pattern REMOVE_ENTS = Pattern.compile("&.*?;");

	public static String removeTags(String in) {

		if (in == null || in.length() == 0) {
			return in;
		}

		Matcher m;
		m = REMOVE_SCRIPT.matcher(in);
		m = REMOVE_STYLES.matcher(m.replaceAll(""));
		m = REMOVE_CSSSTYLES.matcher(m.replaceAll(""));
		m = REMOVE_TAGS.matcher(m.replaceAll(""));
		m = REMOVE_ENTS.matcher(m.replaceAll(" "));
		String out = m.replaceAll(" ").replaceAll("\\s+", " ");
		log.trace("removeTags {} >>> {}", in, out);
		return out;
	}

	public static Bookmark find(String experience, String content) {
		return find(experience, content, 0);
	}

	public static Bookmark find(String experience, String content, int startFrom) {
		experience = removeTags(experience);
		/**
		 * We build a Vector of words from the experience, and we save the
		 * length of the word (wlen) and its offset in the experience string
		 * (wpos)
		 */
		int experienceLength = experience.length();
		String[] wordvec = experience.split("[\\s;\\.,'\"]+");
		Map<Integer, Integer> options = new HashMap<Integer, Integer>();
		// Pick the first 5 longest words
		Map<Integer, Integer> wlen = new HashMap<Integer, Integer>();
		Map<Integer, Integer> wpos = new HashMap<Integer, Integer>();
		int epos = 0;
		for (int w = 0; w < wordvec.length; w++) {
			wlen.put(w, wordvec[w].length());
			wpos.put(w, experience.indexOf(wordvec[w], epos));
			epos = wpos.get(w) + wlen.get(w);
		}
		// We sort the words by length desc
		wlen = MapUtil.sortByValueDesc(wlen);
		//
		double lastScore = 1.0;
		for (Entry<Integer, Integer> ken : wlen.entrySet()) {
			int x = ken.getKey();
			log.debug("> Reference word: {}", wordvec[x]);
			// Only consider words longer then 5 chars
			if (wordvec[x].length() < 6) {
				break;
			}
			// We search the word in the content and iterate over its
			// occurrences, we use the experience length as moving window
			for (int seek = startFrom; seek < content.length() - 1; seek += experienceLength) {
				// Position of the word in the content
				int wordpos = content.indexOf(wordvec[x], seek);
				int pos = wordpos - wpos.get(x); // potential position of the excerpt
				log.trace("{}", pos);
				if (pos > 0) {
					// Let's move to the beginning of the word
					while (pos > 1 && (!content.substring(pos - 1, pos).equals(" "))) {
						pos -= 1;
					}
					// Test
					String extracted;
					if ((content.length() < pos + experienceLength)) {
						extracted = content.substring(pos);
					} else {
						extracted = content.substring(pos, pos + experienceLength);
					}
					int distance = LevenshteinDistance.getDefaultInstance().apply(experience, extracted);
					double thisScore = Bookmark.getNormalisedScore(distance, extracted.length());
					if(thisScore < 0.5) {
						log.debug("{}", extracted);
					}
					if (thisScore < lastScore) {
						options.put(pos, distance);
						lastScore = thisScore;
					}
					seek = pos + extracted.length() - 1;
				} else {
					// pos is negative, word is not found
					break;
				}
			}
			// 0.3 is a good enough score
			if (lastScore < 0.3) {
				break;
			}
		}
		log.debug("> {} options", options.size());
		Map<Integer, Integer> sorted = MapUtil.sortByValue(options);
		for (Entry<Integer, Integer> entry : sorted.entrySet()) {
			int pos = entry.getKey();
			int to;
			if ((content.length() < pos + experienceLength)) {
				to = content.length() - 1;
			} else {
				to = pos + experienceLength;
			}
			log.trace(" >> --: {}", experience);
			log.trace(" << {}: {}", entry.getValue(), content.substring(pos, to).replaceAll("\n", " "));
			return new Bookmark(pos, to, entry.getValue());
		}
		return Bookmark.empty;
	}

	public static class MapUtil {
		public static <K, V extends Comparable<? super V>> Map<K, V> sortByValueDesc(Map<K, V> map) {
			return sortByValue(map, false);
		}

		public static <K, V extends Comparable<? super V>> Map<K, V> sortByValue(Map<K, V> map, boolean ascendent) {
			List<Entry<K, V>> list = new ArrayList<>(map.entrySet());
			list.sort(Entry.comparingByValue());

			// If reverted
			if (!ascendent) {
				@SuppressWarnings("unchecked")
				Entry<K, V>[] li = list.toArray(new Entry[list.size()]);
				Arrays.sort(li, new Comparator<Entry<K, V>>() {
					public int compare(Entry<K, V> o1, Entry<K, V> o2) {
						return o2.getValue().compareTo(o1.getValue());
					};
				});
				list.removeAll(list);
				list.addAll(Arrays.asList(li));
			}
			Map<K, V> result = new LinkedHashMap<>();
			for (Entry<K, V> entry : list) {
				result.put(entry.getKey(), entry.getValue());
			}

			return result;
		}

		public static <K, V extends Comparable<? super V>> Map<K, V> sortByValue(Map<K, V> map) {
			return sortByValue(map, true);
		}
	}
}
