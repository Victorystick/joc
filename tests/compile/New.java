class Main {
	public static void main(String[] args) {
		A a;
		int x;
		int y;
		int z;

		a = new A();

		z = 4;
		x = 7;

		y = a.a();

		z = x + z;

		y = a.a();

	}
}

class A {
	int a;

	public int a() {
		a = a + 1;
		System.out.println(a);
		return a;
	}
}
