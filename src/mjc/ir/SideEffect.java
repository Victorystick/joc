package mjc.ir;

public class SideEffect implements IRStatement {
	public IRNode node;

	public static IRStatement create(IRNode n) {
		if (n instanceof Const) {
			return null;
		}

		return new SideEffect(n);
	}

	SideEffect(IRNode n) {
		node = n;
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();
		toString(sb);
		return sb.toString();
	}

	public void toString(StringBuilder sb) {
		sb.append("{\"sideEffect\":");
		node.toString(sb);
		sb.append("}");
	}

	public NodeList children() {
		return NodeList.create(node);
	}

	public IRStatement build(NodeList nodes) {
		return create(nodes.get(0));
	}
}
