package mjc.ir;

public class Memory extends IRNode {
	public IRNode value;

	public static IRNode create(IRNode v) {
		return new Memory(v);
	}

	Memory(IRNode v) {
		value = v;
	}

	public void toString(StringBuilder sb) {
		sb.append("{\"mem\":");
		value.toString(sb);
		sb.append("}");
	}

	public IRNode build(NodeList nodes) {
		if (value instanceof Binary) {
			value = Binary.create(
				((Binary) value).op,
				nodes.get(0),
				nodes.get(1));
			return this;
		}
		return new Memory(nodes.get(0));
	}

	public NodeList children(){
		if (value instanceof Binary) {
			Binary bin = (Binary) value;
			return NodeList.create(bin.left, bin.right);
		}

		return NodeList.create(value);
	}
}
