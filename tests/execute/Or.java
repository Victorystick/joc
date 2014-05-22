// EXT:BDJ

class Main {
	public static void main(String[] args) {
		Or or;
		boolean b;

		b = true;

		or = new Or();

		System.out.println(or.a() || or.b());
		System.out.println(or.a() || b || or.b());
		System.out.println(b || or.a() || or.b());
		System.out.println(or.b() || b || or.a());
		System.out.println(or.b() || or.a() || b);
	}
}

class Or {
	public boolean a() {
		System.out.println(1);
		return true;
	}

	public boolean b() {
		System.out.println(2);
		return false;
	}
}
