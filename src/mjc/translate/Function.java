package mjc.translate;

import mjc.frame.*;
import mjc.ir.IRStatement;
import mjc.ir.StmtSequence;
import mjc.canon.Canon;

public class Function {
	public final Frame frame;
	public final StmtSequence body;

	public Function (Frame f, IRStatement b) {
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
}
