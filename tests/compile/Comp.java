class Main {
	public static void main(String[] args) {
		Comp c;
		c = new Comp();

		if (c.or(c.lt(5, 4), c.eq(13, 13))) {
			System.out.println(true);
		} else {
			System.out.println(false);
		}

		System.out.println(c.First(c.NewArr()) == 0);
		System.out.println(c.NewArr().length == 7);

		System.out.println(c.and(true, false) == (true && false));

		System.out.println(c.or(true, false) == (true || false));

		System.out.println(c.xor(true, true) == (false));

		System.out.println(c.nor(false, false) == true);
	}
}

class Comp {
	public boolean True() {
		return true;
	}

	public boolean NotTrue() {
		return !true;
	}

	public Comp This() {
		return this;
	}

	public Comp New() {
		return new Comp();
	}

	public int Add(int x, int y) {
		return x + y;
	}

	public int Sub(int x, int y) {
		return x - y;
	}

	public int Times(int x, int y) {
		return x * y;
	}

	public int[] NewArr() {
		return new int[6 + 1];
	}

	public int First(int[] arr) {
		return arr[0];
	}

	public int Len(int[] arr) {
		return arr.length;
	}

	public boolean not(boolean x) {
		return !x;
	}

	public boolean lt(int x, int y) {
		return x < y;
	}

	public boolean gt(int x, int y) {
		return y < x;
	}

	public boolean eq(int x, int y) {
		return !(x < y) && !(y < x);
	}

	public boolean neq(int x, int y) {
		return !this.eq(x, y);
	}

	public boolean leq(int x, int y) {
		return !this.gt(x, y);
	}

	public boolean geq(int x, int y) {
		return !this.lt(x, y);
	}

	public boolean and(boolean x, boolean y) {
		return x && y;
	}

	public boolean nor(boolean x, boolean y) {
		return !this.or(x, y);
	}

	public boolean or(boolean x, boolean y) {
		return !(!x && !y);
	}

	public boolean xor(boolean x, boolean y) {
		return this.or(x && !y, !x && y);
	}
}
