package mjc.ir;

import java.util.ArrayList;
import java.util.List;

public class Temp extends IRNode {
	private static int counter = 0;
	private static final List<Temp> all = new ArrayList<Temp>();
	public static final List<Temp> nonregisters = new ArrayList<Temp>();

	public final int id;
	public final String name;

	private Temp(int i, String n) {
		id = i;
		name = n;
	}

	private Temp(int i) {
		this(i, null);
	}

	public static Temp get(int i) {
		return all.get(i);
	}

	public static Temp create(boolean reg, String name) {
		Temp t = new Temp(counter++, name);

		if (!reg) {
			nonregisters.add(t);
		}
		all.add(t);

		return t;
	}

	/**
	 * Creates a Temp for a named variable,
	 * letting it be tracked throughout compilation.
	 */
	public static Temp create(String name) {
		return create(false, name);
	}

	public static Temp create(boolean reg) {
		return create(reg, null);
	}

	public static Temp create() {
		return create(false);
	}

	public static int numberOfTemps() {
		return counter;
	}

	public void toString(StringBuilder sb) {
		sb.append(String.format("\"temp_%d\"", id));
	}

	public String getName() {
		return name != null ? name : String.format("\"temp_%d\"", id);
	}

	public NodeList children() {
		return NodeList.emptyList();
	}

	public IRNode build(NodeList _) {
		return this;
	}
}
