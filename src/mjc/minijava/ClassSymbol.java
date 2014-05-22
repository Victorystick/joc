package mjc.minijava;

import java.util.Collection;
import java.util.Set;
import java.util.HashSet;
import mjc.translate.SizeOf;

/**
 * The Symbol for the Class construct.
 */
public class ClassSymbol extends TypeSymbol implements VarLookupper, MethodLookupper, SizeOf {
	final SymbolTable<VarSymbol> fields;
	final SymbolTable<MethodSymbol> methods;
	ClassSymbol supr = null;
	Set<ClassSymbol> subclasses = new HashSet<ClassSymbol>();

	public ClassSymbol(String i) {
		super(i);
		fields = new SymbolTable<VarSymbol>();
		methods = new SymbolTable<MethodSymbol>();
	}

	public boolean setSuper(ClassSymbol supr) {
		ClassSymbol cycleChecker = supr;

		while (true) {
			if (cycleChecker == this) {
				return false;
			}
			if (cycleChecker.supr == null) {
				break;
			}
			cycleChecker = cycleChecker.supr;
		}

		this.supr = supr;

		supr.addSubclass(this);

		fields.setParent(supr.fields);
		methods.setParent(supr.methods);

		return true;
	}

	public ClassSymbol getSuper() {
		return supr;
	}

	public void addSubclass(ClassSymbol cls) {
		subclasses.add(cls);
	}

	public boolean isSubclassed() {
		return !subclasses.isEmpty();
	}

	public void addField(VarSymbol var) {
		fields.add(var);
	}

	public void addMethod(MethodSymbol sym) {
		methods.add(sym);
	}

	public VarSymbol localVar(String name) {
		return fields.local(name);
	}

	public VarSymbol lookupVar(String name) {
		return fields.lookup(name);
	}

	public MethodSymbol localMethod(String name) {
		return methods.local(name);
	}

	public MethodSymbol lookupMethod(String name) {
		return methods.lookup(name);
	}

	public String toString() {
		return fields.toString();
	}

	public String getType() {
		return getIdentifier();
	}

	public Collection<VarSymbol> getFields() {
		return fields.values();
	}

	public Collection<MethodSymbol> getMethods() {
		return methods.values();
	}

	private int sizeOfExcludingClassPointer() {
		int size = 0;

		if (supr != null) {
			size += supr.sizeOfExcludingClassPointer();
		}

		for (VarSymbol sym : fields.values()) {
			if (sym.getType().getClass() == ClassSymbol.class) {
				size += 4; // Pointer
			} else {
				size += sym.getType().sizeOf();
			}
		}

		return size;
	}

	public int sizeOf() {
		return sizeOfExcludingClassPointer() + 4;
	}

	public boolean isCompatibleWith(ClassSymbol cs) {
		if (this == cs) {
			return true;
		}

		if (supr != null) {
			return supr.isCompatibleWith(cs);
		}

		return false;
	}

	public boolean isCompatibleWith(TypeSymbol ts) {
		if (ts instanceof ClassSymbol) {
			return isCompatibleWith((ClassSymbol)ts);
		}

		return false;
	}
}

