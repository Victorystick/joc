class Main {
	public static void main(String[] args) {
		Not n;
		boolean b;
		boolean c;

		n = new Not();

		b = true;
		c = !(b);

		System.out.println(b != c);

		b = false;
		c = !(b);

		System.out.println(b != c);

		b = true;
		c = n.ot(b);

		System.out.println(b != c);

		b = false;
		c = n.ot(b);

		System.out.println(b != c);
	}
}

class Not {
	public boolean ot(boolean b) {
		if (b) {
			b = false;
		} else {
			b = true;
		}
		return b;
		// return !b;
	}
}
