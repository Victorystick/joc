package mjc.live;

import java.util.*;
import mjc.asm.*;
import mjc.ir.Temp;
import mjc.Errors;

public class Interference extends InterferenceGraph {
	private Map<Temp, Set<Instruction>> visited;
	private Set<Instruction> set;
	private FlowAnalysis flow;
	private List<MoveOperation> moveList;

	public Interference(FlowAnalysis fa) {
		visited = new HashMap<Temp, Set<Instruction>>();
		flow = fa;

		List<Instruction> list = fa.getInstructions();

		moveList = new ArrayList<MoveOperation>();
		Set<Temp> live = new HashSet<Temp>();

		for (int j = list.size() - 1; j >= 0; j--) {
			Instruction inst = list.get(j);

			if (inst instanceof MoveOperation) {
				MoveOperation move = (MoveOperation) inst;
				moveList.add(move);
				live.removeAll(move.use());
			}

			live.addAll(inst.def());

			for (Temp t : inst.use()) {
				if (visited.containsKey(t)) {
					set = visited.get(t);
				} else {
					set = new HashSet<Instruction>();
					visited.put(t, set);
				}
				mark(inst, t);
			}

			live.removeAll(inst.def());
			live.addAll(inst.use());
		}

		if (!live.isEmpty()) {
			for (Temp unassigned : live) {
				String name = unassigned.getName();

				if (name != null) {
					Errors.warn("Variable '" + name + "' (" + unassigned +
						") isn't assigned a value before it is used.");
				} else {
					Errors.warn("Variable " + unassigned +
						" isn't assigned a value before it is used.");
				}

				remove(unassigned);
			}
		}
	}

	private void mark(Instruction i, Temp node) {
		for (Instruction instr : flow.predecessors(i)) {
			if (set.contains(instr)) {
				continue;
			}

			for (Temp other : instr.def()) {
				if (node == other) {
					continue;
				}
				addSuccessor(node, other);
				addSuccessor(other, node);
			}

			if (!instr.def().contains(node)) {
				set.add(instr);
				mark(instr, node);
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
