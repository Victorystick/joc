package mjc.minijava;

/**
 * The Symbol for a Type.
 */
abstract public class TypeSymbol extends Symbol {
	public TypeSymbol(String i) {
		super(i);
	}

	public abstract String getType();

	// Returns size in bytes.
	public abstract int sizeOf();

	public abstract boolean isCompatibleWith(TypeSymbol ts);
}
