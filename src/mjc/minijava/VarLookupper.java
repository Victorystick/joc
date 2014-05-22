package mjc.minijava;

public interface VarLookupper {
	public VarSymbol localVar(String name);

	public VarSymbol lookupVar(String name);
}
