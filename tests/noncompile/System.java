/**
 * This is a really nasty testcase.
 * I am not really sure this is illegal, since System is not a reserved word.
 * But the grammar states that illegal java programs are illegal minijava
 * programs and javac fails to compile this.
 *
 * Tege, I leave final judgement to you.
 */
class Main {
	public static void main(String[] args) {
		boolean res;
		System s;
		s = new System(); // Wat?
		res = s.out(1337); // ok.....
		System.out.println(res); // Mwhahahahahahahaha!
	}
}

/**
 * Sneaky System class.
 * Shadows the java.lang.System class in javac, but probably not
 * in student compilers.
 */
class System {
	public boolean out(int data) {
		System.out.println(data);
		return true;
	}
}
