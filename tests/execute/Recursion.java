class Main {
	public static void main(String[] args) {
		Recursion r;
		r = new Recursion();
		System.out.println(r.fib(9));
	}
}

class Recursion {
	public int fib(int i) {
		return this._fib(i, 0, 1);
	}

	public int _fib(int i, int a, int b) {
		int res;
		if (i == 0) {
			res = a;
		} else {
			res = this._fib(i-1, b, a+b);
		}
		return res;
	}
}
