class Main {
	public static void main(String[] args) {
		int i;
		int[] a;

		{
			i=0;
		}

		System.out.println(i);
		System.out.println(1+2);

		// a = new int[3][1];
		// This should fail. The rule is new int[ <exp> ]
	}
}
