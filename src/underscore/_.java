package underscore;

import java.lang.StringBuilder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class _ {
	public static <T> String join(List<T> list) {
		return join(list, "");
	}

	public static <T> String join(List<T> list, String sep) {
		if (list.size() == 0) {
			return "";
		} else if (list.size() == 1) {
			return list.get(0).toString();
		}

		StringBuilder sb = new StringBuilder(list.get(0).toString());

		for (int i = 1; i < list.size(); i++) {
			sb.append(sep);
			sb.append(list.get(i).toString());
		}

		return sb.toString();
	}

	public static <T> String join(T[] arr) {
		return join(Arrays.asList(arr));
	}

	public static <T> String join(T[] arr, String sep) {
		return join(Arrays.asList(arr), sep);
	}

	public static <F, T> List<T> map(Collection<F> list, Mapper<F, T> mapper) {
		ArrayList<T> res = new ArrayList<T>(list.size());

		if (list.size() == 0) {
			return res;
		}

		for (F elem : list) {
			res.add(mapper.map(elem));
		}

		return res;
	}

	public static <F, T> List<T> map(Collection<F> list, Map<F, T> map) {
		ArrayList<T> res = new ArrayList<T>(list.size());

		if (list.size() == 0) {
			return res;
		}

		for (F elem : list) {
			res.add(map.get(elem));
		}

		return res;
	}

	public static <T> List<T> filter(List<T> target, Pred<T> predicate) {
		List<T> result = new ArrayList<T>();

		for (T element : target) {
			if (predicate.apply(element)) {
				result.add(element);
			}
		}

		return result;
	}
}
