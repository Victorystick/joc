package mjc.translate;

import java.util.HashMap;
import java.util.Map;
import mjc.ir.Binary;
import mjc.ir.CJump;
import mjc.ir.Const;
import mjc.ir.IRNode;
import mjc.ir.IRStatement;
import mjc.ir.Jump;
import mjc.ir.Label;
import mjc.ir.StmtSequence;

public class If {
	public static IRStatement create(IRNode cond, IRStatement then) {
		Label tru = Label.create();
		Label fals = Label.create();
		Label join = Label.create();

		if (cond instanceof Const) {
			Const val = (Const)cond;

			if (val.getValue() == 0) {
				return null;
			} else {
				return then;
			}
		}

		IRStatement jump = CJump.create(cond, tru, fals);

		// return StmtSequence.create(
		// 	jump.invert(),
		// 	then,
		// 	fals);

		return StmtSequence.create(
			jump,
			tru,
			then,
			Jump.create(join),
			fals,
			Jump.create(join),
			join);
	}

	public static IRStatement create(IRNode cond, IRStatement then, IRStatement els) {
		Label tru = Label.create();
		Label fals = Label.create();
		Label join = Label.create();

		if (cond instanceof Const) {
			if (cond == Const.ZERO) {
				return els;
			} else {
				return then;
			}
		}

		IRStatement jump;

		// if (cond instanceof Binary && ((Binary) cond).op.isCmp()) {
		// 	Binary bin = (Binary) cond;

		// 	jump = CJump.create(bin.op, bin.left, bin.right, tru, fals);
		// } else {
			jump = CJump.create(cond, tru, fals);
		// }

		// return StmtSequence.create(
		// 	jump,
		// 	els,
		// 	Jump.create(join),
		// 	tru,
		// 	then,
		// 	join);

		return StmtSequence.create(
			jump,
			tru,
			then,
			Jump.create(join),
			fals,
			els,
			Jump.create(join),
			join);
	}
}
