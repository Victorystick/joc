package mjc.ir;

import mjc.translate.If;

public class Binary extends IRNode {
	public BinOp op;
	public IRNode left, right;

	Binary(BinOp o, IRNode l, IRNode r) {
		op = o;
		left = l;
		right = r;
	}

	/**
	 * Shorthand for create(o, a, Const.create(i))
	 */
	public static IRNode create(BinOp o, IRNode a, int i) {
		return create(o, a, Const.create(i));
	}

	/**
	 * Shorthand for create(o, Const.create(i), b)
	 */
	public static IRNode create(BinOp o, int i, IRNode b) {
		return create(o, Const.create(i), b);
	}

	/**
	 * Creates a Binary expression. Simple constant folding implemented.
	 */
	public static IRNode create(BinOp o, IRNode a, IRNode b) {
		if (a instanceof Const) {
			if (b instanceof Const) {
				int va = ((Const) a).getValue();
				int vb = ((Const) b).getValue();

				switch (o) {
					case PLUS:
						return Const.create(va + vb);
					case MINUS:
						return Const.create(va - vb);
					case MULT:
						return Const.create(va * vb);
					case AND:
						return Const.create(va != 0 && vb != 0 ? 1 : 0);
					case OR:
						return Const.create(va != 0 || vb != 0 ? 1 : 0);
					case LT:
						return Const.create(va < vb ? 1 : 0);
					case LEQ:
						return Const.create(va <= vb ? 1 : 0);
					case GT:
						return Const.create(va > vb ? 1 : 0);
					case GEQ:
						return Const.create(va >= vb ? 1 : 0);
					case EQ:
						return Const.create(va == vb ? 1 : 0);
					case NEQ:
						return Const.create(va != vb ? 1 : 0);
				}
			} else {
				switch (o) {
					case PLUS:
					case MULT:
						// Consts right.
						return create(o, b, a);
					case AND:
						return a == Const.ZERO ? Const.ZERO : b;
					case OR:
						return a == Const.ZERO ? b : Const.ONE;
					case LT:
					case GT:
					case LEQ:
					case GEQ:
						// a > b  =>  b < a
						// Consts right and flip operation.
						return create(o.flip(), b, a);
					case EQ:
					case NEQ:
						// Consts right
						return create(o, b, a);
				}
			}
		}

		if (b instanceof Const) {
			switch (o) {
				case MINUS:
				case PLUS:
					if (b == Const.ZERO) {
						return a;
					}
					break;
				case MULT:
					if (b == Const.ZERO) {
						return Const.ZERO;
					}
					if (b == Const.ONE) {
						return a;
					}
					break;
			}

			// Attempt to fold more constants!
			//
			//        (-)
			//        / \
			//      (+)  1       (-)
			//      / \     =>   / \
			//    (-)  1        a   1
			//    / \
			//   a   1
			//
			//      (*)          (*)
			//      / \     =>   / \
			//    (*)  4        a   20
			//    / \
			//   a   5
			//
			if (a instanceof Binary) {
				Binary bin = (Binary) a;
				int myBp = o.getBindingPower();
				int itsBp = bin.op.getBindingPower();

				if (myBp == itsBp && myBp > 40) {
					if (bin.right instanceof Const) {

						if (bin.op == BinOp.MINUS) {
							return create(bin.op,
								bin.left,
								create(o, ((Const) bin.right).negate(), b));
						}

						return create(bin.op,
							bin.left,
							create(o, bin.right, b));
					}
				}
			}
		}

		if (a == b) {
			switch (o) {
				case PLUS:
					return create(BinOp.MULT, a, 2);
				case MINUS:
					return Const.ZERO;
				case LT:
					return Const.ZERO;
				case LEQ:
					return Const.ONE;
				case GT:
					return Const.ZERO;
				case GEQ:
					return Const.ONE;
				case EQ:
					return Const.ONE;
				case NEQ:
					return Const.ZERO;
			}
		}

		if (o == BinOp.AND) {
			return createAnd(a, b);
		}

		if (o == BinOp.OR) {
			return createOr(a, b);
		}

		// (a + 3) - 5 -> a + (3 - 5) -> a - 2
		// (a * 3) * 5 -> a * (3 * 5) -> a * 15
		//
		//       +
		//      / \
		//     -   1
		//    / \
		//   a   1

		return new Binary(o, a, b);
	}

	private static IRNode createOr(IRNode a, IRNode b) {
		Temp t = Temp.create();

		return StmtAndValue.create(
			If.create(
				a,
				Move.create(t, Const.ONE),
				Move.create(t, b)),
			t);
	}

	private static IRNode createAnd(IRNode a, IRNode b) {
		Temp t = Temp.create();

		return StmtAndValue.create(
			If.create(
				a,
				Move.create(t, b),
				Move.create(t, Const.ZERO)),
			t);
	}

	public NodeList children() {
		return NodeList.create(left, right);
	}

	public IRNode build(NodeList list) {
		return Binary.create(op, list.get(0), list.get(1));
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();
		toString(sb);
		return sb.toString();
	}

	public void toString(StringBuilder sb) {
		sb.append("{\"op\":\"");
		sb.append(op.toString());
		sb.append("\",\"left\":");
		left.toString(sb);
		sb.append(",\"right\":");
		right.toString(sb);
		sb.append("}");
	}
}
