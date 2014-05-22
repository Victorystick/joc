class Main {
	public static void main(String[] args) {
		A getter;
		int a;
		int b;
		int c;
		int d;
		int tot;

		getter = new A();

		a = getter.val(1);
		b = 7;
		c = 3;

		tot = a + b + c;

		System.out.println(tot);

		// a = a - a;
		// a = 1 + a - 5 + a - 17;

		// tot = (a + 1) - b + c + (d + 1);
	}
}

class A {
	public int val(int a) {
		return 1;
	}
}
