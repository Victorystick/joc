package mjc.ir;

import java.util.HashMap;
import java.util.Map;
import mjc.Errors;

public class CJump implements IRStatement {
	public BinOp cmp;
	public IRNode left;
	public IRNode right;
	public Label tru;
	public Label fals;

	/**
	 * Only create CJumps with non-constant conditions.
	 */
	public static IRStatement create(IRNode val, Label t, Label f) {
		if (val instanceof Const) {
			if (val == Const.ZERO) {
				return Jump.create(f);
			} else {
				return Jump.create(t);
			}
		}

		if (val instanceof Binary) {
			Binary bin = (Binary) val;

			switch (bin.op) {
				case EQ:
				case NEQ:
				case LT:
				case LEQ:
				case GT:
				case GEQ:
					return create(bin.op, bin.left, bin.right, t, f);
				default: // Fall through
			}
		}
		// Compare with EQ and switch tru and fals
		return create(BinOp.EQ, val, Const.create(0), f, t);
	}

	private static IRStatement create(BinOp c, IRNode a, IRNode b, Label t, Label f) {
		if (!c.isCmp()) {
			Errors.fatal(
				String.format(
					"CJump created with non-conditional BinOp '%s'.",
					c));
		}

		if (a instanceof Const) {
			return new CJump(c.flip(), b, a, t, f);
		}

		return new CJump(c, a, b, t, f);
	}

	/**
	 * Actual constructor is private. Use CJump.create
	 */
	private CJump(BinOp c, IRNode l, IRNode r, Label t, Label f) {
		cmp = c;
		left = l;
		right = r;
		tru = t;
		fals = f;
	}

	/**
	 * Inverts the CJumps condition:
	 *   true  -> jumps to what was previously the false branch
	 *   false -> to the old true branch
	 */
	public IRStatement invert() {
		return create(cmp.invert(), left, right, fals, tru);
	}

	/**
	 * CJumps bind tightly to any right-side constants.
	 */
	public NodeList children() {
		if (right instanceof Const) {
			return NodeList.create(left);
		}

		return NodeList.create(left, right);
	}

	/**
	 * CJumps bind tightly to any right-side constants.
	 */
	public IRStatement build(NodeList nodes) {
		if (right instanceof Const) {
			left = nodes.get(0);
			return this;
		}

		return CJump.create(cmp, nodes.get(0), nodes.get(1), tru, fals);
	}

	/**
	 * Debugging code
	 */
	public String toString() {
		StringBuilder sb = new StringBuilder();
		toString(sb);
		return sb.toString();
	}

	public void toString(StringBuilder sb) {
		sb.append("{\"left\":");
		left.toString(sb);
		sb.append(",\"cmp\":\"");
		sb.append(cmp);
		sb.append("\",\"right\":");
		right.toString(sb);

		sb.append(",\"true\":");
		tru.toString(sb);
		sb.append(",\"false\":");
		fals.toString(sb);
		sb.append("}");
	}
}
