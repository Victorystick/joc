package mjc.minijava;

public class BuiltIn {
	public static final PrimitiveSymbol INT = new PrimitiveSymbol("int", 4);
	public static final PrimitiveSymbol INT_ARR = new PrimitiveSymbol("int[]", 4);
	public static final PrimitiveSymbol BOOLEAN = new PrimitiveSymbol("boolean", 4);
	public static final PrimitiveSymbol STRING_ARR = new PrimitiveSymbol("String[]", 4);

	public static Symbol getType(String name) {
		if (name.equals("int")) {
			return INT;
		}

		if (name.equals("int[]")) {
			return INT_ARR;
		}

		if (name.equals("boolean")) {
			return BOOLEAN;
		}

		if (name.equals("String[]")) {
			return STRING_ARR;
		}

		return null;
	}
}
