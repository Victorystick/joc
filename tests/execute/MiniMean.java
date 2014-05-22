class MiniMean {
	public static void main(String[] args) {
		int a;
		int b;
		int c;

		b = 7;
		// b is final

		a = 1;

		a = 1 - b * 7;
		// folds to:
		// a = -48

		a = a * 5 * 4;
		// folds to:
		// a = a * 20

		a = a - 1 + 1 - 1 + 1 - 1 + 1 - 1 + 1 - 1;
		// folds to:
		// a = a - 1;

		c = a;

		System.out.println(c);
	}
}
