class Main {
	public static void main(String[] args) {
		int _;
		Type t;
		A main;

		t = new Type();

		System.out.println(t.setType(1));

		System.out.println(t.getType());

		main = new B().init();

		System.out.println(main.getType());

		main = main.print();

		// main is actual instance of A
		System.out.println(main.getType() == 1);

		main = new A().init();

		System.out.println(main.getType());

		main = main.print();

		// main is actual instance of A
		System.out.println(main.getType() == 1);

	}
}

class Type {
	int type;

	public int setType(int i) {
		type = i;
		return type;
	}

	public int getType() {
		return type;
	}
}

class A extends Type {
	int a;

	public A init() {
		type = 1;
		a = 11;
		return this;
	}

	public int get() {
		return a;
	}

	public A print() {
		System.out.println(a);
		return this;
	}
}

class B extends A {
	public A init() {
		type = 2;
		a = 13;
		return this;
	}
}
