package mjc.ir;

import mjc.Errors;

public class StmtAndValue extends IRNode {
	public final IRStatement stmt;
	public final IRNode value;

	public static IRNode create(IRStatement s, IRNode v) {
		if (s == null) {
			return v;
		}

		return new StmtAndValue(s, v);
	}

	public StmtAndValue(IRStatement s, IRNode v) {
		stmt = s;
		value = v;
	}

	public IRStatement getStmt() {
		return stmt;
	}

	public void toString(StringBuilder sb) {
		sb.append("{\"stmt\":");
		stmt.toString(sb);
		sb.append(",\"value\":");
		value.toString(sb);
		sb.append("}");
	}

  public NodeList children() {
  	Errors.fatal("StmtAndValue has no children. :P");
  	return null;
  }

  public IRNode build(NodeList kids) {
  	Errors.fatal("StmtAndValue cannot be built. :P");
  	return null;
  }
}
