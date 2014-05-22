// testing main as variable name among others.
// this should weed out compilers that handle main like a keyword.

class drain
{
	public static void main(String[] args)
	{
		int main;

		main = 23;
		System.out.println(main);
	}
}

class main
{
	public int main(main main)
	{
		return main.main(main);  // wee recursion
	}
}
