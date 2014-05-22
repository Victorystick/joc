// EXT:CEQ
// EXT:ICG
// EXT:ISC

class Main {
	public static void main(String[] args) {
		M m1;
		M m2;

		m1 = new M();
		m2 = new M();

		System.out.println(m1 == m2 == false == false == false);
	}
}

class M extends Main {}
