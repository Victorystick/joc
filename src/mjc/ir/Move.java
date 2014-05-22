package mjc.ir;

public class Move implements IRStatement {
	public IRNode dest;
	public IRNode value;

	public static IRStatement create(IRNode l, IRNode v) {
		StmtAndValue sv;
		Move otherMove;

		// Short-cut Moves in StmtAndValue nodes.
		if (l instanceof Temp && v instanceof StmtAndValue) {
			sv = (StmtAndValue)v;
			if (sv.getStmt() instanceof Move) {
				otherMove = (Move)sv.getStmt();

				otherMove.dest = l;

				return otherMove;
			}
		}

		// Move self to self.
		if (l == v) {
			return null;
		}

		return new Move(l, v);
	}

	Move(IRNode l, IRNode v) {
		dest = l;
		value = v;
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();
		toString(sb);
		return sb.toString();
	}

	public void toString(StringBuilder sb) {
		sb.append("{\"move\":");
		dest.toString(sb);
		sb.append(",\"value\":");
		value.toString(sb);
		sb.append("}");
	}

	public NodeList children() {
		if (dest instanceof Memory) {
			Memory target = (Memory)dest;

			if (target.value instanceof Binary) {
				Binary bin = (Binary) target.value;

				return NodeList.create(bin.left, bin.right, value);
			}

			return NodeList.create(target.value, value);
		}

		return NodeList.create(value);
	}

	public IRStatement build(NodeList nodes) {
		if (dest instanceof Memory) {
			Memory target = (Memory) dest;

			if (target.value instanceof Binary) {
				Binary bin = (Binary) target.value;
				bin.left = nodes.get(0);
				bin.right = nodes.get(1);
				value = nodes.get(2);
			} else {
				target.value = nodes.get(0);
				value = nodes.get(1);
			}

			return this;
		}

		return Move.create(dest, nodes.get(0));
	}
}
