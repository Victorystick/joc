package mjc.translate;

import mjc.ir.Const;
import mjc.ir.IRNode;
import mjc.ir.Label;
import mjc.ir.IRStatement;
import mjc.ir.StmtSequence;
import mjc.ir.CJump;
import mjc.ir.Jump;

import java.util.Map;
import java.util.HashMap;

public class While {
	public static IRStatement create(IRNode cond, IRStatement then) {
		Label test = Label.create();
		Label body = Label.create();
		Label done = Label.create();

		return StmtSequence.create(
			test,
			CJump.create(cond, body, done),
			body,
			then,
			Jump.create(test),
			done);
	}
}
