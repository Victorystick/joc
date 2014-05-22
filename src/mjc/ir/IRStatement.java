package mjc.ir;

public interface IRStatement {
	public void toString(StringBuilder sb);

	public NodeList children();
	public IRStatement build(NodeList children);
}
