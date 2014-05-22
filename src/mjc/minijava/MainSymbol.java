package mjc.minijava;

import java.util.ArrayList;
import java.util.List;
import mjc.Errors;

class MainSymbol extends TypeSymbol implements VarLookupper, VarIndexer {
	SymbolTable<VarSymbol> vars;
	List<VarSymbol> indexedVars;

	public MainSymbol(String id) {
		super(id);
		vars = new SymbolTable<VarSymbol>();
		indexedVars = new ArrayList<VarSymbol>();
	}

	public VarSymbol lookupVar(String name) {
		VarSymbol res = vars.lookup(name);
		if ( res != null && res.getType().equals(BuiltIn.STRING_ARR) ) {
			Errors.fatal("Attempted to use invalid type String[].");
		}
		return res;
	}

	public VarSymbol localVar(String name) {
		return vars.local(name);
	}

	public void add(VarSymbol sym) {
		vars.add(sym);
		indexedVars.add(sym);
	}

	public String toString() {
		return "() -> void";
	}

	public String getType() {
		return getIdentifier();
	}

	public int indexOf(VarSymbol var) {
		return indexedVars.indexOf(var) + 2;
	}

	public int numLocals() {
		return indexedVars.size() + 2;
	}

	// ???
	public int sizeOf() {
		return 0; // TODO: determine size!?
	}

	public boolean isCompatibleWith(TypeSymbol ts) {
		return this == ts;
	}
}
