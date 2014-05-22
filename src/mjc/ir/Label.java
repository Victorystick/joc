package mjc.ir;

import java.util.Map;
import java.util.HashMap;

public class Label implements IRStatement {
	public static Map<String, Label> map = new HashMap<String, Label>();
	private static int counter = 0;
	public String label;
	public Name name;

	Label(String name) {
		label = name;
	}

	public static Label create(String key) {
		if (map.containsKey(key)) {
			return map.get(key);
		}

		Label label = new Label(key);

		map.put(key, label);

		return label;
	}

	public static Label create() {
		return create(".Label" + counter++);
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();
		toString(sb);
		return sb.toString();
	}

	public void toString(StringBuilder sb) {
		sb.append(String.format("{\"label\": \"%s\"}", label));
	}

	public Name getName() {
		if (name == null) {
			name = new Name(this);
		}

		return name;
	}

	public NodeList children() {
		return null;
	}

	public IRStatement build(NodeList nodes) {
		return this;
	}
}
