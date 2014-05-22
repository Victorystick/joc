class Main {
	public static void main(String[] args) {
		Calc cal;
		int a; int b; int c; int d;
		int e; int f; int g; int h;
		a=1;
		b=2;
		c=3;
		d=4;
		e=5;
		f=7;
		g=8;
		h=9;
		//i=0;
		System.out.println(a+b+c+d+e+f+g+h);
		// 39
		cal = new Calc();
		System.out.println(cal.add(a, b, c, d, e, f, g, h));
		// 39
		System.out.println(cal.wat(a, b, c, d, e, f, g, h));
		// 61

	}
}

class Calc {
	public int add(int a, int b, int c, int d, int e, int f, int g, int h) {
		return a + b + c + d + e + f + g + h;
	}

	public int wat(int a, int b, int c, int d, int e, int f, int g, int h) {
		return a*b + c - d - e - f + g*h;
	}
}
