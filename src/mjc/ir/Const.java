package mjc.ir;

import java.util.Map;
import java.util.HashMap;

public class Const extends IRNode {
	public static final Map<Integer, Const> map = new HashMap<Integer, Const>();

	public static final Const ZERO = Const.create(0);
	public static final Const ONE = Const.create(1);
	public static final Const TWO = Const.create(2);

	public final int value;

	public static Const create(int val) {
		if (map.containsKey(val)) {
			return map.get(val);
		}

		Const v = new Const(val);

		map.put(val, v);

		return v;
	}

	private Const(int v) {
		value = v;
	}

	public Const negate() {
		return create(-value);
	}

	public int getValue() {
		return value;
	}

	public String toString() {
		return Integer.toString(value);
	}

	public void toString(StringBuilder sb) {
		sb.append(value);
	}
}
