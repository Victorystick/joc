package mjc.ir;

import mjc.Errors;

public enum BinOp {
	PLUS("+", 50),
	MINUS("-", 50),
	MULT("*", 60),
	AND("&&", 30),
	OR("||", 20),
	LT("<") {
		public BinOp invert() {
			return GEQ;
		}
		public BinOp flip() {
			return GT;
		}
	},
	LEQ("<=") {
		public BinOp invert() {
			return GT;
		}
		public BinOp flip() {
			return GEQ;
		}
	},
	GT(">") {
		public BinOp invert() {
			return LEQ;
		}
		public BinOp flip() {
			return LT;
		}
	},
	GEQ(">=") {
		public BinOp invert() {
			return LT;
		}
		public BinOp flip() {
			return LEQ;
		}
	},
	EQ("==") {
		public BinOp invert() {
			return NEQ;
		}
		public BinOp flip() {
			return EQ;
		}
	},
	NEQ("!=") {
		public BinOp invert() {
			return EQ;
		}
		public BinOp flip() {
			return NEQ;
		}
	};

	private String str;
	private int bindingPower;
	private boolean cmp;

	BinOp(String s, int bp) {
		str = s;
		bindingPower = bp;
		cmp = bp == 40;
	}

	BinOp(String s) {
		this(s, 40);
	}

	public String toString() {
		return str;
	}

	public boolean isCmp() {
		return cmp;
	}

	public int getBindingPower() {
		return bindingPower;
	}

	public BinOp invert() {
		Errors.fatal(String.format("BinOp '%s' has no inverse!", this));
		return null;
	}

	public BinOp flip() {
		Errors.fatal(String.format("BinOp '%s' can't be flipped!", this));
		return null;
	}



	public static BinOp get(String s) {
		if (s.equals("+")) {
			return PLUS;
		}

		if (s.equals("-")) {
			return MINUS;
		}

		if (s.equals("*")) {
			return MULT;
		}

		if (s.equals("&&")) {
			return AND;
		}

		if (s.equals("||")) {
			return OR;
		}

		if (s.equals("<")) {
			return LT;
		}

		if (s.equals("<=")) {
			return LEQ;
		}

		if (s.equals(">")) {
			return GT;
		}

		if (s.equals(">=")) {
			return GEQ;
		}

		if (s.equals("==")) {
			return EQ;
		}

		if (s.equals("!=")) {
			return NEQ;
		}

		return null;
	}
}
