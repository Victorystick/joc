package mjc.ir;

import java.util.ArrayList;
import java.util.List;
import mjc.Errors;

public class StmtSequence extends ArrayList<IRStatement> implements IRStatement {
	// Once a sequence is "completed",
	// ie. before it is returned etc.
	// the `done` method should be called.
	//
	// return sequence.done();

	public static StmtSequence create() {
		return new StmtSequence();
	}

	public static StmtSequence create(IRStatement stmt) {
		if (stmt instanceof StmtSequence) {
			return (StmtSequence)stmt;
		}

		return new StmtSequence().then(stmt);
	}

	public static StmtSequence create(IRStatement ...stmts) {
		StmtSequence seq = new StmtSequence();

		for (IRStatement stmt : stmts) {
			seq.then(stmt);
		}

		return seq.done();
	}

	public StmtSequence() {
		super();
	}

	public StmtSequence then(IRStatement stmt) {
		if (stmt instanceof StmtSequence) {
			addAll(((StmtSequence) stmt));
		} else if (stmt != null) {
			add(stmt);
		}

		return this;
	}

	public StmtSequence done() {
		if (size() == 0) {
			return null;
		}

		return this;
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();
		toString(sb);
		return sb.toString();
	}

	public void toString(StringBuilder sb) {
		sb.append("[");

		int lastI = size();
		int i = 0;
		for (IRStatement stmt : this) {
			stmt.toString(sb);

			if (++i != lastI) {
				sb.append(",");
			}
		}
		sb.append("]");
	}

  public NodeList children() {
  	Errors.fatal("StmtSequence has no children. :P");
  	return null;
  }

  public IRStatement build(NodeList kids) {
  	Errors.fatal("StmtSequence cannot be built. :P");
  	return null;
  }
}
