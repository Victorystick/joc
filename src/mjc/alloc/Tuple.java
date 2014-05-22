package mjc.alloc;

class Tuple<F, S> {
	F first; S second;

	Tuple(F f, S s) {
		first = f;
		second = s;
	}
}

class Triple<F, S, T> {
	F first; S second; T third;

	Triple(F f, S s, T t) {
		first = f;
		second = s;
		third = t;
	}
}
