class Doge {
	public static void main(String[] args) {
		Such wow;

		wow = new Such();

		System.out.println(wow.call(1, 2, 3, 4, 14));
	}
}

class Such {
	public int call(int a, int b, int c, int durr, int foo) {
		return this.internal(a, durr + foo);
	}

	public int internal(int a, int b) {
		return a + b;
	}
}
