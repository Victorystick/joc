package mjc.minijava;

import java.util.Collection;
import java.util.Set;
import java.util.Map;
import java.util.Map.Entry;
import java.util.HashMap;

/**
 * A symbol table is a datastructure used by the compiler for
 * semantical analysis of the program.
 *
 * More specifically, it is used to do type checking of the program.
 * All classes and variables is entered into the table, along with their type.
 * When a identifier is found in the code, it is looked up using the table, to see its type.
 *
 * @author johfog
 * @author oskarseg
 *
 */
public class SymbolTable<T extends Symbol> {
	private Map<String, T> map;
	private SymbolTable<T> parent = null;

	public SymbolTable() {
		map = new HashMap<String, T>();
	}

	public SymbolTable(SymbolTable<T> p) {
		this();
		parent = p;
	}

	public void setParent(SymbolTable<T> p) {
		parent = p;
	}

	public T local(String name) {
		return map.get(name);
	}

	public T lookup(String name) {
		if (map.containsKey(name)) {
			return map.get(name);
		}

		if (parent != null) {
			return parent.lookup(name);
		}

		return null;
	}

	/**
	 * Add a symbol to the table
	 * This allows it to be found by a later lookup.
	 * @param s
	 */
	public void add(T s) {
		map.put(s.getIdentifier(), s);
	}

	public Set<String> keys() {
		return map.keySet();
	}

	public Collection<T> values() {
		return map.values();
	}

	public String toString() {
		StringBuilder sb = new StringBuilder("{");

		boolean first = true;

		for(Entry<String, T> entry : map.entrySet()) {
	    if (first) {
	    	first = false;
	    } else {
	    	sb.append(", ");
	    }

	    String key = entry.getKey();
	    T value = entry.getValue();

	    sb.append(key + " : " + value.toString());
		}

		sb.append("}");
		return sb.toString();
	}
}
