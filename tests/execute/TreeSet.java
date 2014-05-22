// EXT:ISC
// EXT:ICG
// EXT:IWE

/**
 * Main class decl
 * //Line comment here should be ok.
 */
class Main {
	public static void main(String[] args) {
		Set set;
		int i;

		// new TreeSet(0)
		set = new TreeSet().init(0);

		set = set.insert(0 - 1);
		set = set.insert(1);
		set = set.insert(7);

		i = 0 - 7;
		while (i < 8) {
			System.out.println(set.contains(i));
			i = i + 1;
		}
	}
}

/**
 * Set Interface.
 */
class Set {
	public Set insert(int i) {
		return this;
	}

	public boolean contains(int i) {
		return false;
	}
}

/**
 * Null node in a TreeSet.
 */
class NullNode extends Set {
	public Set insert(int i) {
		return new TreeSet().initWithNull(i, this);
	}
}

/**
 * TreeSet
 */
class TreeSet extends Set {
	int value;

	Set left;
	Set right;

	// This is basically a constructor.
	public TreeSet init(int v) {
		return this.initWithNull(v, new NullNode());
	}

	// "private" constructor.
	public TreeSet initWithNull(int v, NullNode nul) {
		value = v;
		left = nul;
		right = nul;

		return this;
	}

	public Set insert(int v) {
		TreeSet _;

		if (v < value) {
			left = left.insert(v);
		} else if (value < v) {
			right = right.insert(v);
		}

		return this;
	}

	public boolean contains(int v) {
		boolean val;

		if (v < value) {
			val = left.contains(v);
		} else if (value < v) {
			val = right.contains(v);
		} else {
			val = true;
		}

		return val;
	}
}
