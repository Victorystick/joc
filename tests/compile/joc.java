class joc {
	public static void main(String[] args) {
		Lexer l;
		int[] code;
		int i;

		l = new Lexer();
		code = new int[1337];

		i = 0;
		while (i < code.length) {
			code[i] = i;
			i = i + 1;
		}

		System.out.println(code[1]);
		System.out.println(l.get(2));
		System.out.println(l.first(code));
		System.out.println(l.sum(1,2,3,4,5));

		// 1,2,3,4: works
		// 2,3,4  : works
		// 1,3,4  : segs
		// 3,4    : works
		// 1,2,4  : works
		// 1,4    : works
		// 2,4    : works
		// 4      : works
		// 1,2,3  : segs
		// 2,3    : works
		// 1,3    : works
		// 3      : works
	}
}

class Lexer {
	int sizeTest;

	public int get(int i) {
		return sizeTest + i;
	}

	public int first(int[] code) {
		return code[1];
	}

	public int sum(int a, int b, int c, int d, int e) {
		return a + b + c + d + e;
	}
}
