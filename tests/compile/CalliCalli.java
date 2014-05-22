class Main {
	public static void main(String[] args) {
		A a; 
		a = new A(); //1 live.
		System.out.println(a.complex(10, 9, 8, 7, 6, 5, 4, 3, 2, a.call(1))); //defs: 10, 9, 8, 7, 6, 5, 4, 3, 2, 1, a.complex, a.call
		// prints: 2052
	}
}

class A {
	public int call(int i) {
		return i+1;
	}
	public int complex(int a, int b, int c, int d, int e, int f, int g, int h, int i, int j) {
		return a+b+c+d+e+f+g+h+i+999*j;
	}
}

class B {
	public int call(int i) {
		return i+2;
	}
}