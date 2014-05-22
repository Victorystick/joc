package mjc.ir;

import java.util.HashMap;
import java.util.Map;
import mjc.*;

public class Jump implements IRStatement {
	//Cache
	public static Map<Name, Jump> map = new HashMap<Name, Jump>();
	
	public Name target;

	Jump(Name t) {
		target = t;
	}

	public static Jump create(Label label) {
		return create(label.getName());
	}

	public static Jump create(Name name) {
		if (map.containsKey(name)) {
			return map.get(name);
		}

		Jump jump = new Jump(name);

		map.put(name, jump);

		return jump;
	}

	// public static Jump create(IRNode t) {
	// 	if (t instanceof Name) {
	// 		create((Name) t);
	// 	}

	// 	return new Jump(t);
	// }

	public String toString() {
		StringBuilder sb = new StringBuilder();
		toString(sb);
		return sb.toString();
	}

	public void toString(StringBuilder sb) {
		sb.append("{\"jump\":");
		target.toString(sb);
		sb.append("}");

		// sb.append("{\"jump ->\": \"");
		// sb.append(target.label.label);
		// sb.append("\"}");
	}

	public NodeList children() {
		return null;
		// return NodeList.create(target);
	}

	public IRStatement build(NodeList nodes) {
		return this;
		// return Jump.create(nodes.get(0));
	}
}
