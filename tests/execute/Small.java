// EXT:ICG
// EXT:ISC

class Main {
	public static void main(String[] args) {
		A a;
		int b;

		a = new C().init(1);
		b = 2;

		System.out.println(b + a.a());
	}
}

class Q extends C {
	public A tis() {
		return this;
	}
}

class C extends B {
	public int a() {
		return field;
	}
}

class A {
	int field;

	public A init(int i) {
		field = i;
		return this;
	}

	public int a() {
		return 1;
	}

	public int b(int b) {
		return b;
	}

	public int c() {
		return field;
	}
}

class B extends A {
	public int a() {
		return 0;
	}
}
