package mjc.canon;

import java.util.ArrayList;
import java.util.List;
import mjc.ir.CJump;
import mjc.ir.IRStatement;
import mjc.ir.Jump;
import mjc.ir.Label;
import mjc.ir.StmtSequence;

public class BasicBlocks {
	public final List<StmtSequence> list;
	private int i;
	private StmtSequence stmts;
	private int length;
	private StmtSequence seq;
	public final Label done;

	public static BasicBlocks create(StmtSequence stmts) {
		return new BasicBlocks(stmts);
	}

	private BasicBlocks(StmtSequence s) {
		// System.out.println(s);

		list = new ArrayList<StmtSequence>();
		i = 0;

		stmts = s;
		length = stmts.size();

		seq = null;
		done = Label.create();

		extractBlock();

		// System.out.println(list);
	}

	private void extractBlock() {
		if (seq != null) {
			list.add(seq);
		}

		if (i >= length) {
			return;
		}

		// IRStatement label = stmts.get(i++);

		// if (!(label instanceof Label)) {
		// 	seq = StmtSequence.create(Label.create(), label);
		// } else {
		// 	seq = StmtSequence.create(label);
		// }

		IRStatement label = stmts.get(i);

		if (!(label instanceof Label)) {
			seq = StmtSequence.create(Label.create());
		} else {
			seq = StmtSequence.create(label);
			i++;
		}

		extractStatements();
	}

	private void extractStatements() {
		IRStatement stmt;

		while (i <= length) {
			if (i >= length) {
				stmt = Jump.create(done);
			} else {
				stmt = stmts.get(i);
			}

			// System.out.println(seq);
			// System.out.println("gonna add: " + stmt);

			if (stmt instanceof Label) {
				seq.then(Jump.create((Label) stmt));
				extractBlock();
				return;
			}

			i++;

			seq.then(stmt);

			if (stmt instanceof CJump) {
				seq.then(Jump.create(((CJump) stmt).fals));
				// System.out.println("CJump::: " + stmt);
			}

			if (stmt instanceof Jump || stmt instanceof CJump) {
				// System.out.println("Done:::: " + seq);
				extractBlock();
				return;
			}
		}
	}
}
