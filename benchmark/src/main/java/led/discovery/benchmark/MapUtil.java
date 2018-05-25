package led.discovery.benchmark;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class MapUtil {
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