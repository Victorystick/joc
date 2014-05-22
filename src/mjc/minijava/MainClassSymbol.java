package mjc.minijava;

import java.util.Collection;
import java.util.Collections;

// Hack to allow Main to be "instantiated".
class MainClassSymbol extends ClassSymbol {
	private static MainClassSymbol instance = null;

	public static MainClassSymbol getInstance(MainSymbol ms) {
		if (instance == null) {
			instance = new MainClassSymbol(ms);
		}

		return instance;
	}

	MainClassSymbol(MainSymbol ms) {
		super(ms.getIdentifier());
	}


	public boolean setSuper(ClassSymbol supr) {
		return false;
	}

	public ClassSymbol getSuper() {
		return null;
	}

	public void addField(VarSymbol var) {}

	public void addMethod(MethodSymbol sym) {}

	public VarSymbol localVar(String name) {
		return null;
	}

	public VarSymbol lookupVar(String name) {
		return null;
	}

	public MethodSymbol localMethod(String name) {
		return null;
	}

	public MethodSymbol lookupMethod(String name) {
		return null;
	}

	public String toString() {
		return getIdentifier();
	}

	public String getType() {
		return "java/lang/Object";
	}

	public Collection<VarSymbol> getFields() {
		return Collections.<VarSymbol>emptyList();
	}

	public Collection<MethodSymbol> getMethods() {
		return Collections.<MethodSymbol>emptyList();
	}

	private int sizeOfExcludingClassPointer() {
		return 0;
	}

	public int sizeOf() {
		return 0;
	}

	public boolean isCompatibleWith(ClassSymbol cs) {
		return this == cs;
	}

	public boolean isCompatibleWith(TypeSymbol ts) {
		if (ts instanceof ClassSymbol) {
			return isCompatibleWith((ClassSymbol)ts);
		}

		return false;
	}
}
