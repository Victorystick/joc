// EXT:CEQ

class Main {
	public static void main(String[] args) {
		List l;

		System.out.println(false);
		l = new ArrayList().init();

		l = l.test();

		System.out.println(false);
		l = new LinkedList().init();

		l = l.test();
	}
}

class List {
	public List init() { return this; }
	public List add(int val) { return this; }
	public int size() { return 0; }
	public int cap() { return 0; }
	public boolean isEmpty() { return true; }
	public int get(int i) { return 0; }
	public int set(int i, int v) { return 0; }

	public List test() {
		List _;
		int i;

		_ = this;

		System.out.println(_.size());
		System.out.println(_.cap());

		i = 0;
		while (i < 5) {
			_ = _.add(i + 7);
			i = i + 1;
		}

		System.out.println(_.size());

		while (0 < i) {
			i = i - 1;
			System.out.println(_.get(i));
		}

		return _;
	}
}

class ArrayList extends List {
	int[] arr;
	int s;

	public List init() {
		s = 0;
		arr = new int[4];
		return this;
	}

	public List add(int val) {
		List _;
		int[] arr_old;
		int i;

		if (s < arr.length) {
			arr[s] = val;
			s = s + 1;
		} else {
			arr_old = arr;
			i = 0;
			arr = new int[arr_old.length * 2];

			while (i < arr_old.length) {
				arr[i] = arr_old[i];
				i = i + 1;
			}

			_ = this.add(val);
		}

		return this;
	}

	public int size() {
		return s;
	}

	public int cap() {
		return arr.length;
	}

	public boolean isEmpty() {
		return s == 0;
	}

	public int get(int i) {
		return arr[i];
	}

	public int set(int i, int val) {
		int old;

		old = arr[i];
		arr[i] = val;

		return old;
	}
}

class LinkedList extends List {
	public List add(int val) {
		List l;
		l = new LLNode().initWithLL(this);
		val = l.set(0, val);
		return l;
	}
}

class LLNode extends List {
	int val;
	List next;

	public List initWithLL(LinkedList n) {
		next = n;
		return this;
	}

	public List add(int i) {
		next = next.add(i);
		return this;
	}

	public int size() {
		return next.size() + 1;
	}

	public boolean isEmpty() {
		return false;
	}

	public int get(int i) {
		if (i == 0) {
			i = val;
		} else {
			i = next.get(i - 1);
		}

		return i;
	}

	public int set(int i, int v) {
		if (i == 0) {
			val = v;
		} else {
			val = next.set(i - 1, v);
		}

		return v;
	}
}
