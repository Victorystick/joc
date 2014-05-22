package mjc.minijava;

public interface MethodLookupper {
	public MethodSymbol localMethod(String name);

	public MethodSymbol lookupMethod(String name);
}
