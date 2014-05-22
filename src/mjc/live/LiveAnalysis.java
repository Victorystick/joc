package mjc.live;

import java.util.*;
import java.util.Map.Entry;
import mjc.ir.Temp;
import mjc.asm.Instruction;
import mjc.asm.CommentInstruction;
import mjc.asm.MoveOperation;
import mjc.asm.InstructionSequence;
import mjc.Errors;
import underscore.*;

public class LiveAnalysis extends InterferenceGraph {
	private Pred<Instruction> notAComment = new Pred<Instruction>() {
		public boolean apply(Instruction i) {
			return !(i instanceof CommentInstruction);
		}
	};

	private FlowAnalysis flow;
	private List<MoveOperation> moveList;

	public LiveAnalysis(InstructionSequence seq, FlowAnalysis fa) {
		flow = fa;

		Map<Instruction, Set<Temp>>
			in = new HashMap<Instruction, Set<Temp>>(),
			out = new HashMap<Instruction, Set<Temp>>(),
			inp = new HashMap<Instruction, Set<Temp>>(),
			outp = new HashMap<Instruction, Set<Temp>>();

		List<Instruction> list = _.filter(seq.getInstructions(), notAComment);

		moveList = new ArrayList<MoveOperation>();
		// int index = 0;

		// System.out.println(list);

		for (Instruction i : list) {
			in.put(i, new HashSet<Temp>());
			out.put(i, new HashSet<Temp>());

			if (i instanceof MoveOperation)
				moveList.add((MoveOperation) i);
		}

		// System.out.println(in);

		do {
			for (int j = list.size() - 1; j >= 0; j--) {
			// for (int j = 0; j < list.size(); j++) {
				Instruction i = list.get(j);

				inp.put(i, new HashSet<Temp>(in.get(i)));
				outp.put(i, new HashSet<Temp>(out.get(i)));

				Set<Temp> partial = out.get(i);

				partial.removeAll(i.def());
				partial.addAll(i.use());

				in.put(i, partial);

				partial = new HashSet<Temp>();

				// MoveOperation move = null;
				// if (i instanceof MoveOperation) {
				// 	move = (MoveOperation) i;
				// 	System.out.println();
				// 	System.out.println(i);
				// }

				for (Instruction s : fa.successors(i)) {
					// System.out.println(in.get(s));
					// if (move != null)
					// 	System.out.println(s);
					partial.addAll(in.get(s));
				}


				// System.out.println("partial of " + i);
				// System.out.println(partial);
				out.put(i, partial);
			}
		} while (!in.equals(inp) && !out.equals(outp));

		for (Entry<Instruction, Set<Temp>> e : in.entrySet()) {
			// System.out.println(e.getKey());
			// System.out.println(Arrays.asList(e.getValue().toArray()));

			Set<Temp> set = e.getValue();

			if (e.getKey() instanceof MoveOperation) {
				MoveOperation move = (MoveOperation) e.getKey();

				if (!move.use().isEmpty()) {
					Temp target = move.def().get(0);
					Temp source = move.use().get(0);

					for (Temp t1 : set) {
						if (t1 != target) {
							for (Temp t2 : set) {
								if (t1 != t2 && t2 != source) {
									addSuccessor(t2, t1);
									addSuccessor(t1, t2);
								}
							}
						}
					}
				}

			} else {
				for (Temp t1 : set) {
					for (Temp t2 : set) {
						if (t1 != t2) {
							addSuccessor(t1, t2);
							addSuccessor(t2, t1);
						}
					}
				}
			}
		}
	}

	public FlowAnalysis getFlow() {
		return flow;
	}

	public int degree(Temp t) {
		return successors(t).size();
	}

	public List<MoveOperation> moves() {
		return moveList;
	}
}
