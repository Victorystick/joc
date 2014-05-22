package mjc.minijava;

import java.util.List;
import java.util.ArrayList;

import underscore.*;

public class MethodSymbol extends Symbol implements VarLookupper, VarIndexer {
	private static final Mapper<VarSymbol, TypeSymbol> varMapper = new Mapper<VarSymbol, TypeSymbol>() {
		public TypeSymbol map(VarSymbol var) {
			return var.getType();
		}
	};

	private static final Mapper<TypeSymbol, String> typeMapper = new Mapper<TypeSymbol, String>() {
		public String map(TypeSymbol ts) {
			return ts.getType();
		}
	};

	TypeSymbol returnType;
	List<TypeSymbol> paramTypes;
	ClassSymbol owner;
	SymbolTable<VarSymbol> vars;
	List<VarSymbol> indexedVars;
	int offset;

	public MethodSymbol(String id, TypeSymbol returnType,
			List<VarSymbol> params, ClassSymbol owner,
			SymbolTable parent) {
		super(id);
		this.returnType = returnType;
		this.paramTypes = _.map(params, varMapper);
		this.owner = owner;
		this.vars = new SymbolTable<VarSymbol>(owner.fields);
		this.indexedVars = new ArrayList<VarSymbol>();
	}

	public String getCompleteIdentifier() {
		return owner.getIdentifier() + '$' + getIdentifier();
	}

	public VarSymbol localVar(String name) {
		return vars.local(name);
	}

	public VarSymbol lookupVar(String name) {
		return vars.lookup(name);
	}

	public void add(VarSymbol sym) {
		vars.add(sym);
		indexedVars.add(sym);
	}

	public String toString() {
		return "(" + _.join(_.map(paramTypes, typeMapper), ", ") + ") -> " + returnType.getType();
	}

	public ClassSymbol getOwner() {
		return owner;
	}

	public int indexOf(VarSymbol var) {
		return indexedVars.indexOf(var) + 1;
	}

	public int numParams() {
		return paramTypes.size();
	}

	public boolean typeSignatureMatches(MethodSymbol other) {
		if (paramTypes.size() != other.paramTypes.size()) {
			return false;
		}

		if (returnType != other.returnType) {
			return false;
		}

		for (int i = 0; i < paramTypes.size(); i++) {
			if (paramTypes.get(i) != other.paramTypes.get(i)) {
				return false;
			}
		}

		return true;
	}

	public int numLocals() {
		return 1 +                // this
			indexedVars.size() +           // local vars
			this.paramTypes.size(); // arguments
	}

	public void setOffset(int o) {
		offset = o;
	}

	public int getOffset() {
		return offset;
	}
}
