package mjc.minijava;

/**
 * The Symbol for a Type.
 */
class PrimitiveSymbol extends TypeSymbol {
	int size;

	public PrimitiveSymbol(String i, int s) {
		super(i);
		size = s;
	}

	public String getType() {
		return getIdentifier();
	}

	public String toString() {
		return getIdentifier();
	}

	public int sizeOf() {
		return size;
	}

	public boolean isCompatibleWith(TypeSymbol ts) {
		return this == ts;
	}
}
