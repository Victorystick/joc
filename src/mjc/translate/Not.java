package mjc.translate;

import mjc.ir.*;

public class Not {
	public static IRNode create(IRNode n) {
		if (n instanceof Const) {
			return n == Const.ZERO ? Const.ONE : Const.ZERO;
		}

		return Binary.create(BinOp.MINUS, 1, n);
	}
}
