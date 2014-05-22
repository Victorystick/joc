package mjc.canon;

import mjc.ir.*;

public class Canon {
	public static StmtSequence fix(IRStatement stmt) {
		if (stmt == null) {
			return null;
		}

		// return linearize(stmt);
		return TraceSchedule.schedule(
			BasicBlocks.create(
				linearize(stmt)));
	}

	// Does this no nothing?
	static boolean commute(IRStatement a, IRNode b) {
		return isNop(a) || b instanceof Const || b instanceof Temp;
	}

	// Trivial check.
	static boolean isNop(IRStatement a) {
		return a instanceof SideEffect &&
			((SideEffect)a).node instanceof Const;
	}

	// Top call. Will linearize any number of IRStatements.
	public static StmtSequence linearize(IRStatement stmt) {
		return linear(cleanStatement(stmt), new StmtSequence());
	}

	// Linearizes one IRStatement.
	static StmtSequence linear(IRStatement s, StmtSequence seq) {
		if (s instanceof StmtSequence)
			return linear((StmtSequence)s, seq);
		else
			return seq.then(s);
	}

	// Linearizes a StmtSequence.
	static StmtSequence linear(StmtSequence stmts, StmtSequence seq) {
		for (IRStatement stmt : stmts) {
			linear(stmt, seq);
		}

		return seq;
	}

	// Cleans a statement. Whatever that means.
	static IRStatement cleanStatement(IRStatement s) {
		if (s instanceof StmtSequence) {
			return cleanStatement((StmtSequence)s);
		}

		if (s instanceof Move) {
			return cleanStatement((Move)s);
		}

		if (s instanceof SideEffect) {
			return cleanStatement((SideEffect)s);
		}

		return extractStatementsFromExpressions(s);
	}

	// Cleans a statement sequence.
	static IRStatement cleanStatement(StmtSequence s) {
		StmtSequence seq = new StmtSequence();

		for (IRStatement stmt : s) {
			seq.then(cleanStatement(stmt));
		}

		return seq.done();
	}

	// Cleans a Move operation.
	static IRStatement cleanStatement(Move s) {
		if (s.dest instanceof Temp) {
			Temp dest = (Temp) s.dest;

			if (s.value instanceof Call) {
				return extractStatementsFromExpressions(
					new MoveCall(dest, (Call) s.value));
			} else if (s.value instanceof Binary) {
				return extractStatementsFromExpressions(
					new Quad(dest, (Binary) s.value));
			}
		}

		if (s.dest instanceof StmtAndValue) {
			StmtAndValue sav = (StmtAndValue)s.dest;

			return cleanStatement(
				StmtSequence.create(
					sav.stmt,
					Move.create(
						sav.value,
						s.value)));
		}

		return extractStatementsFromExpressions(s);
	}

	// Cleans a SideEffect statement.
	static IRStatement cleanStatement(SideEffect s) {
		if (s.node instanceof Call) {
			return extractStatementsFromExpressions(
				new SideEffectCall((Call)s.node));
		}

		return extractStatementsFromExpressions(s);
	}

	static IRStatement extractStatementsFromExpressions(IRStatement s) {
		StmtAndNodes san = extractStatements(s.children(), 0);
		return StmtSequence.create(san.stmt, s.build(san.nodes));
	}

	static StmtAndValue cleanExpression(IRNode n) {
		if (n instanceof StmtAndValue) {
			return cleanExpression((StmtAndValue) n);
		}

		return extractStatementsFromExpression(n);
	}

	static StmtAndValue cleanExpression(StmtAndValue sav) {
		IRStatement stmt = cleanStatement(sav.stmt);
		StmtAndValue sav2 = cleanExpression(sav.value);

		return new StmtAndValue(
			StmtSequence.create(stmt, sav2.stmt),
			sav2.value);
	}

	static StmtAndValue extractStatementsFromExpression(IRNode s) {
		StmtAndNodes san = extractStatements(s.children(), 0);
		return new StmtAndValue(san.stmt, s.build(san.nodes));
	}

	static StmtAndNodes extractStatements(NodeList nodes, int index) {
		if (nodes == null || nodes.size() - index == 0) {
			return new StmtAndNodes(null, nodes);
		}

		IRNode node = nodes.get(index);

		if (node instanceof Call) {
			Temp t = Temp.create();

			IRNode e = StmtAndValue.create(
				Move.create(t, node),
				t);

			nodes.set(index, e);

			return extractStatements(nodes, index);
		} else {
			StmtAndValue fixedNode = cleanExpression(node);
			StmtAndNodes fixedRest = extractStatements(nodes, index + 1);

			StmtSequence seq = StmtSequence.create(fixedNode.stmt);

			if (commute(fixedRest.stmt, fixedNode.value)) {
				nodes.set(index, fixedNode.value);

				fixedRest.stmt = seq.then(fixedRest.stmt);
			} else {
				Temp t = Temp.create();

				nodes.set(index, t);

				fixedRest.stmt = seq
					.then(Move.create(t, fixedNode.value))
					.then(fixedRest.stmt);
			}

			return fixedRest;
		}
	}
}
