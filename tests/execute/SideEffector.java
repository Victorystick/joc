class Main {
	public static void main(String[] args) {
		SideEffector c;
		c = new SideEffector();
		if (c.op() && c.op() && c.op()) {
			System.out.println(1);
		} else {
			System.out.println(0);
		}
	}
}

class SideEffector {
	public boolean op() {
		System.out.println(1337);
		return false;
	}
}
