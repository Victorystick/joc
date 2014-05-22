package mjc.asm;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import mjc.ir.Temp;
import underscore._;
import underscore.Mapper;

public class InstructionSequence extends Instruction {
	protected List<Instruction> list;

	public InstructionSequence() {
		super(null);
		list = new ArrayList<Instruction>();
	}

	public static InstructionSequence create(Instruction... is) {
		InstructionSequence seq = new InstructionSequence();

		for (Instruction i : is) {
			seq.then(i);
		}

		return seq;
	}

	public static InstructionSequence create(Instruction i) {
		if (i instanceof InstructionSequence) {
			return (InstructionSequence) i;
		}

		return new InstructionSequence().then(i);
	}

	public InstructionSequence then(Instruction i) {
		if (i instanceof InstructionSequence) {
			list.addAll(((InstructionSequence) i).list);
		} else if (i != null) {
			list.add(i);
		}

		return this;
	}

	public InstructionSequence then_(Instruction i) {
		list.add(i);
		return this;
	}

	public InstructionSequence before(Instruction i) {
		if (i instanceof InstructionSequence) {
			list.addAll(0, ((InstructionSequence) i).list);
		} else if (i != null) {
			list.add(0, i);
		}

		return this;
	}

	public String allocateAll(Mapper<Temp, String> allocator) {
		List<String> assembly = new ArrayList<String>();

		for (Instruction i : list) {
			String s = i.allocate(allocator);

			if (s != null)
				assembly.add(s);
		}

		return _.join(assembly, "\n");
	}

	public String toString() {
		return _.join(list, "\n");
	}

	public List<Instruction> getInstructions() {
		return list;
	}
}
