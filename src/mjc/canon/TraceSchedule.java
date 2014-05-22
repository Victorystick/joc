package mjc.canon;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import mjc.Errors;
import mjc.ir.*;

public class TraceSchedule {

	Map<Label, StmtSequence> table = new HashMap<Label, StmtSequence>();
	StmtSequence seq;
	BasicBlocks blocks;
	List<StmtSequence> list;
	int listIndex = 0;

	public static StmtSequence schedule(BasicBlocks b) {
		return new TraceSchedule(b).seq;
	}

	public TraceSchedule(BasicBlocks b) {
		blocks = b;
		list = b.list;

		for (StmtSequence stmts : list) {
			Label label = (Label) stmts.get(0);
			table.put(label, stmts);
		}

		seq = new StmtSequence();

		getNext();

		table = null;
	}

	StmtSequence getNext() {
		if (list.size() - listIndex == 0) {
			return StmtSequence.create(blocks.done);
		} else {
			StmtSequence s = list.get(listIndex++);

			Label label = (Label)s.get(0);

 			if (table.containsKey(label)) {
				trace(s);
				// trace(s) adds s to the sequence.
				// Don't have to return s, and maybe add it again.
				return null;
			} else {
				return getNext();
			 }
		}
	}

	void trace(StmtSequence s) {
		// Add s to seq.
		seq.then(s);

		while (true) {
			Label label = (Label)s.get(0);

			table.remove(label);

			// out last == Appel's last.tail
			IRStatement last = s.get(s.size() - 1);

			if (last instanceof Jump) {
				Jump jump = (Jump) last;

				StmtSequence target = table.get(((Name)jump.target).label);

				if (target != null) {
					// Remove uneccessary jump.
					seq.remove(seq.size() - 1);

					// Then execute target block
					seq.then(target);

					// continue with jump target
					s = target;
				} else {
					seq.then(getNext());
					// last.tail.tail=getNext();
					return;
				}
			} else if (last instanceof CJump) {
				CJump jump = (CJump) last;

				StmtSequence tru = table.get(jump.tru);
				StmtSequence fals = table.get(jump.fals);

				if ( fals != null ) {
					// Then fallthrough to the false block
					seq.then(fals);

					// continue with fals
					s = fals;
				}	else if ( tru != null ) {
					// Remove the jump.
					seq.remove(seq.size() - 1);

					// Then invert the jump and
					// fallthrough to the true block
					seq.then(jump.invert());

					// Then execute tru block
					seq.then(tru);

					// continue with tru
					s = tru;
				}	else {
					Label falseLabel = Label.create();

					seq
						.then(StmtSequence.create(
							CJump.create(
								Binary.create(jump.cmp, jump.left, jump.right),
								// jump.cmp,
								// jump.left, jump.right,
								jump.tru, falseLabel),
							falseLabel,
							Jump.create(jump.fals)));
					getNext();
					return;
				}
			} else {
				Errors.fatal("Bad basic block in TraceSchedule");
			}
		}
	}
}
