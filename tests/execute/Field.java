class Main {
	public static void main(String[] args) {
		Field f;

		f = new Field().init();

		System.out.println(f.getInt());
	}
}

class Field {
	int i;

	public Field init() {
		i = 1;
		return this;
	}

	public int getInt() {
		return i;
	}
}