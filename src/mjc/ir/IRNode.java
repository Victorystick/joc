package mjc.ir;

abstract public class IRNode {
	int row;
	int col;

	abstract public void toString(StringBuilder sb);

	public NodeList children() {
		return null;
	}

	public IRNode build(NodeList children) {
		return this;
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();
		toString(sb);
		return sb.toString();
	}
}
