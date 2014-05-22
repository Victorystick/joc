class Main {
	public static void main(String[] args) {
		A a;

		a = new A();

		System.out.println(a.call() != a);
	}
}

class A {
	public A call() {
		System.out.println(1);
		return this;
	}
}
