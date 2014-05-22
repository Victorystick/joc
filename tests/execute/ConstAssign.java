class Const {
	public static void main(String[] args) {
		int a;
		int b;
		int c;

		a = 1;
		b = 24;

		c = a + b;

		System.out.println(c);

		System.out.println(new Field().call());
	}
}

class Field {
	int f;

	public int call() {
		int a;

		a = 1;
		f = 24;

		return a + f;
	}
}