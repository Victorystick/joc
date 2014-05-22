// EXT:CEQ
// EXT:CNE
// EXT:CLE
// EXT:CGT
// EXT:CGE
// EXT:BDJ

class Main {
	public static void main(String[] args) {
		Arith a;

		a = new Arith().init();
	}
}

class Arith {
	Comp c;

	public Arith init() {
		c = new Comp().test();

		// this.test();

		return this;
	}

	public int div(int a, int b) {
		int res;

		res = 0;

		while (c.gt(a, b)) {
			a = a - b;
			res = res + 1;
		}

		return res;
	}

	public int mod(int a, int b) {
		while (c.gt(a, b)) {
			a = a - b;
		}

		return a;
	}

	public int gcd(int u, int v) {
		int res;

		if (c.or(c.eq(u, v), c.eq(u, 0))) {
			res = v;
		} else if (c.eq(v, 0)) {
			res = u;
		} else {
			res = 0;
		}

		return res;
	}

	public int abs(int x) {
		int res;

		if (x < 0) {
			res = 0 - x;
		} else {
			res = x;
		}

		return res;
	}
}

class Comp {
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
		// return x || y;
	}

	public boolean xor(boolean x, boolean y) {
		return this.or(x && !y, !x && y);
	}

	public Comp test() {
		Assert _;

		_ = new Assert().init();

		// ||
		_ = _.ass_ert(1 == 1 || false && true, this.or(true, false));

		_ = _.ass_ert(true || true, this.or(true, true));

		_ = _.ass_ert(false || false, this.or(false, false));

		_ = _.ass_ert(false || false, this.or(false, false));

		// < <= > >=
		_ = _.ass_ert(1 < 2, this.lt(1, 2));

		_ = _.ass_ert(2 > 1, this.gt(2, 1));

		_ = _.ass_ert(2 <= 2, this.leq(1, 2));

		_ = _.ass_ert(2 >= 2, this.geq(2, 1));

		// &&
		_ = _.ass_ert(this.and(true, true), true && true);

		// == !=
		_ = _.ass_ert(1 == 2, this.eq(1, 2));

		_ = _.ass_ert(1 != 2, this.neq(1, 2));

		return this;
	}
}

class Assert {
	int i;

	public Assert init() {
		i = 0;

		return this;
	}

	public Assert ass_ert(boolean a, boolean b) {
		if (a != b) {
			// Print error id;
			System.out.println(i);
		} else {}
		i = i + 1;

		return this;
	}
}
