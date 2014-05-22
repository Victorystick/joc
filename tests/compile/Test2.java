/**
 * An example test class.
 */
class Main {
	public static void main(String[] args) {
		Q q;
		int[] arr;
		int i;

		arr = new int[20];
		q = new Q();

		if (true && 1 < q.getKillings()) {
			arr[1 + new Exp().getLength()] = 15;
		} else {
			i = arr[1 + new Exp().getLength()] * 15;
		}

		if (true && 1 < q.getKillings())
			arr[1 + new Exp().getLength()] = 15;
		else
			i = arr[1 + new Exp().getLength()] * 15;
	}
}

class Q {
	public int getKillings() {
		return 1;
	}
}

class Exp {
	public int getLength() {
		return 0;
	}
}
