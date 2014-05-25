package mjc.translate;

import mjc.frame.*;
import mjc.ir.IRStatement;
import mjc.ir.StmtSequence;
import mjc.canon.Canon;

public class Function {
	private final String name;
	private final boolean exported;
	public final Frame frame;
	public final StmtSequence body;

	public Function(String n, boolean ex, Frame f, IRStatement b) {
		name = n;
		exported = ex;
		frame = f;
		body = Canon.fix(f.viewShift(b));
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();

		sb.append("{\"type\":\"Function\"");
		sb.append(",\"frame\":");
		frame.toString(sb);
		sb.append(",\"body\":");
		body.toString(sb);
		sb.append("}");

		return sb.toString();
	}

	public String getName() {
		return name;
	}

	public boolean isExported() {
		return exported;
	}
}
