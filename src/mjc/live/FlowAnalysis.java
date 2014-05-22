package mjc.live;

import java.util.List;
import mjc.asm.CommentInstruction;
import mjc.asm.Instruction;
import mjc.asm.InstructionSequence;
import mjc.asm.JumpOperation;
import mjc.asm.LabelInstruction;
import mjc.ir.Label;
import underscore.*;

public class FlowAnalysis extends Graph<Instruction> {
	private List<Instruction> ins;

	private Pred<Instruction> notAComment = new Pred<Instruction>() {
		public boolean apply(Instruction i) {
			return !(i instanceof CommentInstruction);
		}
	};

	public FlowAnalysis() {

	}

	public FlowAnalysis( InstructionSequence is ) {
		ins = _.filter(is.getInstructions(), notAComment);

		Instruction in;
		int end = ins.size() - 1;
L1:	for (int i = 0; i < end; i++) {
			in = ins.get(i);

			if (in instanceof JumpOperation) {
				JumpOperation jump = (JumpOperation) in;

L2:			for (Label l : jump.jumps()) {
L3:				for (Instruction j : ins) {
						if ( j instanceof LabelInstruction ) {
							if ( ((LabelInstruction) j).is(l) ) {
								addSuccessor(in, j);
								break L2;
							}
						}
					}
					//throw new NoDestinationException(l);
				}

				if (jump.canFallThrough()) {
					addSuccessor(in, ins.get(i + 1));
				}
			} else {
				addSuccessor(in, ins.get(i + 1));
			}
		}
	}

	public List<Instruction> getInstructions() {
		return ins;
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();

		sb.append("degree: " + degree() + "\n");

		for (Instruction i : ins) {
			// sb.append(i.assembly);
			// sb.append("\n");
			sb.append(i);
			sb.append("\n|\n");
			sb.append("V\n");
			sb.append(successors(i));
			sb.append("\n");
		}

		return sb.toString();
	}
}

class NoDestinationException extends Exception {
	Label dest;
	public NoDestinationException(Label destination) {
		dest = destination;
	}

	public String toString() {
		return "No destination found for jump to " + dest.label;
	}
}
