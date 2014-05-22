package mjc.minijava;

import mjc.ir.Const;

public class VarSymbol extends Symbol {
	private TypeSymbol type;
	private Symbol owner;
	private int assignments;
	private Const constValue = null;

	// For use by Methods.
	public VarSymbol(String id, TypeSymbol t) {
		super(id);
		type = t;
		owner = null;
	}

	public VarSymbol(String id, TypeSymbol t, Symbol o) {
		super(id);
		type = t;
		owner = o;
	}

	public String toString() {
		return type.getType();
	}

	public TypeSymbol getType() {
		return type;
	}

	// For use by Methods.
	public void setOwner(MethodSymbol o) {
		owner = o;
	}

	public Symbol getOwner() {
		return owner;
	}

	public void addAssignment() {
		assignments++;
	}

	public int getAssignments() {
		return assignments;
	}

	public void setConstValue(Const c) {
		constValue = c;
	}

	public Const getConstValue() {
		return constValue;
	}
}
