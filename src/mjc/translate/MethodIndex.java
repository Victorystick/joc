package mjc.translate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MethodIndex {
	private final MethodIndex parent;
	private final Map<String, Integer> map;
	private final List<String> index;

	public static MethodIndex create() {
		return new MethodIndex();
	}

	public static MethodIndex create(MethodIndex p) {
		return new MethodIndex(p);
	}

	private MethodIndex() {
		index = new ArrayList<String>();
		map = new HashMap<String, Integer>();
		parent = null;
	}

	private MethodIndex(MethodIndex p) {
		index = new ArrayList<String>(p.index);
		map = new HashMap<String, Integer>();
		parent = p;
	}

	public List<String> getList() {
		return index;
	}

	public Integer get(String id) {
		if (map.containsKey(id)) {
			return map.get(id);
		}

		if (parent != null) {
			return parent.get(id);
		}

		return -1;
	}

	public void put(String id, String label) {
		if (parent != null) {
			Integer i = parent.get(id);
			if (i > -1) {
				index.set(i, label);
				return;
			}
		}

		map.put(id, index.size());
		index.add(label);
	}

	public String toString() {
		return index.toString();
	}
}
